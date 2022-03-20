package app.plantdiary.dto

import java.util.*

data class Photo(var localUri: String = "" , var remoteUri : String = "", var description: String = "", var dateTaken : Date = Date(), var id: String = "") {
}