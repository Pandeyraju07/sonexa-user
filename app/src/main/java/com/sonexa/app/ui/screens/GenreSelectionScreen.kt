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
import androidx.compose.material.icons.filled.Check
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
import com.sonexa.app.ui.viewmodel.GenreSelectionViewModel

@Composable
fun GenreSelectionScreen(
    onGenresSelected: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GenreSelectionViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val selected by viewModel.selected.collectAsState()
    val saving by viewModel.saving.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F0726), Color(0xFF080512), Color(0xFF05030A))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        when (val state = uiState) {
            is CatalogUiState.Loading -> {
                CircularProgressIndicator(
                    color = SonexaPurpleLight,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is CatalogUiState.Error -> {
                Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.message, color = SonexaTextMuted)
                    Spacer(Modifier.height(12.dp))
                    SonexaGradientButton(text = "Retry", onClick = { viewModel.load() })
                }
            }
            is CatalogUiState.Ready -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    HeaderSection(
                        title = "Select Favorite Genres",
                        subtitle = "Choose genres you enjoy listening to"
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 16.dp)
                    ) {
                        items(state.data, key = { it.id.ifBlank { it.name } }) { genre ->
                            val isSelected = selected.contains(genre.name)
                            val c1 = genre.color1.toComposeColor(Color(0xFF6B3CE9))
                            val c2 = genre.color2.toComposeColor(Color(0xFF9825DD))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) Brush.horizontalGradient(listOf(c1, c2))
                                        else Brush.horizontalGradient(listOf(SonexaInputBg, SonexaInputBg))
                                    )
                                    .border(
                                        1.5.dp,
                                        if (isSelected) Color.Transparent else SonexaInputBorder,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { viewModel.toggle(genre.name) }
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        genre.name,
                                        color = SonexaTextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, null, tint = SonexaTextWhite, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                    SonexaGradientButton(
                        text = if (saving) "Saving..." else "Continue (${selected.size})",
                        onClick = {
                            if (selected.isEmpty()) {
                                Toast.makeText(context, "Select at least one genre", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.save(onGenresSelected)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
internal fun HeaderSection(title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 10.dp)) {
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = SonexaTextWhite)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, fontSize = 13.sp, color = SonexaTextMuted)
    }
}

internal fun String.toComposeColor(fallback: Color): Color = try {
    Color(android.graphics.Color.parseColor(this))
} catch (_: Exception) {
    fallback
}

data class GenreItem(val name: String, val color1: Color, val color2: Color)
