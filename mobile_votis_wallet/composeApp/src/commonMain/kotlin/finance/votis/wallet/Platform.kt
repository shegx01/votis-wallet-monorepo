package finance.votis.wallet

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform