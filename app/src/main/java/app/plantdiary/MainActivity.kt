package app.plantdiary

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.plantdiary.ui.theme.MyPlantDiaryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyPlantDiaryTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    SpecimenFacts("Android")
                }
            }
        }
    }
}

@Composable
fun SpecimenFacts(name: String) {
    var plantName by remember { mutableStateOf("")}
    var location by remember { mutableStateOf("")}
    var description by remember { mutableStateOf("")}
    var datePlanted by remember { mutableStateOf("")}
    val context = LocalContext.current
    Column {
        OutlinedTextField(
            value = plantName,
            onValueChange = {plantName = it},
            label = { Text(stringResource(R.string.plantName))}
            )
        OutlinedTextField(
            value = location,
            onValueChange = {location = it},
            label = { Text(stringResource(R.string.location))}
        )
        OutlinedTextField(
            value = description,
            onValueChange = {description = it},
            label = { Text(stringResource(R.string.description))}
        )
        OutlinedTextField(
            value = datePlanted,
            onValueChange = {datePlanted = it},
            label = { Text(stringResource(R.string.datePlanted))}
        )
        Button (
            onClick = {
                Toast.makeText(context, "$plantName $location $description $datePlanted", Toast.LENGTH_LONG).show()
            }
        ){
            Text(text = "Save")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyPlantDiaryTheme {
        SpecimenFacts("Android")
    }
}