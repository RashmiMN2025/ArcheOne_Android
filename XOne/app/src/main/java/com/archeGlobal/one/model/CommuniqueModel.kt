package com.archeGlobal.one.model

data class CommuniqueModel(
    val title: String = "Communique",
    val communiques: List<Communique> = emptyList(),
) {
    data class Communique(
        val communiqueName: String,
        val filePath: String,
        val showSosButton: Boolean = false,
        val previewUrl: String? = null,
    )
}
