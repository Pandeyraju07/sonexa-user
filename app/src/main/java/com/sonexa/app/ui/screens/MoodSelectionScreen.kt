package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonexa.app.ui.components.SonexaGradientButton
import com.sonexa.app.ui.theme.*
import com.sonexa.app.ui.viewmodel.CatalogUiState
import com.sonexa.app.ui.viewmodel.MoodSelectionViewModel

@Composable
fun MoodSelectionScreen(
    onMoodsSelected: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MoodSelectionViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val selected by viewModel.selected.collectAsState()
    val saving by viewModel.saving.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0F0726), Color(0xFF080512), Color(0xFF05030A)))
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        when (val state = uiState) {
            is CatalogUiState.Loading -> CircularProgressIndicator(
                color = SonexaPurpleLight, modifier = Modifier.align(Alignment.Center)
            )
            is CatalogUiState.Error -> Column(
                Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(state.message, color = SonexaTextMuted)
                Spacer(Modifier.height(12.dp))
                SonexaGradientButton(text = "Retry", onClick = { viewModel.load() })
            }
            is CatalogUiState.Ready -> Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                HeaderSection("What's your mood?", "We'll tailor recommendations to match")
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 16.dp)
                ) {
                    items(state.data, key = { it.id.ifBlank { it.name } }) { mood ->
                        val isSelected = selected.contains(mood.name)
                        val color = mood.colorHex.toComposeColor(Color(0xFF8B5CF6))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) color.copy(alpha = 0.25f) else SonexaInputBg)
                                .border(
                                    1.5.dp,
                                    if (isSelected) color else SonexaInputBorder,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { viewModel.toggle(mood.name) }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        if (mood.name.contains("Energetic", true)) Icons.Default.Bolt else Icons.Default.Mood,
                                        null,
                                        tint = color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(mood.name, color = SonexaTextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, null, tint = color, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
                SonexaGradientButton(
                    text = if (saving) "Saving..." else "Continue (${selected.size})",
                    onClick = {
                        if (selected.isEmpty()) {
                            Toast.makeText(context, "Select at least one mood", Toast.LENGTH_SHORT).show()
                        } else viewModel.save(onMoodsSelected)
                    }
                )
            }
        }
    }
}
