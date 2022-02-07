package app.plantdiary

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.plantdiary.dto.Plant
import app.plantdiary.service.PlantService
import junit.framework.Assert.*
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class PlantTests {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    lateinit var plantService : PlantService
    var allPlants : List<Plant>? = ArrayList<Plant>()

    @Test
    fun `Given plant data are available when I search for Redbud then I should receive Cercis canadensis` () = runTest {
        givenPlantServiceIsInitialized()
        whenPlantDataAreReadAndParsed()
        thenThePlantCollectionShouldContainCercisCanadensis()
    }

    private fun givenPlantServiceIsInitialized() {
       plantService = PlantService()
    }

    private suspend fun whenPlantDataAreReadAndParsed() {
        allPlants = plantService.fetchPlants()
    }

    private fun thenThePlantCollectionShouldContainCercisCanadensis() {
        assertNotNull(allPlants)
        assertTrue(allPlants!!.isNotEmpty())
        var containsCercisCanadensis = false
        allPlants!!.forEach {
            if (it.genus.equals(("Cercis")) && it.species.equals("canadensis")) {
                containsCercisCanadensis = true
            }
        }
        assertTrue(containsCercisCanadensis)
    }

}