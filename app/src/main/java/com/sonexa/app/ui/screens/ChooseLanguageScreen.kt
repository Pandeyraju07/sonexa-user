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
import com.sonexa.app.data.local.SessionManager
import com.sonexa.app.ui.components.SonexaGradientButton
import com.sonexa.app.ui.theme.*
import com.sonexa.app.ui.viewmodel.ChooseLanguageUiState
import com.sonexa.app.ui.viewmodel.ChooseLanguageViewModel

@Composable
fun ChooseLanguageScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    chooseLanguageViewModel: ChooseLanguageViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager.getInstance(context) }
    val uiState by chooseLanguageViewModel.uiState.collectAsState()
    val selectedLanguages by chooseLanguageViewModel.selectedLanguages.collectAsState()
    val isSaving by chooseLanguageViewModel.isSaving.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0726),
                        Color(0xFF080512),
                        Color(0xFF05030A)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        when (val state = uiState) {
            is ChooseLanguageUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SonexaPurpleLight)
                }
            }

            is ChooseLanguageUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(state.message, color = SonexaTextMuted)
                    Spacer(modifier = Modifier.height(12.dp))
                    SonexaGradientButton(text = "Retry", onClick = { chooseLanguageViewModel.loadLanguages() })
                }
            }

            is ChooseLanguageUiState.Ready -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        Text(
                            text = state.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SonexaTextWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.subtitle,
                            fontSize = 13.sp,
                            color = SonexaTextMuted
                        )
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 16.dp)
                    ) {
                        items(state.languages, key = { it.code.ifBlank { it.name } }) { lang ->
                            val isSelected = selectedLanguages.contains(lang.name)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) Color(0xFF1E1738) else SonexaInputBg)
                                    .border(
                                        1.5.dp,
                                        if (isSelected) SonexaPurpleLight else SonexaInputBorder,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { chooseLanguageViewModel.toggleLanguage(lang.name) }
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = lang.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) SonexaTextWhite else SonexaTextMuted
                                    )
                                    Text(
                                        text = lang.nativeName,
                                        fontSize = 11.sp,
                                        color = SonexaTextSubtle
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = SonexaPurpleLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SonexaGradientButton(
                            text = if (isSaving) {
                                "Saving..."
                            } else {
                                "Continue (${selectedLanguages.size} selected)"
                            },
                            onClick = {
                                if (isSaving) return@SonexaGradientButton
                                if (selectedLanguages.size < state.minSelection) {
                                    Toast.makeText(
                                        context,
                                        "Please select at least ${state.minSelection} language",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    chooseLanguageViewModel.saveAndContinue(sessionManager, onContinue)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}
