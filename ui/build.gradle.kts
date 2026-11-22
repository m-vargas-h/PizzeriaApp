// Módulo de UI: pantallas en Jetpack Compose, tema y ViewModel que expone los datos con LiveData
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "cl.duoc.pizzeriaapp.ui"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        // Desugaring: java.time (LocalDate) con minSdk 24, igual que en app
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // api: la pantalla y el ViewModel exponen tipos de data (EstadoPizzeria, Pedido...) a quien use :ui
    api(project(":data"))
    implementation(project(":utils"))

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // ViewModel y LiveData: api porque PizzeriaViewModel (ViewModel) expone un LiveData a MainActivity
    api(libs.androidx.lifecycle.viewmodel.ktx)
    api(libs.androidx.lifecycle.livedata.ktx)

    // Coroutines: viewModelScope lanza la carga en el hilo principal de Android
    implementation(libs.kotlinx.coroutines.android)

    coreLibraryDesugaring(libs.desugar.jdk.libs)
}
