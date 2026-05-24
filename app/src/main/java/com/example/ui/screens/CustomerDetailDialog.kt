package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Customer
import com.example.data.models.Milestone
import com.example.data.models.PaymentRecord
import com.example.ui.viewmodels.ClientTrackViewModel

@Composable
fun CustomerDetailDialog(
    customer: Customer,
    viewModel: ClientTrackViewModel,
    onDismiss: () -> Unit,
    onOpenEdit: () -> Unit
) {
    val payments by viewModel.selectedCustomerPayments.collectAsState(initial = emptyList())
    val milestones by viewModel.selectedCustomerMilestones.collectAsState(initial = emptyList())

    var showPaymentRecordBox by remember { mutableStateOf(false) }
    var collectAmountStr by remember { mutableStateOf(customer.feeAmount.toInt().toString()) }
    var collectNotes by remember { mutableStateOf("") }
    var collectError by remember { mutableStateOf(false) }

    var newMilestoneTitle by remember { mutableStateOf("") }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Customer?", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete ${customer.name}? This will remove all payment history and progress tracking permanently.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomer(customer)
                        showDeleteConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_customer_btn")
                ) {
                    Text("Delete Permanently", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = customer.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${customer.businessType} • ${customer.feePlanName} Plan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    IconButton(
                        onClick = onOpenEdit,
                        modifier = Modifier.testTag("detail_edit_profile_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Customer details")
                    }
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.testTag("detail_delete_profile_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete customer from system", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_detail_sheet_scroll"),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                // Info block
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ProfileInfoRow(label = "Primary Phone", value = customer.phone, icon = Icons.Default.Phone)
                        if (customer.email.isNotBlank()) {
                            ProfileInfoRow(label = "Email Address", value = customer.email, icon = Icons.Default.Email)
                        }
                        ProfileInfoRow(
                            label = "Onboarding Date",
                            value = formatMillis(customer.joinDateMillis),
                            icon = Icons.Default.AccountBox
                        )
                        ProfileInfoRow(
                            label = "Next renewal Date",
                            value = formatMillis(customer.nextDueDateMillis),
                            icon = Icons.Default.DateRange,
                            textColor = if (customer.nextDueDateMillis < System.currentTimeMillis()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        if (customer.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Special Notes:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = customer.notes, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Billing / Payment recorder block
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Fee Payments Ledger",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Button(
                                onClick = { showPaymentRecordBox = !showPaymentRecordBox },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("detail_collect_payment_toggle_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (showPaymentRecordBox) MaterialTheme.colorScheme.errorContainer
                                    else MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = if (showPaymentRecordBox) MaterialTheme.colorScheme.onErrorContainer
                                    else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(
                                        imageVector = if (showPaymentRecordBox) Icons.Default.Close else Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(if (showPaymentRecordBox) "Close" else "Record Fee", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = showPaymentRecordBox,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .testTag("record_payment_container"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Log a Received payment", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = collectAmountStr,
                                            onValueChange = {
                                                collectAmountStr = it
                                                collectError = it.toDoubleOrNull() == null
                                            },
                                            label = { Text("Amount Recv.") },
                                            isError = collectError,
                                            singleLine = true,
                                            modifier = Modifier.weight(1f).testTag("payment_collect_amount_input")
                                        )

                                        OutlinedTextField(
                                            value = collectNotes,
                                            onValueChange = { collectNotes = it },
                                            label = { Text("Receipt note/mode") },
                                            placeholder = { Text("Cash, GPay") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1.5f).testTag("payment_collect_notes_input")
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            val parsed = collectAmountStr.toDoubleOrNull()
                                            if (parsed == null) {
                                                collectError = true
                                                return@Button
                                            }
                                            viewModel.recordPayment(customer.id, parsed, collectNotes.trim())
                                            collectNotes = ""
                                            showPaymentRecordBox = false
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("payment_collect_confirm_btn"),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Record payment & Extend Due Date", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (payments.isEmpty()) {
                            Text(
                                text = "No payments recorded yet for this user. Record a fee using the button above.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                modifier = Modifier.padding(vertical = 4.dp),
                                lineHeight = 16.sp
                            )
                        } else {
                            payments.forEach { rec ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .testTag("ledger_history_item"),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "Received: ₹${rec.amount.toInt()}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        if (rec.notes.isNotBlank()) {
                                            Text(text = rec.notes, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Text(
                                        text = formatMillis(rec.paymentDateMillis),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            }
                        }
                    }
                }

                // Progress Milestone checklist block (THE CORE USER ASK)
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Customer Progress Milestones",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Fast input box to append extra milestones
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newMilestoneTitle,
                                onValueChange = { newMilestoneTitle = it },
                                label = { Text("Add custom target milestone") },
                                singleLine = true,
                                placeholder = { Text("E.g. Bench press progress assessment") },
                                modifier = Modifier.weight(1f).testTag("detail_add_milestone_input")
                            )

                            IconButton(
                                onClick = {
                                    if (newMilestoneTitle.isNotBlank()) {
                                        viewModel.addCustomMilestone(customer.id, newMilestoneTitle.trim())
                                        newMilestoneTitle = ""
                                    }
                                },
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                    .size(40.dp)
                                    .testTag("detail_add_milestone_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add custom milestone",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (milestones.isEmpty()) {
                            Text(
                                "No milestones defined.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            milestones.forEach { m ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .testTag("milestone_row_item_${m.id}"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = m.isCompleted,
                                        onCheckedChange = { viewModel.toggleMilestoneSelection(m) },
                                        modifier = Modifier.testTag("milestone_checkbox_${m.id}")
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = m.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            textDecoration = if (m.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                            color = if (m.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            else MaterialTheme.colorScheme.onSurface
                                        )

                                        if (m.isCompleted && m.completedDateMillis > 0L) {
                                            Text(
                                                text = "Achieved on: ${formatMillis(m.completedDateMillis)}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF2E7D32),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteMilestone(m) },
                                        modifier = Modifier.testTag("delete_milestone_btn_${m.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete milestone",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.testTag("detail_dialog_close_btn")
            ) {
                Text("Close Panel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun ProfileInfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth() // safe space
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$label:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
