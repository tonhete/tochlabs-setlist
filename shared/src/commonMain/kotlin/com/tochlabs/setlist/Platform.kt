package com.tochlabs.setlist

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform