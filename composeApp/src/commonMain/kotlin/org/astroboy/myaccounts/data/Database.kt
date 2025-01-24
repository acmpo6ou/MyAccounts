package org.astroboy.myaccounts.data

import kotlinx.serialization.Serializable

@Serializable
data class Database(
    val name: String,
    val password: String,
    val accounts: Accounts,
)
