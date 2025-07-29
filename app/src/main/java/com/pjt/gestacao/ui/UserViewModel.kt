package com.pjt.gestacao.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.pjt.gestacao.FirebaseUtils

// Possíveis estados do usuário para a UI reagir durante o fluxo de entrada.
enum class UserState {
    UNKNOWN,          // Estado inicial, aguardando ação.
    AUTHENTICATING,   // Processando login anônimo ou buscando dados.
    NEEDS_ONBOARDING, // Usuário autenticado, mas sem dados no Firestore. Deve ir para o onboarding.
    LOGGED_IN,        // Usuário autenticado e com dados. Pode ir para a Home.
    AUTH_ERROR        // Ocorreu um erro na autenticação ou ao buscar dados.
}

class UserViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // LiveData para controlar o fluxo de entrada (EntryFragment).
    private val _userState = MutableLiveData<UserState>(UserState.UNKNOWN)
    val userState: LiveData<UserState> = _userState

    // LiveData para armazenar os dados da gestante e exibi-los (HomeFragment).
    private val _gestanteData = MutableLiveData<Map<String, Any>?>()
    val gestanteData: LiveData<Map<String, Any>?> = _gestanteData


    // Inicia o processo de login anônimo e verificação de dados.
    fun signInAndProceed() {
        _userState.value = UserState.AUTHENTICATING

        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Novo usuário. Tenta fazer o login anônimo.
            auth.signInAnonymously()
                .addOnSuccessListener {
                    // Após o login, o usuário definitivamente precisa do onboarding.
                    _userState.value = UserState.NEEDS_ONBOARDING
                }
                .addOnFailureListener {
                    _userState.value = UserState.AUTH_ERROR
                }
        } else {
            // Usuário recorrente. Apenas verifica os dados.
            fetchOnboardingStatus()
        }
    }


    // Verifica se os dados do onboarding existem no Firestore.
    private fun fetchOnboardingStatus() {
        FirebaseUtils.buscarDadosGestante(
            onSuccess = { dados ->
                if (dados != null) {
                    _userState.value = UserState.LOGGED_IN
                } else {
                    _userState.value = UserState.NEEDS_ONBOARDING
                }
            },
            onFailure = {
                _userState.value = UserState.AUTH_ERROR
            }
        )
    }


    fun loadGestanteData() {
        FirebaseUtils.buscarDadosGestante(
            onSuccess = { dados ->
                _gestanteData.value = dados
            },
            onFailure = {
                _gestanteData.value = null
            }
        )
    }
}