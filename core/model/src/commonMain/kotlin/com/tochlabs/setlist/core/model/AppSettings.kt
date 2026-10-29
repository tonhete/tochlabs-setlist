package com.tochlabs.setlist.core.model

data class AppSettings(
    val language: AppLanguage,
    val defaultPauseSeconds: Int,
)

enum class AppLanguage { SYSTEM, ES, EN }
