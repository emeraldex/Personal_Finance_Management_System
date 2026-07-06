package com.example.expenseandroid.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.example.expenseandroid.viewmodel.SettingsViewModel
import com.example.expenseandroid.viewmodel.SettingsViewModelFactory

class SettingsScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SettingsScreen()
        }
    }
}

@Composable
fun SettingsScreen() {
    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory())
    val databasePath = remember { mutableStateOf("") }

    Scaffold {
        Column {
            Text("Database Path")
            TextField(value = databasePath.value, onValueChange = { databasePath.value = it })
            Button(onClick = {
                settingsViewModel.saveSettings(databasePath.value)
            }) {
                Text("Save")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SettingsScreen()
}
