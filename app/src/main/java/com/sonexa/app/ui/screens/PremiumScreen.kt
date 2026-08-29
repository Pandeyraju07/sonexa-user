package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonexa.app.ui.components.SonexaGradientButton
import com.sonexa.app.ui.theme.*

data class PremiumPlan(val title: String, val price: String, val period: String, val desc: String, val color1: Color, val color2: Color)

@Composable
fun PremiumScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: com.sonexa.app.ui.viewmodel.PremiumViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val premiumState by viewModel.uiState.collectAsState()
    val busy by viewModel.busy.collectAsState()
    var selectedPlan by remember { mutableStateOf("Individual") }
    var selectedPaymentMethod by remember { mutableStateOf("UPI (GPay / PhonePe)") }
    var showPaymentModal by remember { mutableStateOf(false) }
    var showInvoiceModal by remember { mutableStateOf(false) }
    var paymentSuccess by remember { mutableStateOf(false) }

    val apiPlans = (premiumState as? com.sonexa.app.ui.viewmodel.CatalogUiState.Ready)?.data?.plans.orEmpty()
    val plans = if (apiPlans.isNotEmpty()) {
        apiPlans.map {
            PremiumPlan(
                it.name,
                it.price.substringBefore("/").ifBlank { it.price },
                "per month",
                it.description,
                Color(0xFF6B3CE9),
                Color(0xFF9825DD)
            )
        }
    } else {
        listOf(
            PremiumPlan("Individual", "₹119", "per month", "1 Premium Account • Lossless Audio • Zero Ads", Color(0xFF6B3CE9), Color(0xFF9825DD)),
            PremiumPlan("Duo", "₹149", "per month", "2 Premium Accounts for couples or roommates", Color(0xFFE534B2), Color(0xFFFF52C4)),
            PremiumPlan("Family", "₹179", "per month", "Up to 6 Premium Accounts + Family Mix", Color(0xFF06B6D4), Color(0xFF3B82F6)),
            PremiumPlan("Student", "₹59", "per month", "Special discount for verified university students", Color(0xFFF59E0B), Color(0xFFEF4444))
        )
    }

    val benefits = (premiumState as? com.sonexa.app.ui.viewmodel.CatalogUiState.Ready)?.data?.benefits?.takeIf { it.isNotEmpty() }
        ?: listOf(
        "🎧 Hi-Fi Lossless 24-bit/192kHz Audio Streaming",
        "🚫 100% Ad-Free Music Experience",
        "📥 Unlimited Offline Downloads on 5 Devices",
        "🤖 Unlimited Access to Sonexa AI DJ & Voice Assistant",
        "🎨 Exclusive AI Playlist Cover Generator Tools"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SonexaBgDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(bottom = 135.dp)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SonexaInputBg)
                            .clickable { onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SonexaTextWhite, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Sonexa Premium", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                }
            }

            // Hero Premium Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFEF4444))))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "TRY 3 MONTHS FREE", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Unlock Lossless Audio & Zero Ads", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Cancel anytime. Terms and conditions apply.", fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
                    }
                }
            }

            // Benefits Checklist
            item {
                Text(text = "Premium Benefits", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    benefits.forEach { b ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SonexaInputBg)
                                .padding(12.dp)
                        ) {
                            Text(text = b, fontSize = 13.sp, color = SonexaTextWhite, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Plans Selector
            item {
                Text(text = "Choose Your Plan", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
            }

            items(plans) { plan ->
                val isSelected = selectedPlan == plan.title
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(SonexaInputBg)
                        .border(1.5.dp, if (isSelected) SonexaPurpleLight else SonexaInputBorder, RoundedCornerShape(18.dp))
                        .clickable { selectedPlan = plan.title }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = isSelected, onClick = { selectedPlan = plan.title })
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = plan.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = plan.desc, fontSize = 12.sp, color = SonexaTextMuted)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = plan.price, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC084FC))
                            Text(text = plan.period, fontSize = 11.sp, color = SonexaTextSubtle)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                SonexaGradientButton(
                    text = "Subscribe Now ($selectedPlan)",
                    onClick = { showPaymentModal = true }
                )
            }

            // History & Invoice Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = { showInvoiceModal = true }) {
                        Text(text = "View Subscription History & Invoices", color = SonexaPurpleLight, fontSize = 13.sp)
                    }
                }
            }
        }

        // Payment Gateway Checkout Modal
        if (showPaymentModal) {
            AlertDialog(
                onDismissRequest = { showPaymentModal = false },
                containerColor = SonexaCardDark,
                title = { Text(text = "Payment Gateway ($selectedPlan)", color = SonexaTextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(text = "Select Payment Method:", fontSize = 13.sp, color = SonexaTextMuted)
                        Spacer(modifier = Modifier.height(10.dp))
                        listOf("UPI (GPay / PhonePe / Paytm)", "Credit / Debit Card", "Net Banking", "Mobile Wallet").forEach { method ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPaymentMethod = method }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selectedPaymentMethod == method, onClick = { selectedPaymentMethod = method })
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = method, color = SonexaTextWhite, fontSize = 14.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    SonexaGradientButton(
                        text = "Pay Now",
                        onClick = {
                            viewModel.subscribe(selectedPlan.lowercase()) {
                                showPaymentModal = false
                                paymentSuccess = true
                                Toast.makeText(context, "Payment Successful! Premium Activated", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                },
                dismissButton = {
                    TextButton(onClick = { showPaymentModal = false }) { Text(text = "Cancel", color = SonexaTextMuted) }
                }
            )
        }

        // Invoice Modal
        if (showInvoiceModal) {
            AlertDialog(
                onDismissRequest = { showInvoiceModal = false },
                containerColor = SonexaCardDark,
                title = { Text(text = "Subscription Invoice #SNX-99482", color = SonexaTextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(text = "Plan: Sonexa Premium Individual", color = SonexaTextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Amount Paid: ₹119.00 (Tax Incl.)", color = SonexaTextMuted, fontSize = 13.sp)
                        Text(text = "Payment Method: UPI (GPay)", color = SonexaTextMuted, fontSize = 13.sp)
                        Text(text = "Billing Date: 30 July 2026", color = SonexaTextMuted, fontSize = 13.sp)
                        Text(text = "Status: ACTIVE ✅", color = Color(0xFF10B981), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                },
                confirmButton = {
                    Button(onClick = { showInvoiceModal = false }) { Text(text = "Download PDF Invoice") }
                }
            )
        }
    }
}
