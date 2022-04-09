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

class MainViewModel(var plantService : IPlantService) : ViewModel() {

    val photos: ArrayList<Photo> by mutableStateOf(ArrayList<Photo>())
    internal val NEW_SPECIMEN = "New Specimen"
    var plants = plantService.getLocalPlantDAO().getAllPlants()
    var specimens: MutableLiveData<List<Specimen>> = MutableLiveData<List<Specimen>>()
    var selectedSpecimen by mutableStateOf(Specimen())
    var user : User? = null
    val eventPhotos : MutableLiveData<List<Photo>> = MutableLiveData<List<Photo>>()

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
             plantService.fetchPlants()

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

    internal fun updatePhotoDatabase(photo: Photo) {
        user?.let {
            user ->
            var photoDocument = if (photo.id.isEmpty()) {
                firestore.collection("users").document(user.uid).collection("specimens").document(selectedSpecimen.specimenId).collection("photos").document()
            } else {
                firestore.collection("users").document(user.uid).collection("specimens").document(selectedSpecimen.specimenId).collection("photos").document(photo.id)

            }
            photo.id = photoDocument.id
            var handle = photoDocument.set(photo)
            handle.addOnSuccessListener {
                Log.i(TAG, "Successfully updated photo metadata")
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

    fun fetchPhotos() {
        user?.let {
                user ->
            var photoCollection = firestore.collection("users").document(user.uid).collection("specimens").document(selectedSpecimen.specimenId).collection("photos")
            var photosListener = photoCollection.addSnapshotListener {
                    querySnapshot, firebaseFirestoreException ->
                querySnapshot?.let {
                        querySnapshot ->
                    var documents = querySnapshot.documents
                    var inPhotos = ArrayList<Photo>()
                    documents?.forEach {
                        var photo = it.toObject(Photo::class.java)
                        photo?.let {
                            inPhotos.add(it)
                        }
                    }
                    eventPhotos.value = inPhotos
                }
            }
        }
    }

    fun delete(photo: Photo) {
        user?.let {
            user ->
            var photoCollection = firestore.collection("users").document(user.uid).collection("specimens").document(selectedSpecimen.specimenId).collection("photos")
            photoCollection.document(photo.id).delete()
            val uri = Uri.parse(photo.localUri)
            val imageRef = storageReference.child("images/${user.uid}/${uri.lastPathSegment}")
            imageRef.delete()
                .addOnSuccessListener {
                    Log.i(TAG, "Photo binary file deleted ${photo}")
                }
                .addOnFailureListener {
                    Log.e(TAG, "Photo delete failed.  ${it.message}")
                }
        }
    }
}