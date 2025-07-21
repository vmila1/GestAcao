package com.pjt.gestacao.ui.buscar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pjt.gestacao.R
import com.pjt.gestacao.model.Institution

class InstitutionAdapter(
    private var items: List<Institution>,
    private val onClick: (Institution) -> Unit
) : RecyclerView.Adapter<InstitutionAdapter.ViewHolder>() {

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        // 1) Linkando as views do seu item_institution.xml
        private val imgLogo: ImageView      = view.findViewById(R.id.imgLogo)
        private val tvName: TextView        = view.findViewById(R.id.tvName)
        private val tvType: TextView        = view.findViewById(R.id.tvType)
        private val tvDistance: TextView    = view.findViewById(R.id.tvDistance)

        fun bind(inst: Institution) {
            imgLogo.setImageResource(inst.image)
            tvName.text     = inst.name
            tvType.text     = inst.type
            tvDistance.text = inst.address

            view.setOnClickListener { onClick(inst) }
        }
    }

    // 2) Inflando o layout do card
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_institution, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount() = items.size

    // 3) Ligando cada item à posição
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    /** Chama para atualizar a lista e redesenhar o RecyclerView */
    fun updateData(newItems: List<Institution>) {
        items = newItems
        notifyDataSetChanged()
    }

}