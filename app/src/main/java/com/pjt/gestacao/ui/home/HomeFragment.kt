package com.pjt.gestacao.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.pjt.gestacao.R

class HomeFragment : Fragment() {

    // O aviso "viewModel is never used" pode ser ignorado por enquanto
    // ou você pode remover a linha se não for usar o viewModel.
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val botao1: ImageButton = view.findViewById(R.id.imageButton)
        val botao2: ImageButton = view.findViewById(R.id.imageButton6)
        val botao3: ImageButton = view.findViewById(R.id.imageButton4)
        val botao4: ImageButton = view.findViewById(R.id.imageButton5)
        val botao5: ImageButton = view.findViewById(R.id.imageButton7)
        val botaoMaisInfo: Button = view.findViewById(R.id.btnSaibaMais)

        botaoMaisInfo.setOnClickListener {
            // Esta linha agora funcionará após você editar o mobile_navigation.xml
            findNavController().navigate(R.id.action_navigation_home_to_navigation_dashboard)
        }

        botao1.setOnClickListener {
            Toast.makeText(requireContext(), "Botão 1 clicado!", Toast.LENGTH_SHORT).show()
        }

        botao2.setOnClickListener {
            Toast.makeText(requireContext(), "Botão 2 clicado!", Toast.LENGTH_SHORT).show()
        }

        botao3.setOnClickListener {
            Toast.makeText(requireContext(), "Botão 3 clicado!", Toast.LENGTH_SHORT).show()
        }

        botao4.setOnClickListener {
            Toast.makeText(requireContext(), "Botão 4 clicado!", Toast.LENGTH_SHORT).show()
        }

        botao5.setOnClickListener {
            Toast.makeText(requireContext(), "Botão 5 clicado!", Toast.LENGTH_SHORT).show()
        }

        // Mapa com a indentação corrigida
        val mapView: MapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { googleMap ->
            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-8.0476, -34.8770), 12f))
        }
    }
}