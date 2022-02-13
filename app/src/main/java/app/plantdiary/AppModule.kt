package app.plantdiary

import app.plantdiary.service.IPlantService
import app.plantdiary.service.PlantService
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { MainViewModel(get()) }
    single<IPlantService> { PlantService() }
}