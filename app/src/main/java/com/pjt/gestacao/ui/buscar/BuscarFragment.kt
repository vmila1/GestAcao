package com.pjt.gestacao.ui.buscar

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.pjt.gestacao.R
import com.pjt.gestacao.databinding.FragmentBuscarBinding
import com.pjt.gestacao.model.Institution
import com.pjt.gestacao.ui.buscar.InstitutionAdapter
import com.pjt.gestacao.ui.InstitutionDetailActivity
import android.widget.AdapterView


class BuscarFragment : Fragment() {

    private var _binding: FragmentBuscarBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: InstitutionAdapter

    private val institutions = listOf(
        Institution(1, "Mãe Coruja", "Programa Social", "À 20 Km de você", R.mipmap.ic_launcher_round),
        Institution(2, "Casa da Gestante", "ONG", "À 10 Km de você", R.mipmap.ic_launcher_round),
        Institution(3, "Centro de Parto Rita Barradas", "Casa de parto", "À 1 Km de você", R.mipmap.ic_launcher_round),
        Institution(4, "Bons Samaritanos", "Casa de parto", "À 1 Km de você", R.mipmap.ic_launcher_round),
        Institution(5, "ONG Serra Calhada", "ONG", "À 1 Km de você", R.mipmap.ic_launcher_round),
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBuscarBinding.inflate(inflater, container, false)
        val view = binding.root

        // Configurar RecyclerView
        adapter = InstitutionAdapter(institutions) { inst ->
            val intent = Intent(requireContext(), InstitutionDetailActivity::class.java)
            intent.putExtra("INST_ID", inst.id)
            startActivity(intent)
        }

        binding.rvInstitutions.layoutManager = LinearLayoutManager(context)
        binding.rvInstitutions.adapter = adapter

        // Configurar Spinner
        val tipos = listOf("Todos") + institutions.map { it.type }.distinct()
        val spinner = binding.spinnerFilter
        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tipos)

        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val filtro = parent.getItemAtPosition(position).toString()
                val query = binding.searchBar.text.toString()
                applyFilters(query, filtro)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        // Listener do Search
        binding.searchBar.addTextChangedListener { text ->
            val filtro = spinner.selectedItem.toString()
            applyFilters(text.toString(), filtro)
        }

        return view
    }

    private fun applyFilters(query: String, filtro: String) {
        val filtered = institutions.filter {
            it.name.contains(query, ignoreCase = true) && (filtro == "Todos" || it.type == filtro)
        }
        adapter.updateData(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
