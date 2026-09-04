package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonexa.app.data.model.PremiumPlanDto
import com.sonexa.app.ui.components.SonexaGradientButton
import com.sonexa.app.ui.theme.*
import com.sonexa.app.ui.viewmodel.CatalogUiState
import com.sonexa.app.ui.viewmodel.PremiumViewModel

private val SpotifyGreen = Color(0xFF1ED760)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PremiumViewModel = viewModel()
) {
    val context = LocalContext.current
    val premiumState by viewModel.uiState.collectAsState()
    val busy by viewModel.busy.collectAsState()

    var selectedPlanId by remember { mutableStateOf("individual") }
    var selectedPaymentMethod by remember { mutableStateOf("UPI (GPay / PhonePe / Paytm)") }
    var promoCodeInput by remember { mutableStateOf("") }

    var showPaymentModal by remember { mutableStateOf(false) }
    var showInvoiceModal by remember { mutableStateOf(false) }
    var showCancelModal by remember { mutableStateOf(false) }
    var showPromoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    val ready = premiumState as? CatalogUiState.Ready
    val isPremium = ready?.data?.isPremium == true

    val apiPlans = ready?.data?.plans.orEmpty()
    val plans = if (apiPlans.isNotEmpty()) {
        apiPlans
    } else {
        listOf(
            PremiumPlanDto("individual", "Individual", "₹119", "per month", "1 Premium Account • Lossless 320kbps • Zero Ads", "3 Months Free", "#6B3CE9", "#9825DD", listOf("1 Premium account", "Zero ads", "Lossless Audio", "Offline Downloads")),
            PremiumPlanDto("duo", "Duo", "₹149", "per month", "2 Premium Accounts for couples or roommates", "Most Popular", "#E534B2", "#FF52C4", listOf("2 Premium accounts", "Duo Mix playlist", "Lossless Audio", "Offline Downloads")),
            PremiumPlanDto("family", "Family", "₹179", "per month", "Up to 6 Premium Accounts + Family Mix", "Best Value", "#06B6D4", "#3B82F6", listOf("6 Premium accounts", "Family Mix", "Explicit Filter", "Lossless Audio")),
            PremiumPlanDto("student", "Student", "₹59", "per month", "Special discount for verified students", "Students Only", "#F59E0B", "#EF4444", listOf("1 Verified student account", "50% discount", "Zero ads", "Offline Downloads"))
        )
    }

    val benefits = ready?.data?.benefits?.takeIf { it.isNotEmpty() }
        ?: listOf(
            "🎧 Hi-Fi Lossless 24-bit/192kHz Audio Streaming",
            "🚫 100% Ad-Free Music Experience Across All Platforms",
            "📥 Unlimited Offline Downloads on 5 Devices",
            "🤖 Unlimited Access to Zynera AI DJ, Beat Generator & Studio",
            "🎨 Exclusive AI Playlist Cover Generator Tools",
            "⚡ Unlimited Track Skips & Maximum Fidelity"
        )

    val currentSelectedPlan = plans.find { it.id == selectedPlanId } ?: plans.firstOrNull()

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
            // Header Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF242424))
                                .clickable { onNavigateBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Zynera Premium",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    if (isPremium) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500))))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "VIP ACTIVE",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // Hero Status Banner
            item {
                if (isPremium) {
                    // Active VIP Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF4C1D95), Color(0xFF7C3AED), Color(0xFFBE185D))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFD700)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Stars, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("You're on Zynera Premium VIP", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("High-Fidelity 24-bit Lossless Audio Active", fontSize = 12.sp, color = Color(0xFFE9D5FF))
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Auto-renews next billing cycle", fontSize = 12.sp, color = Color(0xFFE9D5FF))
                                TextButton(onClick = { showCancelModal = true }) {
                                    Text("Manage / Cancel", color = Color(0xFFFFB4AB), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // Upgrade Promotion Hero
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF6B3CE9), Color(0xFF9825DD), Color(0xFFEC4899))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SpotifyGreen)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("LIMITED TIME OFFER", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Experience Music\nWithout Limits.",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                lineHeight = 28.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Ad-free, unlimited downloads, 24-bit lossless streaming, and AI DJ studio.",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // Promo Code Quick Card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF242424))
                        .clickable { showPromoDialog = true }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = SpotifyGreen, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Have a promo code or gift card?", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Redeem code 'ZYNERA2026' for 3 months free VIP", fontSize = 11.5.sp, color = Color(0xFFA19BAE))
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                }
            }

            // Section: Choose Your Plan
            item {
                Text(
                    text = "Choose Your Plan",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Plan Cards
            items(plans) { plan ->
                val isSelected = selectedPlanId == plan.id
                val colorHex1 = try {
                    Color(android.graphics.Color.parseColor(plan.color1.ifBlank { "#6B3CE9" }))
                } catch (_: Exception) { Color(0xFF6B3CE9) }
                val colorHex2 = try {
                    Color(android.graphics.Color.parseColor(plan.color2.ifBlank { "#9825DD" }))
                } catch (_: Exception) { Color(0xFF9825DD) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E1E1E))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) SpotifyGreen else Color(0xFF333333),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedPlanId = plan.id }
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) SpotifyGreen else Color(0xFF555555))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = plan.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            if (plan.badge.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Brush.horizontalGradient(listOf(colorHex1, colorHex2)))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = plan.badge,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = plan.price,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = " / ${plan.period}",
                                fontSize = 13.sp,
                                color = Color(0xFFA19BAE),
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = plan.description,
                            fontSize = 12.5.sp,
                            color = Color(0xFFA19BAE)
                        )

                        if (plan.features.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFF282828))
                            Spacer(modifier = Modifier.height(8.dp))
                            plan.features.forEach { feat ->
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = SpotifyGreen, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(feat, fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Get Premium CTA Button
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = {
                        if (isPremium) {
                            Toast.makeText(context, "You are already a Zynera VIP member!", Toast.LENGTH_SHORT).show()
                        } else {
                            showPaymentModal = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (busy) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (isPremium) "👑 You Are A VIP Member" else "Get Premium (${currentSelectedPlan?.name ?: "Individual"})",
                            color = Color.Black,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Premium Benefits List
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Why Join Premium?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E1E1E))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    benefits.forEach { benefit ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SpotifyGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(benefit, color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    // Checkout & Payment Modal
    if (showPaymentModal && currentSelectedPlan != null) {
        AlertDialog(
            onDismissRequest = { showPaymentModal = false },
            containerColor = Color(0xFF242424),
            title = {
                Text(
                    text = "Complete Your Subscription",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Selected Plan Summary Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E1E1E))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(currentSelectedPlan.name + " Plan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Monthly Recurring", color = Color(0xFFA19BAE), fontSize = 12.sp)
                            }
                            Text(currentSelectedPlan.price, color = SpotifyGreen, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        }
                    }

                    Text("Select Payment Method:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

                    listOf("UPI (GPay / PhonePe / Paytm)", "Credit / Debit Card", "Net Banking").forEach { method ->
                        val isMethodSelected = selectedPaymentMethod == method
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isMethodSelected) Color(0xFF333333) else Color(0xFF1E1E1E))
                                .clickable { selectedPaymentMethod = method }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isMethodSelected,
                                onClick = { selectedPaymentMethod = method },
                                colors = RadioButtonDefaults.colors(selectedColor = SpotifyGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(method, color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.subscribe(currentSelectedPlan.id) { success ->
                            showPaymentModal = false
                            if (success) {
                                showInvoiceModal = true
                                Toast.makeText(context, "Welcome to Zynera Premium VIP!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Subscription failed. Please try again.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                ) {
                    Text("Pay & Subscribe", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentModal = false }) {
                    Text("Cancel", color = Color(0xFFA19BAE))
                }
            }
        )
    }

    // Invoice & Receipt Modal
    if (showInvoiceModal && currentSelectedPlan != null) {
        AlertDialog(
            onDismissRequest = { showInvoiceModal = false },
            containerColor = Color(0xFF242424),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SpotifyGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Payment Successful!", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Thank you for upgrading to Zynera Premium!", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E1E1E))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Plan: ${currentSelectedPlan.name}", color = Color(0xFFA19BAE), fontSize = 12.sp)
                            Text("Amount: ${currentSelectedPlan.price}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Payment: $selectedPaymentMethod", color = Color(0xFFA19BAE), fontSize = 12.sp)
                            Text("Status: Active & Lossless Enabled", color = SpotifyGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showInvoiceModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                ) {
                    Text("Start Listening", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Promo Code Redemption Dialog
    if (showPromoDialog) {
        AlertDialog(
            onDismissRequest = { showPromoDialog = false },
            containerColor = Color(0xFF242424),
            title = { Text("Redeem Promo Code", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter your promotional code or gift card code below:", color = Color(0xFFA19BAE), fontSize = 13.sp)
                    OutlinedTextField(
                        value = promoCodeInput,
                        onValueChange = { promoCodeInput = it.uppercase() },
                        placeholder = { Text("e.g. ZYNERA2026", color = Color(0xFFA19BAE)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SpotifyGreen,
                            unfocusedBorderColor = Color(0xFF444444)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val code = promoCodeInput.trim()
                        if (code.isBlank()) {
                            Toast.makeText(context, "Please enter a code", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.redeemCoupon(code) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            if (success) {
                                showPromoDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                ) {
                    Text("Redeem", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPromoDialog = false }) {
                    Text("Cancel", color = Color(0xFFA19BAE))
                }
            }
        )
    }

    // Cancel Subscription Confirmation Dialog
    if (showCancelModal) {
        AlertDialog(
            onDismissRequest = { showCancelModal = false },
            containerColor = Color(0xFF242424),
            title = { Text("Cancel Premium Subscription?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "If you cancel, you will lose access to ad-free listening, offline downloads, and 24-bit lossless audio.",
                    color = Color(0xFFA19BAE),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelPremium {
                            showCancelModal = false
                            Toast.makeText(context, "Premium subscription cancelled.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Yes, Cancel", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelModal = false }) {
                    Text("Keep Premium", color = SpotifyGreen)
                }
            }
        )
    }
}
