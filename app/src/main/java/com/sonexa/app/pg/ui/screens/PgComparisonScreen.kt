package com.sonexa.app.pg.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonexa.app.pg.model.PropertyComparison
import com.sonexa.app.pg.ui.theme.PgPrimary
import com.sonexa.app.pg.ui.theme.PgRose
import com.sonexa.app.pg.ui.theme.PgVerifiedGreen
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PgComparisonScreen(
    comparisons: List<PropertyComparison>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Compare PG Properties (${comparisons.size})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    comparisons.forEach { p ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.width(220.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = p.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                                Text(text = "${p.locality}, ${p.city}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))

                                Text(text = "Starting Rent", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = currencyFormat.format(p.startingRent).replace(".00", "") + " /mo",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = PgPrimary
                                )

                                Text(text = "Est. Total Monthly Cost", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = currencyFormat.format(p.estimatedTotalMonthlyCost).replace(".00", ""),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )

                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                                ComparisonFeatureRow(title = "AC in Room", hasFeature = p.hasAc)
                                ComparisonFeatureRow(title = "Attached Bath", hasFeature = p.hasAttachedBathroom)
                                ComparisonFeatureRow(title = "Wi-Fi Included", hasFeature = p.hasWifi)
                                ComparisonFeatureRow(title = "Daily Meals", hasFeature = p.hasFoodIncluded)
                                ComparisonFeatureRow(title = "Laundry", hasFeature = p.hasLaundry)
                                ComparisonFeatureRow(title = "Power Backup", hasFeature = p.hasPowerBackup)
                                ComparisonFeatureRow(title = "CCTV Security", hasFeature = p.hasSecurityCctv)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComparisonFeatureRow(title: String, hasFeature: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        Icon(
            imageVector = if (hasFeature) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (hasFeature) PgVerifiedGreen else PgRose,
            modifier = Modifier.size(16.dp)
        )
    }
}
