package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.AppDatabase
import com.example.data.repository.ClientTrackRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodels.ClientTrackViewModel
import com.example.ui.viewmodels.ClientTrackViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val context = LocalContext.current
        val database = AppDatabase.getDatabase(context.applicationContext)
        val repository = remember { ClientTrackRepository(database.clientTrackDao()) }
        val application = context.applicationContext as Application
        val viewModel: ClientTrackViewModel = viewModel(
          factory = remember { ClientTrackViewModelFactory(application, repository) }
        )

        MainAppScreen(viewModel)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: ClientTrackViewModel) {
  val isSignedUp by viewModel.isSignedUp.collectAsState()

  if (!isSignedUp) {
    OnboardingScreen(viewModel = viewModel)
  } else {
    val activeTab by viewModel.activeTab.collectAsState()
    val businessName by viewModel.businessName.collectAsState()

    var showRenameDialog by remember { mutableStateOf(false) }
    var tempBusinessName by remember { mutableStateOf(businessName) }

    if (showRenameDialog) {
      AlertDialog(
        onDismissRequest = { showRenameDialog = false },
        title = { Text("Rename Business Studio", fontWeight = FontWeight.Bold) },
        text = {
          OutlinedTextField(
            value = tempBusinessName,
            onValueChange = { tempBusinessName = it },
            label = { Text("Business Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("business_rename_input_field")
          )
        },
        confirmButton = {
          Button(
            onClick = {
              viewModel.updateBusinessName(tempBusinessName.trim())
              showRenameDialog = false
            },
            modifier = Modifier.testTag("business_rename_confirm_btn")
          ) {
            Text("Save", fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showRenameDialog = false }) {
            Text("Cancel")
          }
        }
      )
    }

    Scaffold(
      modifier = Modifier.fillMaxSize().testTag("main_scaffold"),
      topBar = {
        TopAppBar(
          title = {
            Column {
              Text(
                text = businessName,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
              )
              Text(
                text = "ClientTrack Fee Desk",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          },
          actions = {
            IconButton(
              onClick = {
                tempBusinessName = businessName
                showRenameDialog = true
              },
              modifier = Modifier.testTag("rename_business_top_btn")
            ) {
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Rename business studio header"
              )
            }
            IconButton(
              onClick = {
                viewModel.logout()
              },
              modifier = Modifier.testTag("logout_business_top_btn")
            ) {
              Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Logout and onboard again"
              )
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
          ),
          modifier = Modifier.testTag("app_top_bar")
        )
      },
      bottomBar = {
        NavigationBar(
          modifier = Modifier.testTag("app_bottom_nav_bar")
        ) {
          NavigationBarItem(
            selected = activeTab == "dashboard",
            onClick = { viewModel.setActiveTab("dashboard") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard screen") },
            label = { Text("Home", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_item_dashboard")
          )
          NavigationBarItem(
            selected = activeTab == "customers",
            onClick = { viewModel.setActiveTab("customers") },
            icon = { Icon(Icons.Default.Person, contentDescription = "Client management screen") },
            label = { Text("Clients", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_item_clients")
          )
          NavigationBarItem(
            selected = activeTab == "fees",
            onClick = { viewModel.setActiveTab("fees") },
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Fee dues screen") },
            label = { Text("Dues", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_item_dues")
          )
          NavigationBarItem(
            selected = activeTab == "reports",
            onClick = { viewModel.setActiveTab("reports") },
            icon = { Icon(Icons.Default.DateRange, contentDescription = "Receipts and Reports screen") },
            label = { Text("Receipts", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_item_receipts")
          )
          NavigationBarItem(
            selected = activeTab == "plans",
            onClick = { viewModel.setActiveTab("plans") },
            icon = { Icon(Icons.Default.Star, contentDescription = "Plans screen") },
            label = { Text("Plans", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_item_plans")
          )
        }
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        when (activeTab) {
          "dashboard" -> DashboardTab(
            viewModel = viewModel,
            onNavigateToPlans = { viewModel.setActiveTab("plans") }
          )
          "customers" -> CustomersTab(
            viewModel = viewModel
          )
          "fees" -> FeesTab(
            viewModel = viewModel
          )
          "reports" -> ReportsTab(
            viewModel = viewModel
          )
          "plans" -> PlansTab(
            viewModel = viewModel
          )
        }
      }
    }
  }
}
