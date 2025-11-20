package com.cs407.cadence.ui.screens

import androidx.compose.runtime.Composable
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

@Composable
fun MapScreen() {

    // UW–Madison campus as default position
    val uwMadison = LatLng(43.0731, -89.4012)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(uwMadison, 14f)
    }

    GoogleMap(
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = false),
        uiSettings = MapUiSettings(zoomControlsEnabled = true)
    )
}
