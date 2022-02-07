package app.plantdiary.service

import app.plantdiary.RetrofitClientInstance
import app.plantdiary.dao.IPlantDAO
import app.plantdiary.dto.Plant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse

class PlantService {

    suspend fun fetchPlants() : List<Plant>? {
        return withContext(Dispatchers.IO) {
            val service = RetrofitClientInstance.retrofitInstance?.create(IPlantDAO::class.java)
            val plants = async {service?.getAllPlants()}
            var result = plants.await()?.awaitResponse()?.body()
            return@withContext result
        }
    }
}