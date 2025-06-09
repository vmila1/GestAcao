package com.pjt.gestacao.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pjt.gestacao.R
import com.pjt.gestacao.data.ActionType
import com.pjt.gestacao.data.Message
import com.pjt.gestacao.data.Sender

class MessageAdapter(
    private val messages: MutableList<Message>,
    private val onActionClickListener: (ActionType) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_USER_TEXT = 1
        private const val VIEW_TYPE_AI_TEXT = 2
        private const val VIEW_TYPE_AI_ACTION = 3
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return when {
            message.sender == Sender.USER -> VIEW_TYPE_USER_TEXT
            message.sender == Sender.AI && message.actionType != null -> VIEW_TYPE_AI_ACTION
            message.sender == Sender.AI -> VIEW_TYPE_AI_TEXT
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER_TEXT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_user, parent, false)
                UserMessageViewHolder(view)
            }
            VIEW_TYPE_AI_TEXT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_ai, parent, false)
                AiMessageViewHolder(view)
            }
            VIEW_TYPE_AI_ACTION -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_ai_action, parent, false) // Seu novo layout
                AiActionButtonViewHolder(view, onActionClickListener)
            }
            else -> throw IllegalArgumentException("Invalid view type in onCreateViewHolder")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is UserMessageViewHolder -> holder.bind(message)
            is AiMessageViewHolder -> holder.bind(message)
            is AiActionButtonViewHolder -> holder.bind(message) // Bind para o botão de ação
        }
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    // ViewHolders para User e AI (texto)
    inner class UserMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.textViewMessageUser)
        fun bind(message: Message) {
            messageText.text = message.text
        }
    }

    inner class AiMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.textViewMessageAI)
        fun bind(message: Message) {
            messageText.text = message.text
        }
    }

    // Novo ViewHolder para o botão de ação da IA
    inner class AiActionButtonViewHolder(
        itemView: View,
        private val listener: (ActionType) -> Unit // Recebe o listener
    ) : RecyclerView.ViewHolder(itemView) {
        private val actionButton: Button = itemView.findViewById(R.id.buttonAction) // ID do botão no XML

        fun bind(message: Message) {
            actionButton.text = message.buttonText ?: "Ação"
            message.actionType?.let { type ->
                actionButton.setOnClickListener {
                    listener(type)
                }
            }
        }
    }
}