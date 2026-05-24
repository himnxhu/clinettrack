package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Customer
import com.example.data.models.PaymentRecord
import com.example.ui.viewmodels.ClientTrackViewModel

@Composable
fun ReportsTab(
    viewModel: ClientTrackViewModel,
    modifier: Modifier = Modifier
) {
    val customers by viewModel.customers.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val overdueCustomers by viewModel.overdueCustomers.collectAsState()
    val businessName by viewModel.businessName.collectAsState()
    val totalRevenue by viewModel.totalRevenue.collectAsState()
    val context = LocalContext.current

    // Calculated metrics
    val totalClientsCount = customers.size
    val overdueClientsCount = overdueCustomers.size
    val paidClientsCount = Math.max(0, totalClientsCount - overdueClientsCount)

    val collectionSuccessRate = if (totalClientsCount > 0) {
        (paidClientsCount.toFloat() / totalClientsCount) * 100
    } else {
        100f
    }

    val estimatedMonthlyReceivables = customers.sumOf { it.feeAmount }

    // Receipt generation selections
    var selectedPaymentForReceipt by remember { mutableStateOf<PaymentRecord?>(null) }
    var showReceiptDropdown by remember { mutableStateOf(false) }

    // Auto-select latest payment if none selected
    LaunchedEffect(payments) {
        if (selectedPaymentForReceipt == null && payments.isNotEmpty()) {
            selectedPaymentForReceipt = payments.firstOrNull()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_tab_root"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Analytics Summary Title
        item {
            Text(
                text = "Performance Metrics & Analytics",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Stats Box
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("collection_rates_analytics_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Collection Efficiency Rate",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${collectionSuccessRate.toInt()}%",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (collectionSuccessRate > 80f) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "$paidClientsCount of $totalClientsCount Accounts Clear",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { collectionSuccessRate / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .testTag("collection_efficiency_ratio_progress_bar"),
                        color = if (collectionSuccessRate > 80f) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Recorded Earnings", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${totalRevenue.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Est. Active Contract Value", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${estimatedMonthlyReceivables.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Receipt mock generation block
        item {
            Text(
                text = "Auto-Invoice Receipts Generator",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (payments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("empty_receipts_placeholder"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No recorded payments found.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Log payments inside individual customer profiles to auto-generate PDF receipt formats.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Receipt dropdown layout
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    val clientNamePaying = customers.find { it.id == (selectedPaymentForReceipt?.customerId ?: -1) }?.name ?: "Unknown"

                    OutlinedTextField(
                        value = "Payment: ₹${selectedPaymentForReceipt?.amount?.toInt() ?: 0} by $clientNamePaying",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select logged payment transaction") },
                        trailingIcon = {
                            IconButton(onClick = { showReceiptDropdown = true }) {
                                Icon(imageVector = Icons.Default.DateRange, contentDescription = "Choose records")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showReceiptDropdown = true }
                            .testTag("receipt_payment_select_box")
                    )

                    DropdownMenu(
                        expanded = showReceiptDropdown,
                        onDismissRequest = { showReceiptDropdown = false }
                    ) {
                        payments.forEach { rec ->
                            val clientName = customers.find { it.id == rec.customerId }?.name ?: "Unknown Client"
                            DropdownMenuItem(
                                text = { Text("₹${rec.amount.toInt()} - $clientName", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                onClick = {
                                    selectedPaymentForReceipt = rec
                                    showReceiptDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Generated Receipt Preview card
            item {
                selectedPaymentForReceipt?.let { py ->
                    val customerObject = customers.find { it.id == py.customerId }
                    val clientName = customerObject?.name ?: "Unknown"
                    val clientPhone = customerObject?.phone ?: "Not Given"
                    val planUsed = customerObject?.feePlanName ?: "Contract Plan"

                    val receiptId = "TXN-${py.id.toString().padStart(6, '0')}"
                    val dateFormatted = formatMillis(py.paymentDateMillis)

                    val fullReceiptText = """
                        ==============================
                              $businessName RECEIPT    
                        ==============================
                        Receipt ID : $receiptId
                        Date       : $dateFormatted
                        client     : $clientName
                        PhoneNo    : $clientPhone
                        ------------------------------
                        Description : subscription Fee
                        Plan period : $planUsed
                        Amount Paid : INR ${py.amount.toInt()}
                        Ledger Note : ${if (py.notes.isBlank()) "Standard checkout" else py.notes}
                        Status      : PAID VERIFIED ✔
                        ==============================
                        Thank you for your business!
                    """.trimIndent()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_receipt_preview_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "E-Receipt Preview:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, fullReceiptText)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Share Transaction Receipts"))
                                    },
                                    modifier = Modifier.testTag("share_invoice_receipt_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share text invoice receipt description",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = fullReceiptText,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color.DarkGray,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
