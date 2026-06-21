package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AddBillDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, value: Double) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var name by remember { mutableStateOf("") }
    var valueStr by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            focusManager.clearFocus()
            onDismiss()
        },
        title = { Text(text = "Adicionar Conta Fixa") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Conta") },
                    placeholder = { Text("Ex: Academia, Aluguel...") },
                    modifier = Modifier.fillMaxWidth().testTag("bill_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = valueStr,
                    onValueChange = { valueStr = it },
                    label = { Text("Valor Mensal (R$)") },
                    placeholder = { Text("Ex: 150.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("bill_value_input"),
                    singleLine = true
                )

                if (showError) {
                    Text(
                        text = "Por favor, insira um nome e um valor válido maior que zero.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val valueParsed = valueStr.replace(",", ".").toDoubleOrNull()
                    if (name.isNotBlank() && valueParsed != null && valueParsed > 0) {
                        focusManager.clearFocus()
                        onConfirm(name.trim(), valueParsed)
                        onDismiss()
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier.testTag("bill_confirm_button")
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    focusManager.clearFocus()
                    onDismiss()
                }
            ) {
                Text("Cancelar")
            }
        }
    )
}
