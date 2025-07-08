package com.pjt.gestacao.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.logging.Log
import com.pjt.gestacao.FirebaseUtils
import com.pjt.gestacao.R

class HomeFragment : Fragment() {

    // Referências para os elementos da UI que serão atualizados
    private lateinit var tvMeses: TextView
    private lateinit var tvMensagem: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa o Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Inicializa as Views
        tvMeses = view.findViewById(R.id.tvMeses)
        tvMensagem = view.findViewById(R.id.tvMensagem)

        setupButtons(view)
        setupMap(view, savedInstanceState)

        // Inicia o processo de verificação de usuário e carregamento de dados
        iniciarSessaoEBuscarDados()
    }

    private fun iniciarSessaoEBuscarDados() {
        if (auth.currentUser == null) {
            // Se não há usuário, realiza o login anônimo
            activity?.let {
                it.setProgressBar(true) // Mostra um loading na tela, se tiver
            }
            auth.signInAnonymously()
                .addOnSuccessListener {
                    Log.d("HomeFragment", "Login anônimo realizado com sucesso. UID: ${it.user?.uid}")
                    // Após o login, busca os dados
                    carregarDadosDaGestante()
                }
                .addOnFailureListener { e ->
                    activity?.let {
                        it.setProgressBar(false)
                    }
                    Log.e("HomeFragment", "Falha no login anônimo", e)
                    Toast.makeText(requireContext(), "Erro de conexão. Verifique sua internet.", Toast.LENGTH_LONG).show()
                }
        } else {
            // Se o usuário já existe de uma sessão anterior, apenas carrega os dados
            Log.d("HomeFragment", "Usuário já logado. UID: ${auth.currentUser?.uid}")
            carregarDadosDaGestante()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun carregarDadosDaGestante() {
        activity?.let {
            it.setProgressBar(true)
        }
        FirebaseUtils.buscarDadosGestante(
            onSuccess = { dados ->
                activity?.let {
                    it.setProgressBar(false)
                }
                if (dados != null) {
                    // Documento encontrado, atualiza a UI
                    val meses = dados["semanasGestacao"] as? Long ?: 0
                    val semanas = (meses * 4).toInt()

                    tvMeses.text = "$meses meses"
                    // Adicione sua lógica para a mensagem aqui...
                    tvMensagem.text = "Bem-vinda de volta!"

                } else {
                    // Documento não encontrado, é o primeiro acesso da usuária.
                    // Você deve navegar para a tela de Onboarding/Cadastro aqui.
                    Toast.makeText(requireContext(), "Bem-vinda! Complete seu cadastro.", Toast.LENGTH_LONG).show()
                    // Exemplo: findNavController().navigate(R.id.action_home_to_onboarding)
                }
            },
            onFailure = { e ->
                activity?.let {
                    it.setProgressBar(false)
                }
                Toast.makeText(requireContext(), "Erro ao buscar dados: ${e.message}", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Funções de setup para organizar o código
    private fun setupButtons(view: View) {
        val botaoMaisInfo: Button = view.findViewById(R.id.btnSaibaMais)
        botaoMaisInfo.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_dashboard)
        }
        val botao1: ImageButton = view.findViewById(R.id.imageButton)
        val botao2: ImageButton = view.findViewById(R.id.imageButton6)
        val botao3: ImageButton = view.findViewById(R.id.imageButton4)
        val botao4: ImageButton = view.findViewById(R.id.imageButton5)
        val botao5: ImageButton = view.findViewById(R.id.imageButton7)

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
    }

    private fun setupMap(view: View, savedInstanceState: Bundle?) {
        val mapView: MapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { googleMap ->
            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-8.0476, -34.8770), 12f))
        }
    }
}