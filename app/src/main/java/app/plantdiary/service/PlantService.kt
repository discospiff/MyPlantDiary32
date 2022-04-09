package app.plantdiary.service

import android.app.Application
import android.content.ContentValues.TAG
import android.util.Log
import androidx.room.Room
import app.plantdiary.RetrofitClientInstance
import app.plantdiary.dao.ILocalPlantDAO
import app.plantdiary.dao.IPlantDAO
import app.plantdiary.dao.PlantDatabase
import app.plantdiary.dto.Plant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse

interface IPlantService {
    suspend fun fetchPlants() : List<Plant>?
    fun getLocalPlantDAO(): ILocalPlantDAO
}

class PlantService(val application: Application) : IPlantService {

    lateinit var db: PlantDatabase

    override suspend fun fetchPlants() : List<Plant>? {
        return withContext(Dispatchers.IO) {
            val service = RetrofitClientInstance.retrofitInstance?.create(IPlantDAO::class.java)
            val plants = async {service?.getAllPlants()}
            var result = plants.await()?.awaitResponse()?.body()
            updateLocalPlants(result)
            return@withContext result
        }
    }

    private suspend fun updateLocalPlants(plants : ArrayList<Plant>?) {
        try {
            plants?.let {
                val localPlantDAO = getLocalPlantDAO()
                localPlantDAO.insertAll(plants)
            }
        } catch (e: Exception) {
            Log.e(TAG, "error saving countries ${e.message}")
        }
    }

    override fun getLocalPlantDAO(): ILocalPlantDAO {
        if (!this::db.isInitialized) {
            db = Room.databaseBuilder(application, PlantDatabase::class.java, "myplants").build()
        }
        return db.localPlantDAO()
    }
}