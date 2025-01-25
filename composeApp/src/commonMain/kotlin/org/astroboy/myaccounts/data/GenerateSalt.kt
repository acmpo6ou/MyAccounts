package org.astroboy.myaccounts.data

import dev.whyoleg.cryptography.random.CryptographyRandom

class GenerateSalt {
    operator fun invoke() = CryptographyRandom.nextBytes(16)
}
