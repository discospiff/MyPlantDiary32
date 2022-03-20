package app.plantdiary

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.plantdiary.dto.Photo
import app.plantdiary.dto.Plant
import app.plantdiary.dto.Specimen
import app.plantdiary.dto.User
import app.plantdiary.service.IPlantService
import app.plantdiary.service.PlantService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class MainViewModel(var plantService : IPlantService =  PlantService()) : ViewModel() {

    val photos: ArrayList<Photo> = ArrayList<Photo>()
    internal val NEW_SPECIMEN = "New Specimen"
    var plants : MutableLiveData<List<Plant>> = MutableLiveData<List<Plant>>()
    var specimens: MutableLiveData<List<Specimen>> = MutableLiveData<List<Specimen>>()
    var selectedSpecimen by mutableStateOf(Specimen())
    var user : User? = null

    private lateinit var firestore : FirebaseFirestore
    private var storageReference = FirebaseStorage.getInstance().getReference()

    init {
        firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
    }

    fun listenToSpecimens() {
        user?.let {
            user ->
            firestore.collection("users").document(user.uid).collection("specimens").addSnapshotListener {
                snapshot, e ->
                // handle the error if there is one, and then return
                if (e != null) {
                    Log.w("Listen failed", e)
                    return@addSnapshotListener
                }
                // if we reached this point , there was not an error
                snapshot?.let {
                    val allSpecimens = ArrayList<Specimen>()
                    allSpecimens.add(Specimen(plantName = NEW_SPECIMEN))
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

    }

    fun fetchPlants() {
        viewModelScope.launch {
            var innerPlants = plantService.fetchPlants()
            plants.postValue(innerPlants)
        }

    }

    fun saveSpecimen() {
        user?.let {
            user ->
            val document =
                if (selectedSpecimen.specimenId == null || selectedSpecimen.specimenId.isEmpty()) {
                    // create a new specimen
                    firestore.collection("users").document(user.uid).collection("specimens").document()
                } else {
                    // update an existing specimen.
                    firestore.collection("users").document(user.uid).collection("specimens").document(selectedSpecimen.specimenId)
                }
            selectedSpecimen.specimenId = document.id
            val handle = document.set(selectedSpecimen)
            handle.addOnSuccessListener {
                Log.d("Firebase", "Document Saved")
                if (photos.isNotEmpty()) {
                    uploadPhotos()
                }
            }
            handle.addOnFailureListener { Log.e("Firebase", "Save failed $it ") }
        }
    }

    private fun uploadPhotos() {
        photos.forEach {
            photo ->
            var uri = Uri.parse(photo.localUri)
            val imageRef = storageReference.child("images/${user?.uid}/${uri.lastPathSegment}")
            val uploadTask  = imageRef.putFile(uri)
            uploadTask.addOnSuccessListener {
                Log.i(TAG, "Image Uploaded $imageRef")
                val downloadUrl = imageRef.downloadUrl
                downloadUrl.addOnSuccessListener {
                    remoteUri ->
                    photo.remoteUri = remoteUri.toString()
                    updatePhotoDatabase(photo)

                }
            }
            uploadTask.addOnFailureListener {
                Log.e(TAG, it.message ?: "No message")
            }
        }
    }

    private fun updatePhotoDatabase(photo: Photo) {
        user?.let {
            user ->
            var photoCollection = firestore.collection("users").document(user.uid).collection("specimens").document(selectedSpecimen.specimenId).collection("photos")
            var handle = photoCollection.add(photo)
            handle.addOnSuccessListener {
                Log.i(TAG, "Successfully updated photo metadata")
                photo.id = it.id
                firestore.collection("users").document(user.uid).collection("specimens").document(selectedSpecimen.specimenId).collection("photos").document(photo.id).set(photo)
            }
            handle.addOnFailureListener {
                Log.e(TAG, "Error updating photo data: ${it.message}")
            }
        }
    }


    fun saveUser () {
        user?.let {
            user ->
            val handle = firestore.collection("users").document(user.uid).set(user)
            handle.addOnSuccessListener { Log.d("Firebase", "Document Saved") }
            handle.addOnFailureListener { Log.e("Firebase", "Save failed $it ") }
        }
    }
}