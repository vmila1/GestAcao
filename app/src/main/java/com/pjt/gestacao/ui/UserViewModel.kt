package com.pjt.gestacao.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.pjt.gestacao.FirebaseUtils

// Define os possíveis estados do usuário para a UI reagir
enum class UserState {
    UNKNOWN,          // Estado inicial
    AUTHENTICATING,   // Processando login
    NEEDS_ONBOARDING, // Logado, mas sem dados no Firestore
    LOGGED_IN,        // Logado e com dados
    AUTH_ERROR        // Erro na autenticação
}

class UserViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _userState = MutableLiveData<UserState>(UserState.UNKNOWN)
    val userState: LiveData<UserState> = _userState

    fun checkUserStatus() {
        _userState.value = UserState.AUTHENTICATING

        if (auth.currentUser == null) {
            // 1. Usuário NUNCA usou o app -> Tenta login anônimo
            auth.signInAnonymously()
                .addOnSuccessListener {
                    // Login anônimo bem-sucedido, mas ele ainda não tem dados.
                    _userState.value = UserState.NEEDS_ONBOARDING
                }
                .addOnFailureListener {
                    _userState.value = UserState.AUTH_ERROR
                }
        } else {
            // 2. Usuário JÁ usou o app (tem um UID) -> Verifica se completou o onboarding
            FirebaseUtils.buscarDadosGestante(
                onSuccess = { dados ->
                    if (dados != null) {
                        // Tem dados, pode ir para a Home
                        _userState.value = UserState.LOGGED_IN
                    } else {
                        // Não tem dados, precisa fazer o onboarding
                        _userState.value = UserState.NEEDS_ONBOARDING
                    }
                },
                onFailure = {
                    // Erro ao buscar no Firestore (ex: sem internet)
                    _userState.value = UserState.AUTH_ERROR
                }
            )
        }
    }
    // LiveData para armazenar os dados da gestante
    private val _gestanteData = MutableLiveData<Map<String, Any>?>()
    val gestanteData: LiveData<Map<String, Any>?> = _gestanteData

    // LiveData para controlar o estado de "carregando"
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Pede ao FirebaseUtils para carregar os dados do Firestore.
     * O resultado é postado nos LiveData.
     */
    fun loadGestanteData() {
        _isLoading.value = true
        FirebaseUtils.buscarDadosGestante(
            onSuccess = { dados ->
                _gestanteData.value = dados
                _isLoading.value = false
            },
            onFailure = {
                // Em caso de falha, informa que não há dados e para de carregar
                _gestanteData.value = null
                _isLoading.value = false
            }
        )
    }
}