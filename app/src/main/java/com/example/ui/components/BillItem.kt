package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Bill
import java.util.*

@Composable
fun BillItem(
    bill: Bill,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit,
    onUpdateValue: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var editValueStr by remember { mutableStateOf(bill.value.toString()) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (bill.isActive) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (bill.isActive) 2.dp else 0.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("bill_item_${bill.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Checkbox and Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = bill.isActive,
                    onCheckedChange = { onToggleActive() },
                    modifier = Modifier.testTag("bill_checkbox_${bill.id}")
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = bill.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (bill.isActive) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    },
                    textDecoration = if (bill.isActive) null else TextDecoration.LineThrough,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Price Value representation / Interactive text field if editing
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                if (isEditing) {
                    OutlinedTextField(
                        value = editValueStr,
                        onValueChange = { editValueStr = it },
                        modifier = Modifier
                            .width(90.dp)
                            .height(55.dp)
                            .padding(end = 4.dp)
                            .testTag("bill_edit_input_${bill.id}"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    IconButton(
                        onClick = {
                            val doubleVal = editValueStr.replace(",", ".").toDoubleOrNull()
                            if (doubleVal != null && doubleVal >= 0) {
                                onUpdateValue(doubleVal)
                                isEditing = false
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("bill_save_button_${bill.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Salvar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Text(
                        text = String.format(Locale("pt", "BR"), "R$ %.2f", bill.value),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (bill.isActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        }
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = {
                            editValueStr = bill.value.toString()
                            isEditing = true
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar valor",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { onDelete() },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("bill_delete_button_${bill.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir conta",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
