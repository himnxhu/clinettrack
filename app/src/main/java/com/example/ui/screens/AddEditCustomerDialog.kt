package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Customer
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomerDialog(
    customer: Customer? = null, // if null, in "Add" mode, otherwise "Edit" mode
    defaultFee: Double = 1500.0,
    defaultBusinessType: String = "Gym / Fitness",
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        phone: String,
        email: String,
        businessType: String,
        feePlanName: String,
        feeAmount: Double,
        notes: String,
        nextDueDateMillis: Long
    ) -> Unit
) {
    val context = LocalContext.current
    val isEditMode = customer != null

    var name by remember { mutableStateOf(customer?.name ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }
    var email by remember { mutableStateOf(customer?.email ?: "") }
    var notes by remember { mutableStateOf(customer?.notes ?: "") }
    var feeAmountStr by remember { mutableStateOf(customer?.feeAmount?.toInt()?.toString() ?: defaultFee.toInt().toString()) }

    val businessTypes = listOf(
        "Gym / Fitness",
        "Dance Academy",
        "Coaching / Classes",
        "Beauty Salon",
        "Sports Club",
        "Yoga / Wellness",
        "Clinic",
        "Music School",
        "Other"
    )
    var selectedBusinessType by remember { mutableStateOf(customer?.businessType ?: defaultBusinessType) }

    val planOptions = listOf("Monthly", "Quarterly", "Annually", "Custom")
    var selectedPlan by remember { mutableStateOf(customer?.feePlanName ?: planOptions[0]) }

    // Date calculations
    val calendar = remember {
        Calendar.getInstance().apply {
            if (isEditMode && customer != null) {
                timeInMillis = customer.nextDueDateMillis
            } else {
                // Default next due date is 30 days from now
                add(Calendar.DAY_OF_YEAR, 30)
            }
        }
    }
    var nextDueDateMillis by remember { mutableStateOf(calendar.timeInMillis) }

    // Dropdown flags
    var businessTypeExpanded by remember { mutableStateOf(false) }
    var planExpanded by remember { mutableStateOf(false) }

    // Validation
    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditMode) "Edit Customer Profile" else "Onboard New Customer",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .testTag("add_customer_form_container"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    label = { Text("Customer Full Name") },
                    isError = nameError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_customer_name_input"),
                    supportingText = { if (nameError) Text("Name cannot be empty", color = MaterialTheme.colorScheme.error) }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Phone Field
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            phoneError = it.isBlank()
                        },
                        label = { Text("Phone Number") },
                        isError = phoneError,
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("add_customer_phone_input"),
                        supportingText = { if (phoneError) Text("Required", color = MaterialTheme.colorScheme.error) }
                    )

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email (Optional)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("add_customer_email_input")
                    )
                }

                // Business Category Selection Box
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedBusinessType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Business / Milestone Type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = businessTypeExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { businessTypeExpanded = true }
                            .testTag("add_customer_business_type_dropdown")
                    )
                    DropdownMenu(
                        expanded = businessTypeExpanded,
                        onDismissRequest = { businessTypeExpanded = false }
                    ) {
                        businessTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedBusinessType = type
                                    businessTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Fee Plan Cycle
                    Box(modifier = Modifier.weight(1.1f)) {
                        OutlinedTextField(
                            value = selectedPlan,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pricing Plan") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = planExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { planExpanded = true }
                                .testTag("add_customer_plan_dropdown")
                        )
                        DropdownMenu(
                            expanded = planExpanded,
                            onDismissRequest = { planExpanded = false }
                        ) {
                            planOptions.forEach { plan ->
                                DropdownMenuItem(
                                    text = { Text(plan) },
                                    onClick = {
                                        selectedPlan = plan
                                        planExpanded = false
                                        // Autofill standard fee suggestions depending on tier selection
                                        if (feeAmountStr == "999" || feeAmountStr == "" || feeAmountStr == "499" || feeAmountStr == "1999") {
                                            feeAmountStr = when (plan) {
                                                "Quarterly" -> "2499"
                                                "Annually" -> "8999"
                                                "Custom" -> "1500"
                                                else -> "999" // Monthly
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Fee Amount
                    OutlinedTextField(
                        value = feeAmountStr,
                        onValueChange = {
                            feeAmountStr = it
                            amountError = it.toDoubleOrNull() == null
                        },
                        label = { Text("Fee (INR)") },
                        isError = amountError,
                        singleLine = true,
                        modifier = Modifier.weight(0.9f).testTag("add_customer_fee_input"),
                        supportingText = { if (amountError) Text("Invalid number", color = MaterialTheme.colorScheme.error) }
                    )
                }

                // Next Due Date DatePicker Display
                OutlinedTextField(
                    value = formatMillis(nextDueDateMillis),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("First Dues Due Date") },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val datePicker = DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        calendar.set(Calendar.YEAR, year)
                                        calendar.set(Calendar.MONTH, month)
                                        calendar.set(Calendar.DAY_OF_MONTH, day)
                                        nextDueDateMillis = calendar.timeInMillis
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                )
                                datePicker.show()
                            },
                            modifier = Modifier.testTag("due_date_picker_button")
                        ) {
                            Icon(imageVector = Icons.Default.DateRange, contentDescription = "Select calendar due date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("add_customer_due_date_display")
                )

                // Notes input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Remarks (e.g. requested split bill)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("add_customer_notes_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalFee = feeAmountStr.toDoubleOrNull()
                    val formsValid = name.isNotBlank() && phone.isNotBlank() && finalFee != null

                    if (!formsValid) {
                        nameError = name.isBlank()
                        phoneError = phone.isBlank()
                        amountError = finalFee == null
                        return@Button
                    }

                    onConfirm(
                        name.trim(),
                        phone.trim(),
                        email.trim(),
                        selectedBusinessType,
                        selectedPlan,
                        finalFee ?: 0.0,
                        notes.trim(),
                        nextDueDateMillis
                    )
                },
                modifier = Modifier.testTag("save_customer_confirm_button")
            ) {
                Text("Confirm & Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_dialog_button")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
