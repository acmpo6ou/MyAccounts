package org.astroboy.myaccounts

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform