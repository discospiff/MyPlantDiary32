package app.plantdiary

import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.PopupProperties
import app.plantdiary.dto.Plant
import app.plantdiary.dto.Specimen
import app.plantdiary.ui.theme.MyPlantDiaryTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity() : ComponentActivity() {

    private var selectedPlant: Plant? = null
    private val viewModel: MainViewModel by viewModel<MainViewModel>()
    private var inPlantName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            viewModel.fetchPlants()
            val plants by viewModel.plants.observeAsState(initial = emptyList())
            MyPlantDiaryTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    color = MaterialTheme.colors.background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SpecimenFacts("Android", plants)
                }
            }
        }
    }


    @Composable
    fun TextFieldWithDropdownUsage(dataIn: List<Plant>, label : String = "", take :Int = 3) {

        val dropDownOptions = remember { mutableStateOf(listOf<Plant>()) }
        val textFieldValue = remember { mutableStateOf(TextFieldValue()) }
        val dropDownExpanded = remember { mutableStateOf(false) }

        fun onDropdownDismissRequest() {
            dropDownExpanded.value = false
        }

        fun onValueChanged(value: TextFieldValue) {
            inPlantName = value.text
            dropDownExpanded.value = true
            textFieldValue.value = value
            dropDownOptions.value = dataIn.filter {
                it.toString().startsWith(value.text) && it.toString() != value.text
            }.take(take)
        }

        TextFieldWithDropdown(
            modifier = Modifier.fillMaxWidth(),
            value = textFieldValue.value,
            setValue = ::onValueChanged,
            onDismissRequest = ::onDropdownDismissRequest,
            dropDownExpanded = dropDownExpanded.value,
            list = dropDownOptions.value,
            label = label
        )
    }

    @Composable
    fun TextFieldWithDropdown(
        modifier: Modifier = Modifier,
        value: TextFieldValue,
        setValue: (TextFieldValue) -> Unit,
        onDismissRequest: () -> Unit,
        dropDownExpanded: Boolean,
        list: List<Plant>,
        label: String = ""
    ) {
        Box(modifier) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused)
                            onDismissRequest()
                    },
                value = value,
                onValueChange = setValue,
                label = { Text(label) },
                colors = TextFieldDefaults.outlinedTextFieldColors()
            )
            DropdownMenu(
                expanded = dropDownExpanded,
                properties = PopupProperties(
                    focusable = false,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                ),
                onDismissRequest = onDismissRequest
            ) {
                list.forEach { text ->
                    DropdownMenuItem(onClick = {
                        setValue(
                            TextFieldValue(
                                text.toString(),
                                TextRange(text.toString().length)
                            )
                        )
                        selectedPlant = text
                    }) {
                        Text(text = text.toString())
                    }
                }
            }
        }
    }

    @Composable
    fun SpecimenFacts(name: String, plants : List<Plant> = ArrayList<Plant>()) {
        var inLocation by remember { mutableStateOf("") }
        var inDescription by remember { mutableStateOf("") }
        var inDatePlanted by remember { mutableStateOf("") }
        val context = LocalContext.current
        Column {
            TextFieldWithDropdownUsage(dataIn = plants, stringResource(R.string.plantName))
            OutlinedTextField(
                value = inLocation,
                onValueChange = { inLocation = it },
                label = { Text(stringResource(R.string.location)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = inDescription,
                onValueChange = { inDescription = it },
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = inDatePlanted,
                onValueChange = { inDatePlanted = it },
                label = { Text(stringResource(R.string.datePlanted)) },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    var specimen = Specimen().apply {
                        plantName = inPlantName
                        plantId = selectedPlant?.let {
                            it.id
                        } ?: 0
                        location = inLocation
                        description = inDescription
                        datePlanted = inDatePlanted
                    }
                    viewModel.save(specimen)
                    Toast.makeText(
                        context,
                        "$inPlantName $inLocation $inDescription $inDatePlanted",
                        Toast.LENGTH_LONG
                    ).show()
                }
            ) {
                Text(text = "Save")
            }
        }
    }

    @Preview(name = "Light Mode", showBackground = true)
    @Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode")
    @Composable
    fun DefaultPreview() {
        MyPlantDiaryTheme {
            // A surface container using the 'background' color from the theme
            Surface(
                color = MaterialTheme.colors.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                SpecimenFacts("Android")
            }
        }
    }
}