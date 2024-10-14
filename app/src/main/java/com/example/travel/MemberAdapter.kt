package com.example.travel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MemberAdapter(
    private val members: List<Member>,
    private val onMemberClick: (Member) -> Unit
) : RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    class MemberViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.member_name)
        val seatTextView: TextView = view.findViewById(R.id.member_seat)
        val attendanceTextView: TextView = view.findViewById(R.id.member_attendance) // Tambahkan TextView untuk status
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        holder.nameTextView.text = member.fullname
        holder.seatTextView.text = member.seat
        holder.attendanceTextView.text = member.attendanceStatus // Tampilkan status kehadiran
        holder.itemView.setOnClickListener { onMemberClick(member) }
    }

    override fun getItemCount() = members.size
}

