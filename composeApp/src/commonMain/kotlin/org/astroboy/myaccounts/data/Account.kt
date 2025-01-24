package org.astroboy.myaccounts.data

import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val accountName: String,
    val username: String,
    val email: String,
    val passwords: Map<String, String>,
    val notes: String,
    val copyEmail: Boolean = true,
    val attachedFiles: Map<String, String>,
)

typealias Accounts = Map<String, Account>
