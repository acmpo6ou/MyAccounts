package org.astroboy.myaccounts.data

import dev.whyoleg.cryptography.random.CryptographyRandom

const val SALT_LENGTH = 16

class GenerateSalt {
    operator fun invoke() = CryptographyRandom.nextBytes(SALT_LENGTH)
}
