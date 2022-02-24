package app.plantdiary

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.plantdiary.dto.Plant
import app.plantdiary.dto.Specimen
import app.plantdiary.service.IPlantService
import app.plantdiary.service.PlantService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.launch

class MainViewModel(var plantService : IPlantService =  PlantService()) : ViewModel() {

    var plants : MutableLiveData<List<Plant>> = MutableLiveData<List<Plant>>()
    var specimens: MutableLiveData<List<Specimen>> = MutableLiveData<List<Specimen>>()

    private lateinit var firestore : FirebaseFirestore

    init {
        firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        listenToSpecimens()
    }

    private fun listenToSpecimens() {
        firestore.collection("specimens").addSnapshotListener {
            snapshot, e ->
            // handle the error if there is one, and then return
            if (e != null) {
                Log.w("Listen failed", e)
                return@addSnapshotListener
            }
            // if we reached this point , there was not an error
            snapshot?.let {
                val allSpecimens = ArrayList<Specimen>()
                val documents = snapshot.documents
                documents.forEach {
                    val specimen = it.toObject(Specimen::class.java)
                    specimen?.let {
                        allSpecimens.add(it)
                    }
                }
                specimens.value = allSpecimens
            }
        }

    }

    fun fetchPlants() {
        viewModelScope.launch {
            var innerPlants = plantService.fetchPlants()
            plants.postValue(innerPlants)
        }

    }

    fun save(specimen: Specimen) {
        val document = if (specimen.specimenId == null || specimen.specimenId.isEmpty()) {
            // create a new specimen
            firestore.collection("specimens").document()
        } else {
            // update an existing specimen.
            firestore.collection("specimens").document(specimen.specimenId)
        }
        specimen.specimenId = document.id
        val handle = document.set(specimen)
        handle.addOnSuccessListener { Log.d("Firebase", "Document Saved") }
        handle.addOnFailureListener { Log.e("Firebase", "Save failed $it ")}
    }
}