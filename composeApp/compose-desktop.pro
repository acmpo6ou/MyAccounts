-dontwarn java.awt.*
-ignorewarnings

# for some reason ProGuard removes Dispatchers.Main on Desktop.
# if more stuff gets removed, check here for more rules that could help:
# https://youtrack.jetbrains.com/issue/CMP-4288
-keep class * implements kotlinx.coroutines.internal.MainDispatcherFactory

# https://github.com/whyoleg/cryptography-kotlin/issues/51
-keep class dev.whyoleg.cryptography.*
-keep class dev.whyoleg.cryptography.providers.jdk.*