package com.example.buscar.ui

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.buscar.R
import com.example.buscar.model.Institution

class InstitutionDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // 1) Corrige a extração do ID da intent
        val id = intent.getIntExtra("INST_ID", -1)

        // 2) Cria a lista fixa de instituições com named args corretos
        val institutions = listOf(
            Institution(
                id = 1,
                name = "Mãe Coruja",
                type = "Programa Social Brasileiro",
                distance = "À 20 Km de você",
                logoResId = R.mipmap.ic_launcher_round
            ),
            Institution(
                id = 2,
                name = "Casa da Gestante",
                type = "ONG",
                distance = "À 10 Km de você",
                logoResId = R.mipmap.ic_launcher_round
            ),
            Institution(
                id = 3,
                name = "Centro de Parto Rita Barradas",
                type = "Casa de parto",
                distance = "À 1 Km de você",
                logoResId = R.mipmap.ic_launcher_round
            )
        )

        // 3) Encontra a instituição que corresponde ao ID
        val inst = institutions.firstOrNull { it.id == id }

        // 4) Busca as views com findViewById
        val imgLogo     = findViewById<ImageView>(R.id.imgLogoDetail)
        val tvName      = findViewById<TextView>(R.id.tvNameDetail)
        val tvType      = findViewById<TextView>(R.id.tvTypeDetail)
        val tvDistance  = findViewById<TextView>(R.id.tvDistanceDetail)

        // 5) Preenche os campos se encontrar a instituição
        inst?.let {
            imgLogo.setImageResource(it.logoResId)
            tvName.text     = it.name
            tvType.text     = it.type
            tvDistance.text = it.distance
        }
    }
}
