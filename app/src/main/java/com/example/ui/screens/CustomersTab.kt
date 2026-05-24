package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Customer
import com.example.ui.viewmodels.ClientTrackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersTab(
    viewModel: ClientTrackViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val activeFilterType by viewModel.businessTypeFilter.collectAsState()
    val filteredCustomers by viewModel.filteredCustomers.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Categories available
    val categories = listOf("All", "Gym / Fitness", "Coaching / Classes", "Beauty / Academy")

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("customers_tab_root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Search by name, phone, email...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search bar icon") },
                maxLines = 1,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_search_bar")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Tabs Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("category_filter_row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = activeFilterType == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.businessTypeFilter.value = category },
                        label = { Text(category, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.testTag("filter_chip_$category")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Results List
            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "No Customers Found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Try refining your search text or switching business filters."
                            else "Add your first custom customer record using the + button below to track recurring fees!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("customer_profiles_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCustomers) { customer ->
                        CustomerProfileCard(
                            customer = customer,
                            viewModel = viewModel,
                            onClick = { viewModel.selectCustomer(customer) }
                        )
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .navigationBarsPadding() // prevent navigation overlay
                .testTag("onboard_customer_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Onboard a new customer record")
        }

        // Popups triggers
        if (showAddDialog) {
            val configFee by viewModel.defaultFee.collectAsState()
            val configType by viewModel.businessType.collectAsState()
            AddEditCustomerDialog(
                customer = null,
                defaultFee = configFee,
                defaultBusinessType = configType,
                onDismiss = { showAddDialog = false },
                onConfirm = { name, phone, email, type, plan, fee, notes, due ->
                    viewModel.addCustomer(name, phone, email, System.currentTimeMillis(), type, plan, fee, notes, due)
                    showAddDialog = false
                }
            )
        }

        if (showEditDialog && selectedCustomer != null) {
            AddEditCustomerDialog(
                customer = selectedCustomer,
                onDismiss = { showEditDialog = false },
                onConfirm = { name, phone, email, type, plan, fee, notes, due ->
                    viewModel.updateCustomerDetails(selectedCustomer!!.id, name, phone, email, type, plan, fee, notes, due)
                    showEditDialog = false
                }
            )
        }

        // Customer details sheet modal view
        if (selectedCustomer != null && !showEditDialog) {
            CustomerDetailDialog(
                customer = selectedCustomer!!,
                viewModel = viewModel,
                onDismiss = { viewModel.selectCustomer(null) },
                onOpenEdit = { showEditDialog = true }
            )
        }
    }
}

@Composable
fun CustomerProfileCard(
    customer: Customer,
    viewModel: ClientTrackViewModel,
    onClick: () -> Unit
) {
    val isOverdue = customer.nextDueDateMillis < System.currentTimeMillis()
    val milestones by viewModel.getMilestonesForCustomer(customer.id).collectAsState(initial = emptyList())

    val totalMilestonesCount = milestones.size
    val completedMilestonesCount = milestones.count { it.isCompleted }
    val progressPercent = if (totalMilestonesCount > 0) {
        completedMilestonesCount.toFloat() / totalMilestonesCount
    } else {
        0f
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("customer_card_${customer.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Initial Letter Avatar circle outline
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isOverdue) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = customer.name.firstOrNull()?.toString()?.uppercase() ?: "?",
                        fontWeight = FontWeight.Bold,
                        color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = customer.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // Overdue / Paid pill state indicator
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isOverdue) MaterialTheme.colorScheme.errorContainer
                                    else Color(0xFFE8F5E9),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                .testTag("payment_status_pill_${customer.id}")
                        ) {
                            Text(
                                text = if (isOverdue) "OVERDUE" else "PAID",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOverdue) MaterialTheme.colorScheme.error
                                else Color(0xFF2E7D32)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${customer.businessType} • Fee: ₹${customer.feeAmount.toInt()}/${customer.feePlanName.substring(0, Math.min(customer.feePlanName.length, 3))}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Due: ${formatMillis(customer.nextDueDateMillis)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Interactive Progress Tracking Bar Segment
            if (totalMilestonesCount > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Milestones Progress: $completedMilestonesCount/$totalMilestonesCount completed",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(progressPercent * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progressPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .testTag("milestone_progress_bar_${customer.id}"),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            }
        }
    }
}
