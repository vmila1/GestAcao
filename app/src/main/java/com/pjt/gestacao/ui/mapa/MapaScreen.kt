package com.pjt.gestacao.ui.mapa

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.pjt.gestacao.model.Place
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaScreen(mapViewModel: MapViewModel = viewModel()) {
    val uiState by mapViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(uiState.initialLocation, 12f)
    }

    // Efeito para mover a câmera quando a localização do usuário for encontrada
    LaunchedEffect(uiState.userLocation) {
        uiState.userLocation?.let {
            cameraPositionState.animate(
                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(it, 15f)
            )
        }
    }

    Scaffold(
        topBar = {
            MapSearchBar(
                onSearch = { placeType ->
                    mapViewModel.findAndRouteToNearest(placeType)
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true), // Habilita o ponto azul da localização
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            ) {
                // Adiciona os pinos dos locais no mapa
                uiState.nearbyPlaces.forEach { place ->
                    Marker(
                        state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                        title = place.name,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                        onClick = {
                            mapViewModel.onMarkerClicked(place)
                            true
                        }
                    )
                }

                // Desenha a rota no mapa se existir
                if (uiState.routePoints.isNotEmpty()) {
                    Polyline(
                        points = uiState.routePoints,
                        color = Color.Blue,
                        width = 10f
                    )
                }
            }

            // Exibe o BottomSheet se um local for selecionado
            if (uiState.selectedPlaceDetails !is LocationDetailsUiState.Idle) {
                ModalBottomSheet(
                    onDismissRequest = { mapViewModel.dismissModal() },
                    sheetState = sheetState
                ) {
                    LocationDetailsBottomSheet(
                        state = uiState.selectedPlaceDetails,
                        onRouteClick = { place ->
                            // Lança um Intent para o Google Maps traçar a rota
                            val gmmIntentUri = "google.navigation:q=${place.latitude},${place.longitude}".toUri()
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            context.startActivity(mapIntent)
                        },
                        onClose = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    mapViewModel.dismissModal()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapSearchBar(onSearch: (String) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    val placeTypes = listOf("Hospitais", "Postos de Saúde", "ONGs de apoio a gestantes")
    var selectedType by remember { mutableStateOf("") }

    Box(modifier = Modifier.padding(16.dp)) {
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = it }
        ) {
            TextField(
                value = selectedType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Buscar por tipo...") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(8.dp)
            )
            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
            ) {
                placeTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            selectedType = type
                            isExpanded = false
                            onSearch(type) // Chama a função de busca no ViewModel
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LocationDetailsBottomSheet(
    state: LocationDetailsUiState,
    onRouteClick: (Place) -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 250.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is LocationDetailsUiState.Loading -> CircularProgressIndicator()
            is LocationDetailsUiState.Success -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(state.place.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Horário de Funcionamento", fontWeight = FontWeight.Bold)
                    Text(state.place.operatingHours ?: "Não informado")
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { onRouteClick(state.place) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Traçar Rota")
                    }
                }
            }
            is LocationDetailsUiState.Error -> Text(state.message)
            else -> {} // Não mostra nada no estado Idle
        }
    }
}