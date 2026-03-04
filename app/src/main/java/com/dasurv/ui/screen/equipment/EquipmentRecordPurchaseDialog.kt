package com.dasurv.ui.screen.equipment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dasurv.data.local.entity.Equipment
import com.dasurv.ui.component.*
import com.dasurv.util.FMT_DATE
import com.dasurv.util.showDatePicker
import java.text.SimpleDateFormat
import java.util.*

@Composable
internal fun EquipmentRecordPurchaseDialog(
    equipmentList: List<Equipment>,
    purchaseSources: List<String>,
    sellers: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (equipmentId: Long, quantity: Int, totalCost: Double, purchaseDate: Long, notes: String, purchaseSource: String, seller: String) -> Unit
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat(FMT_DATE, Locale.getDefault()) }

    var selectedEquipmentName by remember { mutableStateOf("") }
    val selectedEquipment = remember(selectedEquipmentName, equipmentList) {
        equipmentList.find { it.name == selectedEquipmentName }
    }
    var quantity by remember { mutableIntStateOf(1) }
    var totalCost by remember { mutableStateOf("") }
    var purchaseDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var notes by remember { mutableStateOf("") }
    var purchaseSource by remember { mutableStateOf("") }
    var seller by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = M3DialogSurfaceBg,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = M3CyanContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ShoppingCart, null, tint = M3CyanColor, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Record Purchase",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = M3OnSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close", tint = M3OnSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Equipment dropdown
                    val equipmentNames = remember(equipmentList) {
                        equipmentList.filter { it.category != "pigment" }.map { it.name }
                    }
                    DasurvDropdownField(
                        value = selectedEquipmentName,
                        label = "Equipment",
                        options = equipmentNames,
                        onOptionSelected = { selectedEquipmentName = it },
                        placeholder = "Select equipment..."
                    )

                    // Quantity
                    Column {
                        Text("Quantity", style = M3LabelStyle)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            DasurvQuantityStepper(
                                value = quantity,
                                onValueChange = { quantity = it },
                                minValue = 1,
                                accentColor = M3CyanColor
                            )
                        }
                    }

                    // Total cost
                    DasurvCurrencyField(
                        value = totalCost,
                        onValueChange = { totalCost = it },
                        label = "Total Cost"
                    )

                    // Purchase date
                    DasurvClickableField(
                        label = "Purchase Date",
                        value = dateFormat.format(Date(purchaseDate)),
                        onClick = {
                            showDatePicker(context, purchaseDate) { purchaseDate = it }
                        }
                    )

                    // Purchase source
                    DasurvAutoCompleteField(
                        value = purchaseSource,
                        onValueChange = { purchaseSource = it },
                        label = "Purchased From (App/Platform)",
                        suggestions = purchaseSources,
                        placeholder = "e.g. Shopee, Lazada"
                    )

                    // Seller
                    DasurvAutoCompleteField(
                        value = seller,
                        onValueChange = { seller = it },
                        label = "Seller",
                        suggestions = sellers,
                        placeholder = "e.g. Store name"
                    )

                    // Notes
                    DasurvTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = "Notes (optional)",
                        singleLine = false,
                        minLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = M3FieldBg,
                            contentColor = M3OnSurfaceVariant
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Medium)
                    }
                    Button(
                        onClick = {
                            val eq = selectedEquipment ?: return@Button
                            val cost = totalCost.toDoubleOrNull() ?: 0.0
                            onConfirm(eq.id, quantity, cost, purchaseDate, notes.trim(), purchaseSource.trim(), seller.trim())
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50),
                        enabled = selectedEquipment != null && quantity > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = M3CyanColor,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text("Record", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
