package org.astroboy.myaccounts.data

data class Database(
    val name: String,
    val password: String = "",
    val accounts: Accounts = mapOf(),
) {
    val isOpen get() = password.isNotEmpty()
    val isSaved: Boolean get() = TODO("implement")
}
