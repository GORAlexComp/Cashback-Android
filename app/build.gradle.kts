import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.jetbrains.kotlin.android)
	id("com.google.gms.google-services")
}

android {
	namespace = "ua.pp.gac.cashback_android"
	compileSdk = 34

	defaultConfig {
		applicationId = "ua.pp.gac.cashback_android"
		minSdk = 26
		targetSdk = 34

		val versionPropsFile = file("version.properties")
		val versionProps = Properties()

		if (versionPropsFile.canRead()) {
			versionProps.load(FileInputStream(versionPropsFile))

			versionCode = versionProps["VERSION_CODE"].toString().toInt()
			versionName = versionProps["VERSION_NAME"].toString()
		} else {
			throw GradleException("Could not read version.properties!")
		}

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		signingConfig = signingConfigs.getByName("debug")
		multiDexEnabled = true
	}

	buildTypes {
		release {
			isMinifyEnabled = true
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
			signingConfig = signingConfigs.getByName("debug")
			isJniDebuggable = false
			isDebuggable = false
			multiDexEnabled = true
			renderscriptOptimLevel = 3
		}
		getByName("debug") {
			isDebuggable = true
			isJniDebuggable = true
			signingConfig = signingConfigs.getByName("debug")
			isMinifyEnabled = false
			multiDexEnabled = true
			matchingFallbacks += listOf()
			proguardFiles()
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
	kotlinOptions {
		jvmTarget = "17"
	}
	buildFeatures {
		viewBinding = true
	}
	ndkVersion = "27.1.12297006"
	buildToolsVersion = "34.0.0"
}

tasks.register("incrementVersionCode") {
	doLast {
		val versionPropsFile = file("version.properties")
		val versionProps = Properties()

		if (versionPropsFile.canRead()) {
			versionProps.load(FileInputStream(versionPropsFile))

			val currentVersionCode = versionProps["VERSION_CODE"].toString().toInt() + 1
			versionProps["VERSION_CODE"] = currentVersionCode.toString()

			versionProps.store(FileOutputStream(versionPropsFile), null)
			println("Version code updated to $currentVersionCode")
		} else {
			throw GradleException("Could not read version.properties!")
		}
	}
}

tasks.named("preBuild") {
	dependsOn("incrementVersionCode")
}

dependencies {
	implementation(libs.core)
	implementation(libs.material)

	implementation(libs.androidx.activity)
	implementation(libs.androidx.appcompat)
	implementation(libs.androidx.constraintlayout)
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.navigation.fragment.ktx)
	implementation(libs.androidx.navigation.ui.ktx)
	implementation(libs.androidx.sqlite.ktx)
	implementation(libs.androidx.camera.camera2)
	implementation(libs.androidx.camera.core)
	implementation(libs.androidx.camera.extensions)
	implementation(libs.androidx.camera.lifecycle)
	implementation(libs.androidx.camera.view)
	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.kotlinx.coroutines.core)

	implementation(platform(libs.firebase.bom))
	implementation(libs.firebase.analytics)
	implementation(libs.firebase.storage)

	implementation(libs.android.image.slider)
	implementation(libs.barcode.scanning)
	implementation(libs.glide)
	implementation(libs.zxing.android.embedded)

	testImplementation(libs.junit)
	debugImplementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.8.6")
	debugImplementation("androidx.lifecycle:lifecycle-runtime:2.8.6")
	debugImplementation("androidx.customview:customview-poolingcontainer:1.0.0")
}
