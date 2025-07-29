package com.pjt.gestacao.ui.mapa

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.pjt.gestacao.model.Place
import kotlinx.coroutines.launch

// Cores personalizadas para o tema do mapa
val PinkPrimary = Color(0xFFDBA4A4)
val PinkLight = Color(0xFFFFF6F6)
val TextColorPrimary = Color(0xFF333333)
val TextColorSecondary = Color(0xFF666666)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaScreen(mapViewModel: MapViewModel = viewModel()) {

    val uiState by mapViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(uiState.initialLocation) {
        uiState.initialLocation?.let {
            cameraPositionState.animate(
                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(it, 14f)
            )
        }
    }

    LaunchedEffect(uiState.selectedPlaceDetails) {
        if (uiState.selectedPlaceDetails is LocationDetailsUiState.Idle && sheetState.isVisible) {
            scope.launch { sheetState.hide() }
        }
    }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        containerColor = PinkLight,
        topBar = {
            Column(modifier = Modifier.background(PinkLight)) {
                MapHeader()
                MapSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { mapViewModel.onSearchQueryChanged(it) },
                    onSearch = { mapViewModel.searchNearbyPlaces() }
                )
                FilterChips(
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = { filter ->
                        mapViewModel.onFilterChanged(filter)
                        mapViewModel.searchNearbyPlaces()
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isInitializing) {
                CircularProgressIndicator(color = PinkPrimary)
            } else {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = true),
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = true)
                ) {
                    uiState.nearbyPlaces.forEach { place ->
                        Marker(
                            state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                            title = place.name,
                            onClick = {
                                mapViewModel.onMarkerClicked(place)
                                scope.launch { sheetState.expand() }
                                true
                            }
                        )
                    }

                    if (uiState.routePoints.isNotEmpty()) {
                        Polyline(
                            points = uiState.routePoints,
                            color = PinkPrimary,
                            width = 15f
                        )
                    }
                }
            }

            if (uiState.isSearching) {
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Buscando locais próximos...", color = Color.White)
                    }
                }
            }

            if (uiState.selectedPlaceDetails !is LocationDetailsUiState.Idle) {
                ModalBottomSheet(
                    onDismissRequest = { mapViewModel.dismissModal() },
                    sheetState = sheetState,
                    containerColor = Color.White
                ) {
                    LocationDetailsBottomSheet(
                        state = uiState.selectedPlaceDetails,
                        onRouteClick = { place ->
                            mapViewModel.getDirectionsToPlace(place)
                            val gmmIntentUri = "google.navigation:q=${place.latitude},${place.longitude}".toUri()
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            context.startActivity(mapIntent)
                        },
                        onClose = { mapViewModel.dismissModal() }
                    )
                }
            }
        }
    }
}

@Composable
fun MapHeader() {
    Text(
        text = "Encontre ajuda perto de você",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = TextColorPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        textAlign = TextAlign.Center
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapSearchBar(query: String, onQueryChange: (String) -> Unit, onSearch: () -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Pesquisar por nome...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Pesquisar") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(30.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            containerColor = Color.White,
            unfocusedBorderColor = Color.LightGray,
            focusedBorderColor = PinkPrimary
        ),
        singleLine = true,
        keyboardActions = KeyboardActions(onSearch = { onSearch() })
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips(selectedFilter: String, onFilterSelected: (String) -> Unit) {
    val filters = listOf("Todos", "Hospitais ou maternidades", "Postos de Saúde", "ONGs de apoio a gestantes")
    LazyRow(
        modifier = Modifier.padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter) },
                enabled = true,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PinkPrimary,
                    selectedLabelColor = Color.White,
                    containerColor = Color.White,
                    labelColor = TextColorSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = PinkPrimary,
                    selectedBorderColor = Color.Transparent,
                    enabled = true,
                    selected = selectedFilter == filter
                )
            )
        }
    }
}

@Composable
fun LocationDetailsBottomSheet(state: LocationDetailsUiState, onRouteClick: (Place) -> Unit, onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 250.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is LocationDetailsUiState.Loading -> CircularProgressIndicator(color = PinkPrimary)
            is LocationDetailsUiState.Success -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(state.place.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextColorPrimary, modifier = Modifier.weight(1f))
                        IconButton(onClick = onClose) { Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextColorSecondary) }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(state.place.type, fontSize = 16.sp, color = TextColorSecondary)
                    Spacer(Modifier.height(16.dp))

                    Text("Endereço:", fontWeight = FontWeight.Bold, color = TextColorPrimary)
                    Text(state.place.address ?: "Não informado", color = TextColorSecondary)
                    Spacer(Modifier.height(8.dp))
                    Text("Telefone:", fontWeight = FontWeight.Bold, color = TextColorPrimary)
                    Text(state.place.phoneNumber ?: "Não informado", color = TextColorSecondary)
                    Spacer(Modifier.height(8.dp))
                    Text("Horário de Funcionamento:", fontWeight = FontWeight.Bold, color = TextColorPrimary)
                    Text(state.place.operatingHours ?: "Não informado", color = TextColorSecondary)
                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = { onRouteClick(state.place) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Traçar Rota", fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
            is LocationDetailsUiState.Error -> Text(state.message, color = Color.Red)
            else -> {}
        }
    }
}