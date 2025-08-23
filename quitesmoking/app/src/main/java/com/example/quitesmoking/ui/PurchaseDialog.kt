package com.example.quitesmoking.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.quitesmoking.model.Purchase

@Composable
fun PurchaseDialog(
    lastBrand: String?,
    lastPrice: Double?,
    onDismiss: () -> Unit,
    onSave: (Purchase) -> Unit
) {
    var brand by remember { mutableStateOf(lastBrand ?: "") }
    var unitsText by remember { mutableStateOf("1") }
    var priceText by remember { mutableStateOf(lastPrice?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                enabled = brand.isNotBlank()
                        && unitsText.toIntOrNull() != null
                        && priceText.toDoubleOrNull() != null,
                onClick = {
                    onSave(
                        Purchase(
                            brand = brand,
                            units = unitsText.toInt(),
                            price = priceText.toDouble()
                        )
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Log purchase you avoided") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand / SKU") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = unitsText,
                    onValueChange = { unitsText = it },
                    label = { Text("Units") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Total price ($)") },
                    singleLine = true
                )
            }
        }
    )
}
