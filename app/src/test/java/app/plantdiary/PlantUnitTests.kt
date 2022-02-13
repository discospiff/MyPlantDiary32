package app.plantdiary

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import app.plantdiary.dto.Plant
import app.plantdiary.service.PlantService
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import junit.framework.Assert.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.*
import org.junit.rules.TestRule
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class PlantUnitTests {
    @get:Rule var rule: TestRule = InstantTaskExecutorRule()

    lateinit var mvm : MainViewModel

    @MockK
    lateinit var mockPlantService : PlantService

    private val mainThreadSurrogate = newSingleThreadContext("Main Thread")

    @Before
    fun initMocksAndMainThread() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    @Test
    fun `given a view model with live data when populated with plants then results show eastern redbud` () {
        givenViewModelIsInitializedWithMockData()
        whenPlantServiceFetchCountriesInvoked()
        thenResultsShouldContainCercisCanadensis()
    }

    private fun givenViewModelIsInitializedWithMockData() {
        val plants = ArrayList<Plant>()
        plants.add(Plant("Cercis", "canadensis", "Eastern Redbud"))
        val redOak = Plant("Quercus", "rubra", "Red Oak")
        plants.add(redOak)
        plants.add(Plant("Quercus", "alba", "White Oak"))

        coEvery { mockPlantService.fetchPlants() } returns plants

        mvm = MainViewModel(plantService = mockPlantService)
    }

    private fun whenPlantServiceFetchCountriesInvoked() {
        mvm.fetchPlants()
    }

    private fun thenResultsShouldContainCercisCanadensis() {
        var allPlants : List<Plant>? = ArrayList<Plant>()
        val latch = CountDownLatch(1)
        val observer = object : Observer<List<Plant>> {
            override fun onChanged(receivedPlants: List<Plant>?) {
                allPlants = receivedPlants
                latch.countDown()
                mvm.plants.removeObserver(this)
            }
        }
        mvm.plants.observeForever(observer)
        latch.await(10, TimeUnit.SECONDS)
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