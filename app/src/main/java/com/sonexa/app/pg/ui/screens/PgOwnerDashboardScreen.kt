package com.sonexa.app.pg.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonexa.app.pg.model.Lead
import com.sonexa.app.pg.model.OwnerDashboard
import com.sonexa.app.pg.model.PropertySummary
import com.sonexa.app.pg.ui.theme.PgPrimary
import com.sonexa.app.pg.ui.theme.PgRose
import com.sonexa.app.pg.ui.theme.PgSecondary
import com.sonexa.app.pg.ui.theme.PgVerifiedGreen
import com.sonexa.app.pg.ui.theme.PgWarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PgOwnerDashboardScreen(
    dashboard: OwnerDashboard?,
    myProperties: List<PropertySummary>,
    leads: List<Lead>,
    onBackClick: () -> Unit,
    onAddPropertyClick: () -> Unit,
    onPropertyClick: (String) -> Unit,
    onViewLeadsClick: () -> Unit,
    onViewVisitsClick: () -> Unit,
    onViewBookingsClick: () -> Unit,
    onSubscriptionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "Owner Operations Hub", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = "StayLux Co-Living Partner", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onSubscriptionClick) {
                        Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = "Subscription", tint = PgWarningAmber)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddPropertyClick,
                icon = { Icon(imageVector = Icons.Default.AddHome, contentDescription = null) },
                text = { Text(text = "Add New PG", fontWeight = FontWeight.Bold) },
                containerColor = PgPrimary,
                contentColor = Color.White
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // KPI Metrics Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Real-Time Occupancy & Inventory",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Total Beds",
                            value = "${dashboard?.totalBeds ?: 0}",
                            subtitle = "${dashboard?.occupiedBeds ?: 0} Occupied",
                            icon = Icons.Default.Bed,
                            color = PgPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        MetricCard(
                            title = "Available Beds",
                            value = "${dashboard?.availableBeds ?: 0}",
                            subtitle = "${dashboard?.occupancyRatePercentage ?: 0.0}% Occupancy",
                            icon = Icons.Default.CheckCircle,
                            color = PgVerifiedGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "New Leads",
                            value = "${dashboard?.newLeads ?: 0}",
                            subtitle = "${dashboard?.totalLeads ?: 0} Total Enquiries",
                            icon = Icons.Default.PersonSearch,
                            color = PgSecondary,
                            modifier = Modifier.weight(1f)
                        )

                        MetricCard(
                            title = "Visits & Bookings",
                            value = "${dashboard?.upcomingVisits ?: 0}",
                            subtitle = "${dashboard?.pendingBookingRequests ?: 0} Booking Req",
                            icon = Icons.Default.EventAvailable,
                            color = PgWarningAmber,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Quick Operations Navigation
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = onViewLeadsClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Leads (${dashboard?.newLeads ?: 0})", fontSize = 12.sp)
                    }

                    FilledTonalButton(
                        onClick = onViewVisitsClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Visits (${dashboard?.upcomingVisits ?: 0})", fontSize = 12.sp)
                    }

                    FilledTonalButton(
                        onClick = onViewBookingsClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Bookings", fontSize = 12.sp)
                    }
                }
            }

            // My Managed PG Properties Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My PG Properties (${myProperties.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (myProperties.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AddBusiness, contentDescription = null, tint = PgPrimary, modifier = Modifier.size(48.dp))
                            Text(text = "No PG Properties Listed Yet", fontWeight = FontWeight.Bold)
                            Text(
                                text = "Onboard your PG property, configure room types and start receiving tenant leads directly.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(onClick = onAddPropertyClick, shape = RoundedCornerShape(10.dp)) {
                                Text(text = "Start Multi-Step Onboarding")
                            }
                        }
                    }
                }
            } else {
                items(myProperties) { property ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = property.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(
                                    text = "${property.locality}, ${property.city}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        color = if (property.isVerified) PgVerifiedGreen.copy(alpha = 0.2f) else PgWarningAmber.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (property.isVerified) "VERIFIED" else "UNDER REVIEW",
                                            color = if (property.isVerified) PgVerifiedGreen else PgWarningAmber,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "${property.availableBeds}/${property.totalBeds} Beds Free",
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            IconButton(onClick = { onPropertyClick(property.id) }) {
                                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Manage")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold)
        }
    }
}
