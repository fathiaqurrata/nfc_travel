package com.example.travel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StatusAdapter(private val statusList: List<Status>) : RecyclerView.Adapter<StatusAdapter.StatusViewHolder>() {

    class StatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.name_text_view)
        val statusTextView: TextView = itemView.findViewById(R.id.status_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_status, parent, false)
        return StatusViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        val status = statusList[position]
        holder.nameTextView.text = status.fullname
        holder.statusTextView.text = status.checkInStatus
    }

    override fun getItemCount(): Int {
        return statusList.size
    }
}
