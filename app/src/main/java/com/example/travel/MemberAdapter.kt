package com.example.travel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MemberAdapter(private val memberList: List<Member>) : RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fullnameTextView: TextView = itemView.findViewById(R.id.fullname_text_view)
        val phoneTextView: TextView = itemView.findViewById(R.id.phone_text_view)
        val seatTextView: TextView = itemView.findViewById(R.id.seat_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = memberList[position]
        holder.fullnameTextView.text = member.fullname
        holder.phoneTextView.text = "Phone: ${member.phone}"
        holder.seatTextView.text = "Seat: ${member.seat}"
    }

    override fun getItemCount() = memberList.size
}
