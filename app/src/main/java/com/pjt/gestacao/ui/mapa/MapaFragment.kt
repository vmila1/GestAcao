package com.pjt.gestacao.ui.mapa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.pjt.gestacao.ui.theme.GestAcaoTheme

class MapaFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Cria uma ComposeView, que é a ponte entre o sistema de Views (XML) e o Jetpack Compose.
        return ComposeView(requireContext()).apply {
            // Define o conteúdo da UI para esta ComposeView.
            setContent {
                // Aplica o tema geral do seu aplicativo ao conteúdo do Compose.
                GestAcaoTheme {
                    // Chama a função @Composable principal que constrói a tela do mapa.
                    MapaScreen()
                }
            }
        }
    }
}