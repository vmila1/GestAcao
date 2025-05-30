package com.pjt.gestacao.adapter // Certifique-se de que este é o pacote correto!

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pjt.gestacao.R // Importa a classe R do seu projeto principal
import com.pjt.gestacao.data.Message // Importa sua data class Message
import com.pjt.gestacao.data.Sender  // Importa seu enum Sender

class MessageAdapter(private val messages: MutableList<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Constantes para os tipos de view, dentro de um companion object
    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_AI = 2
    }

    override fun getItemViewType(position: Int): Int {
        // Determina se a mensagem é do usuário ou da IA para inflar o layout correto
        return if (messages[position].sender == Sender.USER) {
            VIEW_TYPE_USER
        } else {
            VIEW_TYPE_AI
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // Infla o layout apropriado (usuário ou IA) com base no viewType
        return if (viewType == VIEW_TYPE_USER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_user, parent, false) // Layout para mensagem do usuário
            UserMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_ai, parent, false) // Layout para mensagem da IA
            AiMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        // Vincula os dados da mensagem ao ViewHolder correto
        if (holder.itemViewType == VIEW_TYPE_USER) {
            (holder as UserMessageViewHolder).bind(message)
        } else {
            (holder as AiMessageViewHolder).bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    // Função para adicionar uma nova mensagem à lista e notificar o adapter
    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1) // Notifica que um item foi inserido no final
    }

    // ViewHolder para as mensagens do usuário
    inner class UserMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Assume que seu item_message_user.xml tem um TextView com id textViewMessageUser
        private val messageText: TextView = itemView.findViewById(R.id.textViewMessageUser)

        fun bind(message: Message) {
            messageText.text = message.text
        }
    }

    // ViewHolder para as mensagens da IA
    inner class AiMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Assume que seu item_message_ai.xml tem um TextView com id textViewMessageAI
        private val messageText: TextView = itemView.findViewById(R.id.textViewMessageAI)

        fun bind(message: Message) {
            messageText.text = message.text
        }
    }
}