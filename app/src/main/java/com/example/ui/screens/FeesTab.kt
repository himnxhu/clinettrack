package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Customer
import com.example.ui.viewmodels.ClientTrackViewModel

@Composable
fun FeesTab(
    viewModel: ClientTrackViewModel,
    modifier: Modifier = Modifier
) {
    val overdueCustomers by viewModel.overdueCustomers.collectAsState()
    val upcomingCustomers by viewModel.upcomingCustomers.collectAsState()
    val businessName by viewModel.businessName.collectAsState()
    val context = LocalContext.current

    var selectedTemplateType by remember { mutableStateOf("WhatsApp") } // WhatsApp, SMS, Email
    var showBulkHelpDialog by remember { mutableStateOf(false) }

    if (showBulkHelpDialog) {
        AlertDialog(
            onDismissRequest = { showBulkHelpDialog = false },
            title = { Text("Bulk Reminder Help", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "You can trigger native Android share prompts for individual clients sequentially, " +
                            "which copies the selected prefilled '$selectedTemplateType' standard fee template text, " +
                            "allowing you to instantly paste into messaging apps easily."
                )
            },
            confirmButton = {
                Button(onClick = { showBulkHelpDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("fees_tab_root"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overdue template customizable draft visual card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reminders_template_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Customize Reminder Templates",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Messages are automatically generated with customer-specific pending fees",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row of template categories
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("WhatsApp", "Standard SMS", "Formal Email").forEach { option ->
                            val isSelected = selectedTemplateType == option
                            Button(
                                onClick = { selectedTemplateType = option },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .testTag("template_btn_$option"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(option, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulated live text template preview card
                    val sampleName = "Rahul Sharma"
                    val sampleFee = "999"
                    val samplePlan = "Monthly Plan"
                    val sampleDueDate = "20 May 2026"

                    val messagePreviewText = when (selectedTemplateType) {
                        "WhatsApp" -> "Hello *$sampleName*! 🔔\n\nThis is a friendly reminder that your payment of *₹$sampleFee* for plan '*$samplePlan*' was due on *$sampleDueDate*. Please settle this as soon as possible. Thank you!\n\n- *$businessName*"
                        "Standard SMS" -> "Dear $sampleName, payment of Rs. $sampleFee on your $samplePlan is pending since $sampleDueDate. Please settle immediately. Thank you - $businessName."
                        else -> "Subject: Pending Invoice Notification: $businessName\n\nDear $sampleName,\n\nWe hope this message finds you well. This is a notification regarding the invoice rate balance of ₹$sampleFee due on $sampleDueDate. Your account is currently overdue. Let us know if you have questions.\n\nSincerely,\n$businessName"
                    }

                    Text(
                        text = "Template Live Preview:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = messagePreviewText,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }

        // Bulk reminders launch header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Clients with Outstanding Dues",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Select a user to dispatch individual drafts",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (overdueCustomers.isNotEmpty()) {
                    FilledTonalButton(
                        onClick = { showBulkHelpDialog = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("bulk_how_to_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bulk Info", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Overdue List Empty State
        if (overdueCustomers.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "No dues icon",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "All accounts are paid up!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "There are no overdue accounts currently requiring payment drafts.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            items(overdueCustomers) { customer ->
                OverdueReminderCard(
                    customer = customer,
                    templateType = selectedTemplateType,
                    businessName = businessName,
                    onRemindClick = {
                        val finalMsg = when (selectedTemplateType) {
                            "WhatsApp" -> "Hello *${customer.name}*! 🔔\n\nThis is a friendly reminder that your payment of *₹${customer.feeAmount.toInt()}* for plan '*${customer.feePlanName}*' was due on *${formatMillis(customer.nextDueDateMillis)}*. Please settle this as soon as possible. Thank you!\n\n- *$businessName*"
                            "Standard SMS" -> "Dear ${customer.name}, payment of Rs. ${customer.feeAmount.toInt()} on your ${customer.feePlanName} is pending since ${formatMillis(customer.nextDueDateMillis)}. Please settle immediately. Thank you - $businessName."
                            else -> "Subject: Pending Invoice Notification: $businessName\n\nDear ${customer.name},\n\nWe hope this message finds you well. This is a notification regarding the invoice rate balance of ₹${customer.feeAmount.toInt()} due on ${formatMillis(customer.nextDueDateMillis)}. Your account is currently overdue. Let us know if you have questions.\n\nSincerely,\n$businessName"
                        }

                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, finalMsg)
                        }
                        context.startActivity(Intent.createChooser(intent, "Remind ${customer.name}"))
                    }
                )
            }
        }

        // Upcoming due reminder header
        if (upcomingCustomers.isNotEmpty()) {
            item {
                Text(
                    text = "Upcoming Dues (Next 7 Days)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(upcomingCustomers) { customer ->
                UpcomingDueCard(
                    customer = customer,
                    onExtendClick = {
                        // Quick pay option to record standard extension in 1-click
                        viewModel.recordPayment(
                            customer.id, 
                            customer.feeAmount, 
                            "Quick renew via upcoming dashboard"
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun OverdueReminderCard(
    customer: Customer,
    templateType: String,
    businessName: String,
    onRemindClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("overdue_reminder_card_${customer.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Pending: ₹${customer.feeAmount.toInt()}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Plan: ${customer.feePlanName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Was due: ${formatMillis(customer.nextDueDateMillis)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onRemindClick,
                modifier = Modifier
                    .testTag("send_individual_reminder_btn_${customer.id}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE65100), // Rich orange alerting tone
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                    Text("Remind", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun UpcomingDueCard(
    customer: Customer,
    onExtendClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("upcoming_due_card_${customer.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Due ₹${customer.feeAmount.toInt()} in ${formatMillis(customer.nextDueDateMillis)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            TextButton(
                onClick = onExtendClick,
                modifier = Modifier.testTag("quick_extend_due_btn_${customer.id}")
            ) {
                Text("Quick Pay", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
