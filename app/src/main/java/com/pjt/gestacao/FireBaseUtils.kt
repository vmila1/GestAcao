package com.pjt.gestacao

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

// Renomeado para seguir a convenção do Kotlin (objeto singleton)
object FirebaseUtils {

    // A função getDeviceId foi removida, pois agora usaremos o UID do Firebase Auth.

    /**
     * Salva os dados iniciais da gestante durante o onboarding.
     * Usa o UID do usuário anônimo logado como ID do documento.
     */
    fun salvarDadosGestante(
        semanas: Int,
        generoBebe: String?,
        latitude: Double,
        longitude: Double,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        // Pega o UID do usuário anônimo atualmente logado.
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Se o userId for nulo, significa que algo deu errado com o login anônimo.
        if (userId == null) {
            onFailure(Exception("Usuário não autenticado. Não é possível salvar os dados."))
            return
        }

        val data = hashMapOf(
            // Não precisamos mais salvar o "uuid" dentro do documento,
            // pois o ID do documento JÁ é o UID do usuário.
            "semanasGestacao" to semanas,
            "generoBebe" to generoBebe,
            "primeiroAcesso" to System.currentTimeMillis(),
            "ultimaLocalizacao" to GeoPoint(latitude, longitude)
        )

        db.collection("gestantes").document(userId)
            .set(data)
            .addOnSuccessListener {
                Log.d("FirebaseUtils", "Dados da gestante salvos com sucesso para o usuário: $userId")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseUtils", "Erro ao salvar dados da gestante", e)
                onFailure(e)
            }
    }

    /**
     * Atualiza os dados da gestante.
     */
    fun atualizarDadosGestante(
        novosDados: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            onFailure(Exception("Usuário não autenticado."))
            return
        }

        db.collection("gestantes").document(userId)
            .update(novosDados)
            .addOnSuccessListener {
                Log.d("FirebaseUtils", "Dados da gestante atualizados com sucesso")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseUtils", "Erro ao atualizar dados da gestante", e)
                onFailure(e)
            }
    }

    /**
     * Busca os dados da gestante do usuário logado.
     */
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
                    Log.d("FirebaseUtils", "Documento encontrado para o usuário: $userId")
                    onSuccess(document.data)
                } else {
                    Log.w("FirebaseUtils", "Nenhum documento encontrado para o usuário: $userId. Provavelmente é o primeiro acesso.")
                    // Retorna nulo para indicar que o documento não existe (e o onboarding deve ser iniciado).
                    onSuccess(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseUtils", "Erro ao buscar dados da gestante", e)
                onFailure(e)
            }
    }
}