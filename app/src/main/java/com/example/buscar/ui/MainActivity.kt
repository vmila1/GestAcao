package com.example.buscar.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.buscar.R
import com.example.buscar.model.Institution
import com.example.buscar.ui.InstitutionAdapter
import com.example.buscar.ui.InstitutionDetailActivity

class MainActivity : AppCompatActivity() {

    // Exemplo de dados fixos
    private val institutions = listOf(
        Institution(
            id = 1,
            name = "Mãe Coruja",
            type = "Programa Social",
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
        ),
        Institution(
            id = 4,
            name = "Bons Samaritanos",
            type = "Casa de parto",
            distance = "À 1 Km de você",
            logoResId = R.mipmap.ic_launcher_round
        ),
        Institution(
            id = 5,
            name = "ONG Serra Calhada",
            type = "ONG",
            distance = "À 1 Km de você",
            logoResId = R.mipmap.ic_launcher_round
        )
    )

    private lateinit var adapter: InstitutionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1) Configura o RecyclerView
        val rv = findViewById<RecyclerView>(R.id.rvInstitutions)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = InstitutionAdapter(institutions) { inst ->
            // Ao clicar num card, abre a tela de detalhe
            val intent = Intent(this, InstitutionDetailActivity::class.java)
            intent.putExtra("INST_ID", inst.id)
            startActivity(intent)
        }
        rv.adapter = adapter

        // 2) Prepara o Spinner de filtro ("Todos" + tipos únicos)
        val tipos = listOf("Todos") + institutions.map { it.type }.distinct()
        val spinner = findViewById<Spinner>(R.id.spinnerFilter)
        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            tipos
        )

        // 3) Reage à seleção do filtro
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                // sempre passe a query atual e o filtro escolhido
                val query = findViewById<EditText>(R.id.searchBar).text.toString()
                val filtro = tipos[position]
                applyFilters(query, filtro)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 4) Reage à digitação na searchBar
        findViewById<EditText>(R.id.searchBar)
            .addTextChangedListener { text ->
                val filtro = spinner.selectedItem as String
                applyFilters(text.toString(), filtro)
            }
    }

    /**
     * Filtra por nome **e** tipo, atualiza o adapter
     */
    private fun applyFilters(query: String, filtro: String) {
        val filtered = institutions.filter { inst ->
            val matchesName = inst.name.contains(query, ignoreCase = true)
            val matchesType = (filtro == "Todos" || inst.type == filtro)
            matchesName && matchesType
        }
        adapter.updateData(filtered)
    }
}
