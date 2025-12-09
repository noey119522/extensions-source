plugins {
    id("com.android.application")
    id("kotlin-android")
}

ext {
    extName = "Neko Hentai"
    pkgNameSuffix = "th.nekohentai"
    extClass = ".NekoHentai"
    extVersionCode = 1
    isNsfw = true // สำคัญ: ระบุว่าเป็น 18+
}

apply(from = "$rootDir/common.gradle")
