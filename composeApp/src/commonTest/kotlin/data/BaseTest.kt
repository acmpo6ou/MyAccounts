package data

import org.astroboy.myaccounts.data.Account

val account = Account(
    accountName = "gmail",
    username = "Gmail User",
    email = "example@gmail.com",
    passwords = mapOf("Password" to "123"),
    notes = "My gmail account.",
    copyEmail = false,
    attachedFiles = mapOf(
        "file1" to "ZmlsZTEgY29udGVudAo=",
        "file2" to "ZmlsZTIgY29udGVudAo=",
    ),
)

val account2 = Account(
    accountName = "mega",
    username = "Mega User",
    email = "example@mega.com",
    passwords = mapOf("Password" to "312", "Key" to "KOTLIN_CONF_2025"),
    notes = "My MEGA account.",
    copyEmail = true,
    attachedFiles = mapOf(),
)

val accounts = mapOf(
    account.accountName to account,
    account2.accountName to account2,
)

val salt = "0123456789abcdef".toByteArray()
