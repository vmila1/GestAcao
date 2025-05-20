package com.pjt.gestacao.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _texto = MutableLiveData("Bem-vinda!")
    val texto: LiveData<String> = _texto

    fun atualizarMensagem(novaMensagem: String) {
        _texto.value = novaMensagem
    }
}
