package com.syntx34.wifilab.data.repository

import com.syntx34.wifilab.domain.content.AttackContent
import com.syntx34.wifilab.domain.content.ExerciseContent
import com.syntx34.wifilab.domain.content.GlossaryContent
import com.syntx34.wifilab.domain.content.LessonContent
import com.syntx34.wifilab.domain.content.ProtocolContent
import com.syntx34.wifilab.domain.content.RecommendationContent
import com.syntx34.wifilab.domain.model.AttackScenario
import com.syntx34.wifilab.domain.model.ProtocolExplanation
import com.syntx34.wifilab.domain.model.ResearchEntry
import com.syntx34.wifilab.domain.model.ResearchSection
import com.syntx34.wifilab.domain.repository.ResearchRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalResearchRepository @Inject constructor() : ResearchRepository {

    override fun entries(section: ResearchSection?): List<ResearchEntry> {
        val all = GlossaryContent.terms +
            ProtocolContent.researchEntries +
            LessonContent.lessons +
            ExerciseContent.exercises +
            RecommendationContent.recommendations
        return if (section == null) all else all.filter { it.section == section }
    }

    override fun search(query: String, section: ResearchSection?): List<ResearchEntry> {
        val normalized = query.trim().lowercase()
        val source = entries(section)
        if (normalized.isEmpty()) return source
        return source.filter { entry ->
            entry.title.lowercase().contains(normalized) ||
                entry.subtitle.lowercase().contains(normalized) ||
                entry.overview.lowercase().contains(normalized) ||
                entry.keyPoints.any { it.lowercase().contains(normalized) } ||
                entry.details.any { it.lowercase().contains(normalized) }
        }
    }

    override fun entry(id: String): ResearchEntry? =
        entries(null).firstOrNull { it.id == id }

    override fun protocols(): List<ProtocolExplanation> = ProtocolContent.protocols

    override fun attacks(): List<AttackScenario> = AttackContent.scenarios

    override fun attack(id: String): AttackScenario? = AttackContent.scenarios.firstOrNull { it.id == id }
}
