package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodels.ClientTrackViewModel

@Composable
fun PlansTab(
    viewModel: ClientTrackViewModel,
    modifier: Modifier = Modifier
) {
    val currentPlan by viewModel.currentPlan.collectAsState()
    val trialDaysLeft by viewModel.trialDaysLeft.collectAsState()

    var showSuccessUpgradeDialogPlan by remember { mutableStateOf<String?>(null) }

    if (showSuccessUpgradeDialogPlan != null) {
        AlertDialog(
            onDismissRequest = { showSuccessUpgradeDialogPlan = null },
            title = {
                Text(
                    "Subscription Upgraded! 🎉",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    "You have successfully dynamic-unlocked the '$showSuccessUpgradeDialogPlan' tier for ClientTrack!\n\n" +
                            "Features like unlimited customers, bulk reminder sequences, and reports are now active."
                )
            },
            confirmButton = {
                Button(onClick = { showSuccessUpgradeDialogPlan = null }) {
                    Text("OK, Proceed!")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("plans_tab_root"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Sub Details Box
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("current_active_subscription_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Account Status",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Current Tier: $currentPlan",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (currentPlan == "Free Trial") {
                        Text(
                            text = "Your 14-day evaluation expires in $trialDaysLeft days. Upgrade below to prevent operations pause.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            lineHeight = 16.sp
                        )
                    } else {
                        Text(
                            text = "Premium payment setup and invoice ledgers unlocked. Next automatic billing invoice in 28 days.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            lineHeight = 16.sp
                        )
                    }

                    // Trial reset button to play around
                    if (currentPlan != "Free Trial") {
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(
                            onClick = { viewModel.resetTrial() },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .testTag("reset_trial_btn")
                        ) {
                            Text("Revert to evaluating State (Trial)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "Select ClientTrack Plan",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Tiers Cards (Starter, Growth, Enterprise)
        item {
            PlanTierCard(
                title = "Starter Plan",
                price = "₹499",
                period = "month",
                isPopular = false,
                features = listOf(
                    "Onboard up to 50 active clients" to true,
                    "Basic due dates & join-date recording" to true,
                    "Manual sharing reminder texts" to true,
                    "Milestones progress checklists" to true,
                    "Custom analytics reports" to false,
                    "Automated bulk alerts" to false
                ),
                activePlan = currentPlan,
                onUpgrade = {
                    viewModel.upgradePlan("Starter")
                    showSuccessUpgradeDialogPlan = "Starter"
                }
            )
        }

        // Growth Plan (Popular tier)
        item {
            PlanTierCard(
                title = "Growth Plan",
                price = "₹999",
                period = "month",
                isPopular = true,
                features = listOf(
                    "UNLIMITED customer profiles" to true,
                    "Comprehensive payments history tracker" to true,
                    "Pre-completed WhatsApp/SMS Templates" to true,
                    "Staggered business milestone categories" to true,
                    "Basic financial collection analytics" to true,
                    "Bulk remind helper utilities" to true
                ),
                activePlan = currentPlan,
                onUpgrade = {
                    viewModel.upgradePlan("Growth")
                    showSuccessUpgradeDialogPlan = "Growth"
                }
            )
        }

        // Enterprise Plan
        item {
            PlanTierCard(
                title = "Enterprise Plan",
                price = "₹1,999",
                period = "month",
                isPopular = false,
                features = listOf(
                    "Everything in Growth Tier" to true,
                    "Receipt download & ledger logs export" to true,
                    "Downloadable invoice PDFs" to true,
                    "Multi-staff role assignment accounts" to true,
                    "Dedicated custom support desk access" to true,
                    "Automated webhooks integration" to true
                ),
                activePlan = currentPlan,
                onUpgrade = {
                    viewModel.upgradePlan("Enterprise")
                    showSuccessUpgradeDialogPlan = "Enterprise"
                }
            )
        }
    }
}

@Composable
fun PlanTierCard(
    title: String,
    price: String,
    period: String,
    isPopular: Boolean,
    features: List<Pair<String, Boolean>>,
    activePlan: String,
    onUpgrade: () -> Unit
) {
    val isCurrent = activePlan.equals(title.replace(" Plan", ""), ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("plan_card_${title.replace(" ", "_").lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (isPopular || isCurrent) 2.dp else 1.dp,
            color = if (isCurrent) MaterialTheme.colorScheme.primary
            else if (isPopular) MaterialTheme.colorScheme.secondary
            else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (isPopular) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "MOST POPULAR",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = price,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "/$period",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Features Checklist Vertical block
            features.forEach { (feat, included) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (included) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (included) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = feat,
                        fontSize = 12.sp,
                        color = if (included) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onUpgrade,
                enabled = !isCurrent,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("activate_plan_${title.replace(" ", "_").lowercase()}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCurrent) MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (isCurrent) "Current Plan" else "Select & Activate",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
