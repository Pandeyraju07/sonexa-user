package com.sonexa.app.pg.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonexa.app.pg.ui.theme.PgPrimary
import com.sonexa.app.pg.ui.theme.PgVerifiedGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PgOwnerOnboardingScreen(
    onBackClick: () -> Unit,
    onSubmitComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 5

    // Step 1: Basic Info
    var pgName by remember { mutableStateOf("") }
    var pgDescription by remember { mutableStateOf("") }
    var propertyType by remember { mutableStateOf("CO_ED") }
    var genderPolicy by remember { mutableStateOf("ANY") }

    // Step 2: Address & Geo
    var addressLine by remember { mutableStateOf("") }
    var locality by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Bangalore") }
    var pincode by remember { mutableStateOf("") }

    // Step 3: Photos & Amenities
    var photoUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1555854877-bab0e564b8d5?auto=format&fit=crop&w=800&q=80") }
    val selectedAmenities = remember { mutableStateListOf("AC", "WIFI", "FOOD", "ATTACHED_BATHROOM", "LAUNDRY") }

    // Step 4: Room Type & Pricing
    var singleRent by remember { mutableStateOf("15000") }
    var singleDeposit by remember { mutableStateOf("30000") }
    var doubleRent by remember { mutableStateOf("9500") }
    var doubleDeposit by remember { mutableStateOf("19000") }

    // Step 5: House Rules
    var gateClosingTime by remember { mutableStateOf("11:00 PM") }
    var noSmoking by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "Onboard New PG Property", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "Step $currentStep of $totalSteps", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Button(
                        onClick = {
                            if (currentStep < totalSteps) {
                                currentStep++
                            } else {
                                onSubmitComplete()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PgPrimary)
                    ) {
                        Text(
                            text = if (currentStep == totalSteps) "Submit for Verification" else "Save & Next",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Linear Step Progress Bar
            item {
                LinearProgressIndicator(
                    progress = { currentStep.toFloat() / totalSteps.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = PgPrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            when (currentStep) {
                1 -> {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(text = "Step 1: PG Basic Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = pgName,
                                onValueChange = { pgName = it },
                                label = { Text("PG Property Name (e.g. StayLux Elite PG)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = pgDescription,
                                onValueChange = { pgDescription = it },
                                label = { Text("Property Description & Highlights") },
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                2 -> {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(text = "Step 2: Address & Location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = addressLine,
                                onValueChange = { addressLine = it },
                                label = { Text("Street Address / Building Number") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = locality,
                                onValueChange = { locality = it },
                                label = { Text("Locality / Area (e.g. Koramangala)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("City (e.g. Bangalore)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = pincode,
                                onValueChange = { pincode = it },
                                label = { Text("Pincode (e.g. 560034)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                3 -> {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(text = "Step 3: Photos & Amenities", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = photoUrl,
                                onValueChange = { photoUrl = it },
                                label = { Text("Cover Photo URL") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(text = "Included Amenities", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            val allAmenityOpts = listOf("AC", "WIFI", "FOOD", "ATTACHED_BATHROOM", "LAUNDRY", "POWER_BACKUP", "SECURITY", "GYM")
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                allAmenityOpts.forEach { code ->
                                    val isChecked = selectedAmenities.contains(code)
                                    FilterChip(
                                        selected = isChecked,
                                        onClick = {
                                            if (isChecked) selectedAmenities.remove(code) else selectedAmenities.add(code)
                                        },
                                        label = { Text(text = code.replace("_", " ")) }
                                    )
                                }
                            }
                        }
                    }
                }
                4 -> {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(text = "Step 4: Room Types & Pricing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = "Single Private Room", fontWeight = FontWeight.Bold)
                                    OutlinedTextField(
                                        value = singleRent,
                                        onValueChange = { singleRent = it },
                                        label = { Text("Monthly Rent (₹)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = singleDeposit,
                                        onValueChange = { singleDeposit = it },
                                        label = { Text("Security Deposit (₹)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = "Double Sharing Room", fontWeight = FontWeight.Bold)
                                    OutlinedTextField(
                                        value = doubleRent,
                                        onValueChange = { doubleRent = it },
                                        label = { Text("Monthly Rent (₹)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = doubleDeposit,
                                        onValueChange = { doubleDeposit = it },
                                        label = { Text("Security Deposit (₹)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
                5 -> {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(text = "Step 5: Review & Submit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Surface(
                                color = PgVerifiedGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = PgVerifiedGreen)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = "Listing Completeness Score: 92%", fontWeight = FontWeight.Bold, color = PgVerifiedGreen)
                                        Text(text = "Ready for Instant Admin Verification", fontSize = 12.sp)
                                    }
                                }
                            }

                            Text(
                                text = "By submitting, you confirm that the property information, room pricing, and amenities provided are accurate and comply with local housing regulations.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
