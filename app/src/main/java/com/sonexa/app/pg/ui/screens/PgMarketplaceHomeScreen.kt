package com.sonexa.app.pg.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonexa.app.pg.model.PropertySummary
import com.sonexa.app.pg.ui.components.PropertyCard
import com.sonexa.app.pg.ui.theme.PgPrimary
import com.sonexa.app.pg.ui.theme.PgSecondary
import com.sonexa.app.pg.ui.theme.PgVerifiedGreen
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PgMarketplaceHomeScreen(
    properties: List<PropertySummary>,
    isLoading: Boolean,
    onPropertyClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onCompareClick: (List<String>) -> Unit,
    onOwnerDashboardClick: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    favoriteIds: Set<String> = emptySet(),
    modifier: Modifier = Modifier
) {
    var selectedCity by remember { mutableStateOf("All Cities") }
    var selectedGenderFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    val cities = listOf("All Cities", "Bangalore", "Pune", "Noida", "Delhi", "Hyderabad", "Mumbai")
    val genderFilters = listOf("All", "Boys PG", "Girls PG", "Co-Living")

    val filteredList = remember(properties, selectedCity, selectedGenderFilter, searchQuery) {
        properties.filter { p ->
            val matchCity = if (selectedCity == "All Cities") true else p.city.equals(selectedCity, ignoreCase = true)
            val matchGender = when (selectedGenderFilter) {
                "Boys PG" -> p.genderPolicy == "MALE_ONLY" || p.propertyType == "BOYS"
                "Girls PG" -> p.genderPolicy == "FEMALE_ONLY" || p.propertyType == "GIRLS"
                "Co-Living" -> p.propertyType == "CO_ED"
                else -> true
            }
            val matchQuery = if (searchQuery.isBlank()) true else {
                p.name.contains(searchQuery, ignoreCase = true) ||
                p.locality.contains(searchQuery, ignoreCase = true) ||
                p.city.contains(searchQuery, ignoreCase = true)
            }
            matchCity && matchGender && matchQuery
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Header Row: Brand Logo, Owner Switch, Wishlist
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "STAY",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = PgPrimary
                            )
                            Text(
                                text = "LUX",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = PgSecondary
                            )
                        }
                        Text(
                            text = "Verified PGs & Co-Living Spaces",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Owner Mode Button
                        FilledTonalButton(
                            onClick = onOwnerDashboardClick,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = "Owner Mode",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Owner Hub", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Wishlist Icon
                        IconButton(
                            onClick = onFavoritesClick,
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Favorites",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar Input with Filter Action
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onSearchClick)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Search locality, city, landmark...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onFilterClick,
                            modifier = Modifier
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Filters",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // City Filter Horizontal Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cities.forEach { city ->
                        val isSelected = selectedCity == city
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCity = city },
                            label = { Text(text = city, fontSize = 12.sp) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
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
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Gender Policy Sub-filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    genderFilters.forEach { filter ->
                        val isSelected = selectedGenderFilter == filter
                        SuggestionChip(
                            onClick = { selectedGenderFilter = filter },
                            label = { Text(text = filter, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            shape = RoundedCornerShape(12.dp),
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Results Counter & Verified Count
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredList.size} Verified Properties Available",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Verified Only",
                            tint = PgVerifiedGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "100% Verified",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = PgVerifiedGreen
                        )
                    }
                }
            }

            // Loading Skeleton or Properties
            if (isLoading) {
                items(3) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            } else if (filteredList.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apartment,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "No PG properties match your filters",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Try clearing search filters or changing the selected city",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                selectedCity = "All Cities"
                                selectedGenderFilter = "All"
                                searchQuery = ""
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "Reset All Filters")
                        }
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { property ->
                    val isFav = favoriteIds.contains(property.id)
                    PropertyCard(
                        property = property,
                        isFavorite = isFav,
                        onCardClick = { onPropertyClick(property.id) },
                        onFavoriteClick = { onToggleFavorite(property.id) }
                    )
                }
            }
        }
    }
}
