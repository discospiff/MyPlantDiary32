package app.plantdiary.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import app.plantdiary.dto.Plant

@Dao
interface ILocalPlantDAO {

    @Query("SELECT * FROM plants")
    fun getAllPlants() : LiveData<List<Plant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(plants: ArrayList<Plant>)

    @Delete
    fun delete(plant : Plant)

}