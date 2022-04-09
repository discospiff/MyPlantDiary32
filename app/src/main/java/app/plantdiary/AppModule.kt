package app.plantdiary

import app.plantdiary.service.IPlantService
import app.plantdiary.service.PlantService
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { MainViewModel(get()) }
    viewModel { ApplicationViewModel(androidApplication())}
    single<IPlantService> { PlantService(androidApplication()) }
}