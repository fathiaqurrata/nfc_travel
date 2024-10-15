package com.example.travel

import android.content.res.ColorStateList
import android.graphics.Color
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

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]

        holder.nameTextView.text = member.fullname


        holder.itemView.backgroundTintList = ColorStateList.valueOf(
            when (member.attendanceStatus) {
                "Check In" -> Color.parseColor("#377E4F")
                "Check Out" -> Color.parseColor("#980000")
                else -> Color.TRANSPARENT
            }
        )

        holder.itemView.setOnClickListener { onMemberClick(member) }
    }

    override fun getItemCount() = members.size
}
