package com.syntx34.wifilab.domain.model

data class ProtocolExplanation(
    val id: String,
    val name: String,
    val shortName: String,
    val summary: String,
    val authenticationFlow: List<String>,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val researcherNote: String,
)

data class SecurityTip(
    val id: String,
    val title: String,
    val detail: String,
    val category: String,
)
