package com.syntx34.wifilab.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.syntx34.wifilab.data.local.dao.PacketCaptureDao
import com.syntx34.wifilab.data.local.entity.PacketCaptureEntity
import com.syntx34.wifilab.data.packet.DemoCaptureFactory
import com.syntx34.wifilab.data.packet.PcapParser
import com.syntx34.wifilab.domain.model.ParsedCapture
import com.syntx34.wifilab.domain.model.StoredCapture
import com.syntx34.wifilab.domain.repository.PacketCaptureRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class PacketCaptureRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val packetCaptureDao: PacketCaptureDao,
    private val parser: PcapParser,
) : PacketCaptureRepository {

    private val captureDirectory = File(context.filesDir, "captures").apply { mkdirs() }

    override fun observeCaptures(): Flow<List<StoredCapture>> =
        packetCaptureDao.observeAll().map { captures ->
            captures.map { entity ->
                StoredCapture(
                    id = entity.id,
                    fileName = entity.displayName,
                    importedAt = entity.importedAt,
                    sizeBytes = entity.sizeBytes,
                    packetCount = entity.packetCount,
                    eapolCount = entity.eapolCount,
                    durationMillis = entity.durationMillis,
                )
            }
        }

    override suspend fun importCapture(uri: String, displayName: String?): ParsedCapture =
        withContext(Dispatchers.IO) {
            val parsedUri = Uri.parse(uri)
            val resolvedName = sanitizeName(
                displayName
                    ?: queryDisplayName(parsedUri)
                    ?: "capture-${System.currentTimeMillis()}.pcap",
            )
            val storedFile = File(captureDirectory, "${UUID.randomUUID()}.capture")
            try {
                copyToPrivateStorage(parsedUri, storedFile)
                val bytes = storedFile.readBytes()
                val capture = parser.parse(bytes, resolvedName)
                packetCaptureDao.insert(
                    PacketCaptureEntity(
                        displayName = resolvedName,
                        storedFileName = storedFile.name,
                        importedAt = System.currentTimeMillis(),
                        sizeBytes = storedFile.length(),
                        packetCount = capture.summary.packetCount,
                        eapolCount = capture.summary.eapolCount,
                        durationMillis = capture.summary.durationMillis,
                    ),
                )
                capture
            } catch (error: Exception) {
                storedFile.delete()
                throw error
            }
        }

    override suspend fun loadCapture(id: Long): ParsedCapture? = withContext(Dispatchers.IO) {
        val entity = packetCaptureDao.getById(id) ?: return@withContext null
        val file = File(captureDirectory, entity.storedFileName)
        if (!file.isFile) return@withContext null
        parser.parse(file.readBytes(), entity.displayName)
    }

    override suspend fun loadDemoCapture(): ParsedCapture = withContext(Dispatchers.IO) {
        parser.parse(
            bytes = DemoCaptureFactory.createPcapBytes(),
            fileName = "simulated-eapol-demo.pcap",
        )
    }

    override suspend fun deleteCapture(id: Long) = withContext(Dispatchers.IO) {
        val entity = packetCaptureDao.getById(id)
        if (entity != null) {
            File(captureDirectory, entity.storedFileName).delete()
            packetCaptureDao.deleteById(id)
        }
    }

    override suspend fun clearCaptures() = withContext(Dispatchers.IO) {
        val files = packetCaptureDao.allStoredFileNames()
        files.forEach { name -> File(captureDirectory, name).delete() }
        captureDirectory.listFiles()?.forEach { it.delete() }
        packetCaptureDao.deleteAll()
    }

    private fun copyToPrivateStorage(uri: Uri, destination: File) {
        val resolver = context.contentResolver
        val input = resolver.openInputStream(uri)
            ?: throw IllegalArgumentException("The selected file could not be opened")
        input.use { source ->
            destination.outputStream().use { sink ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var total = 0L
                while (true) {
                    val read = source.read(buffer)
                    if (read < 0) break
                    total += read
                    if (total > MAX_CAPTURE_BYTES) {
                        throw IllegalArgumentException("Captures are limited to 64 MB and remain on this device")
                    }
                    sink.write(buffer, 0, read)
                }
                if (total <= 0L) {
                    throw IllegalArgumentException("The selected file is empty")
                }
            }
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null,
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(0)
                } else {
                    null
                }
            }
        }.getOrNull()
    }

    private fun sanitizeName(name: String): String {
        val cleaned = name
            .substringAfterLast('/')
            .substringAfterLast('\\')
            .replace(Regex("[^A-Za-z0-9._ ()\\[\\]-]"), "_")
            .trim()
            .take(100)
        return cleaned.ifBlank { "unnamed-capture.pcap" }
    }

    companion object {
        private const val MAX_CAPTURE_BYTES = 64L * 1024L * 1024L
    }
}
