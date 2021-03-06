object Version {
    const val Kotlin = "1.3.70"
    const val Core = "1.2.0"
    const val AppCompat = "1.1.0"
    const val ConstraintLayout = "1.1.3"
    const val FireStore = "21.0.0"
    const val JUnit = "4.13"
    const val AndroidXTest = "1.2.0"
    const val Espresso = "3.2.0"
    const val AutoService = "1.0-rc6"
    const val KotlinPoet = "1.3.0"
    const val MockitoKotlin = "2.1.0"
    const val JetbrainsAnnotation = "16.0.2"
    const val Material = "1.0.0"
    const val RxJava = "2.2.12"
    const val RxAndroid = "2.1.1"
}

object Kotlin {
    const val StdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Version.Kotlin}"
    const val Reflect = "org.jetbrains.kotlin:kotlin-reflect:${Version.Kotlin}"
    const val Annotation = "org.jetbrains:annotations:${Version.JetbrainsAnnotation}"
}

object Jetpack {
    const val CoreKtx = "androidx.core:core-ktx:${Version.Core}"
    const val AppCompat = "androidx.appcompat:appcompat:${Version.AppCompat}"
    const val ConstraintLayout = "androidx.constraintlayout:constraintlayout:${Version.ConstraintLayout}"
    const val TestRunner = "androidx.test:runner:${Version.AndroidXTest}"
    const val EspressoCore = "androidx.test.espresso:espresso-core:${Version.Espresso}"
    const val Material = "com.google.android.material:material:${Version.Material}"
}

object Firebase {
    const val FireStore = "com.google.firebase:firebase-firestore:${Version.FireStore}"
}

object AptLib {
    const val AutoService = "com.google.auto.service:auto-service:${Version.AutoService}"
    const val KotlinPoet = "com.squareup:kotlinpoet:${Version.KotlinPoet}"
}

object CommonLib {
    const val RxJava = "io.reactivex.rxjava2:rxjava:${Version.RxJava}"
    const val RxAndroid = "io.reactivex.rxjava2:rxandroid:${Version.RxAndroid}"
}

object TestLib {
    const val JUnit = "junit:junit:${Version.JUnit}"
    const val MockitoKotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:${Version.MockitoKotlin}"
}
