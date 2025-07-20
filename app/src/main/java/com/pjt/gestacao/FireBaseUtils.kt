package com.pjt.gestacao

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

object FirebaseUtils {

    fun salvarDadosGestante(
        meses: Int,
        generoBebe: String?,
        latitude: Double,
        longitude: Double,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            onFailure(Exception("Usuário não autenticado."))
            return
        }

        val data = hashMapOf(
            "mesGestacaoInicial" to meses,

            "dataDoCadastro" to FieldValue.serverTimestamp(),

            "generoBebe" to generoBebe,
            "ultimaLocalizacao" to GeoPoint(latitude, longitude)
        )

        db.collection("gestantes").document(userId)
            .set(data)
            .addOnSuccessListener {
                Log.d("FirebaseUtils", "Dados da gestante salvos com sucesso.")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseUtils", "Erro ao salvar dados da gestante", e)
                onFailure(e)
            }
    }

    fun buscarDadosGestante(
        onSuccess: (Map<String, Any>?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            onFailure(Exception("Usuário não autenticado."))
            return
        }

        db.collection("gestantes").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    onSuccess(document.data)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}