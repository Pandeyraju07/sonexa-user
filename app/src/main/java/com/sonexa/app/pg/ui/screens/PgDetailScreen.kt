package com.sonexa.app.pg.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sonexa.app.pg.model.PropertyDetail
import com.sonexa.app.pg.model.RoomType
import com.sonexa.app.pg.ui.theme.PgPrimary
import com.sonexa.app.pg.ui.theme.PgRose
import com.sonexa.app.pg.ui.theme.PgSecondary
import com.sonexa.app.pg.ui.theme.PgVerifiedGreen
import com.sonexa.app.pg.ui.theme.PgWarningAmber
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PgDetailScreen(
    property: PropertyDetail?,
    isLoading: Boolean,
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onScheduleVisitClick: () -> Unit,
    onBookNowClick: (RoomType) -> Unit,
    onCallOwnerClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isLoading || property == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PgPrimary)
        }
        return
    }

    val indianCurrencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    var selectedRoomType by remember { mutableStateOf(property.roomTypes.firstOrNull()) }

    Scaffold(
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pricing Summary
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Starting from",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val rent = selectedRoomType?.baseMonthlyRent ?: property.minMonthlyRent
                        Text(
                            text = indianCurrencyFormat.format(rent).replace(".00", "") + " /mo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Schedule Visit Action
                    OutlinedButton(
                        onClick = onScheduleVisitClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Visit PG", fontSize = 13.sp)
                    }

                    // Book Now Action
                    Button(
                        onClick = { selectedRoomType?.let { onBookNowClick(it) } },
                        enabled = !property.isFullyOccupied,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PgPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = if (property.isFullyOccupied) "Full" else "Book Now", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 24.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Photo Carousel Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    val coverUrl = property.media.firstOrNull { it.isCover }?.mediaUrl
                        ?: property.media.firstOrNull()?.mediaUrl
                        ?: "https://images.unsplash.com/photo-1555854877-bab0e564b8d5?auto=format&fit=crop&w=800&q=80"

                    AsyncImage(
                        model = coverUrl,
                        contentDescription = property.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Top Bar Back + Wishlist Actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }

                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Save",
                                tint = if (isFavorite) PgRose else Color.White
                            )
                        }
                    }

                    // Verified Badge + Quality Score Pill
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(color = PgVerifiedGreen, shape = RoundedCornerShape(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Verified Listing", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(color = Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(8.dp)) {
                            Text(
                                text = "Score ${property.listingQualityScore}%",
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Property Info & Address Block
            item {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = property.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = PgPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${property.address?.addressLine1}, ${property.address?.locality}, ${property.address?.city} - ${property.address?.pincode}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Surface(color = PgWarningAmber, shape = RoundedCornerShape(6.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(text = "${property.averageRating} Rating", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(6.dp)) {
                            Text(
                                text = property.genderPolicy.replace("_", " "),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            color = if (property.availableBeds > 0) PgVerifiedGreen.copy(alpha = 0.15f) else PgRose.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${property.availableBeds} beds available",
                                color = if (property.availableBeds > 0) PgVerifiedGreen else PgRose,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (!property.description.isNullOrBlank()) {
                        Text(
                            text = property.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Room Types Selection & Transparent Pricing Breakdown
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Select Room Sharing Type",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        property.roomTypes.forEach { roomType ->
                            val isSelected = selectedRoomType?.id == roomType.id
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PgPrimary, PgSecondary))) else null,
                                modifier = Modifier
                                    .width(180.dp)
                                    .clickable { selectedRoomType = roomType }
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = roomType.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = indianCurrencyFormat.format(roomType.baseMonthlyRent).replace(".00", ""),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = PgPrimary
                                    )
                                    Text(
                                        text = "Deposit: ${indianCurrencyFormat.format(roomType.securityDeposit).replace(".00", "")}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${roomType.availableBedsCount} beds left",
                                        fontSize = 11.sp,
                                        color = PgVerifiedGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Selected Room Pricing Breakdown (Rent vs Estimated Monthly Cost)
                    selectedRoomType?.pricingBreakdown?.let { breakdown ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Transparent Monthly Cost Breakdown",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Base Room Rent", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = indianCurrencyFormat.format(breakdown.baseMonthlyRent).replace(".00", ""), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Daily Food / Mess", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = if (breakdown.foodCharge.compareTo(java.math.BigDecimal.ZERO) == 0) "Included" else indianCurrencyFormat.format(breakdown.foodCharge).replace(".00", ""), fontSize = 13.sp)
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Maintenance & Wi-Fi", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = indianCurrencyFormat.format(breakdown.maintenanceCharge).replace(".00", ""), fontSize = 13.sp)
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Estimated Total Monthly Cost", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = indianCurrencyFormat.format(breakdown.estimatedTotalMonthlyCost).replace(".00", ""),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Amenities Grid
            item {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Amenities & Services",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        property.amenities.forEach { amenity ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = PgVerifiedGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = amenity.name, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // House Rules
            item {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "House Rules & Policies",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    property.rules.forEach { rule ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (rule.isStrict) Icons.Default.GppMaybe else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (rule.isStrict) PgRose else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = rule.ruleTitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}
