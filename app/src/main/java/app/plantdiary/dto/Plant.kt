package app.plantdiary.dto

import com.google.gson.annotations.SerializedName

data class Plant(@SerializedName("genus") var genus : String, var species : String, var common : String, var cultivar:String ="", var id : Int = 0 ) {
    override fun toString(): String {
        return common
    }
}