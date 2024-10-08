package com.dilip.chess_kmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform