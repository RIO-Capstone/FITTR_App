plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.jacoco)
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest") // Ensure tests run before report generation

    reports {
        xml.required.set(true)
        xml.outputLocation.set(file("${project.buildDir}/reports/jacoco/jacocoTestReport.xml"))
        html.required.set(true)
        html.outputLocation.set(file("${project.buildDir}/reports/jacoco/unitTest/html"))
        csv.required.set(false)
    }

    val fileFilter = listOf(
        "**/R.class", "**/R\$*.class", "**/BuildConfig.*",
        "**/Manifest*.*", "android/**/*.*"
    )

    val javaClasses = fileTree("${project.buildDir}/intermediates/javac/debug") {
        exclude(fileFilter)
    }

    val kotlinClasses = fileTree("${project.buildDir}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    sourceDirectories.setFrom(files("$projectDir/src/main/java"))
    classDirectories.setFrom(files(javaClasses, kotlinClasses))
    executionData.setFrom(fileTree(baseDir = project.buildDir) {
        include(
            "outputs/unit_test_code_coverage/debugUnitTest/*.exec",
            "jacoco/testDebugUnitTest.exec",
            "jacoco/*.exec"
        )
    })

}

tasks.register<JacocoReport>("jacocoAndroidTestReport") {
    dependsOn("connectedDebugAndroidTest") // This ensures instrumented tests run first

    reports {
        xml.required.set(true)
        xml.outputLocation.set(file("${project.buildDir}/reports/jacoco/androidTest/jacocoAndroidTestReport.xml"))
        html.required.set(true)
        html.outputLocation.set(file("${project.buildDir}/reports/jacoco/androidTest/html"))
    }

    val fileFilter = listOf(
        "**/R.class", "**/R\$*.class", "**/BuildConfig.*",
        "**/Manifest*.*", "**/*Test*.*", "android/**/*.*"
    )

    val javaClasses = fileTree("${project.buildDir}/intermediates/javac/debug") {
        exclude(fileFilter)
    }

    val kotlinClasses = fileTree("${project.buildDir}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    sourceDirectories.setFrom(files("$projectDir/src/main/java"))
    classDirectories.setFrom(files(javaClasses, kotlinClasses))

    // The path for instrumented test coverage is different
    executionData.setFrom(fileTree(baseDir = project.buildDir) {
        include("outputs/code_coverage/debugAndroidTest/connected/**/*.ec")
    })
}

tasks.register<JacocoReport>("jacocoFullReport") {
    dependsOn("jacocoTestReport", "jacocoAndroidTestReport")

    reports {
        xml.required.set(true)
        xml.outputLocation.set(file("${project.buildDir}/reports/jacoco/full/jacocoFullReport.xml"))
        html.required.set(true)
        html.outputLocation.set(file("${project.buildDir}/reports/jacoco/full/html"))
    }

    val fileFilter = listOf(
        "**/R.class", "**/R\$*.class", "**/BuildConfig.*",
        "**/Manifest*.*", "**/*Test*.*", "android/**/*.*","**/OverlayView.*",
        "**/CameraFragment.*","**/PoseLandmarkerHelper.*","**/ExerciseSuccessFragment.*","**/BluetoothHelper.*",
        "**/databinding/**"
    )

    val javaClasses = fileTree("${project.buildDir}/intermediates/javac/debug") {
        exclude(fileFilter)
    }

    val kotlinClasses = fileTree("${project.buildDir}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    sourceDirectories.setFrom(files("$projectDir/src/main/java"))
    classDirectories.setFrom(files(javaClasses, kotlinClasses))

    // Combine execution data from both unit tests and instrumented tests
    executionData.setFrom(fileTree(baseDir = project.buildDir) {
        include(
            "outputs/unit_test_code_coverage/debugUnitTest/*.exec",
            "jacoco/testDebugUnitTest.exec",
            "outputs/code_coverage/debugAndroidTest/connected/**/*.ec"
        )
    })
}

android {
    namespace = "com.example.fittr_app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fittr_app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packaging {
        resources {
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/LICENSE-notice.md"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            enableUnitTestCoverage = true
        }
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
    buildFeatures {
        viewBinding = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.systemProperty("robolectric.enabledSdks", "30")
                it.jvmArgs("-noverify") // May help with certain Robolectric issues
            }
        }
        unitTests.all {
            testCoverage {
                (this as? JacocoTaskExtension)?.apply {
                    isIncludeNoLocationClasses = true
                    excludes = listOf("jdk.internal.*")
                }
            }
        }
    }
    tasks.withType<Test>().configureEach {
        useJUnit()
        finalizedBy(tasks.named("jacocoTestReport"))
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.mediapipe.tasks.vision)
    implementation(libs.bundles.camerax)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.mpandroidchart)
    implementation(libs.androidx.espresso.intents)
    implementation(libs.mockwebserver)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)  // Needed for mocking final classes & singletons
    testImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.androidx.espresso.contrib.v361)
    androidTestImplementation(libs.mockito.core)
    androidTestImplementation(libs.mockk.android)
}