package com.example.fittr_app.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fittr_app.R


class UserProfileAdapter(private val userList: List<UserProfile>, private val onClick: (UserProfile) -> Unit) :
    RecyclerView.Adapter<UserProfileAdapter.UserProfileViewHolder>() {

    // ViewHolder class
    class UserProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.userName)
        //val userImage: ImageView = itemView.findViewById(R.id.userView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserProfileViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserProfileViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserProfileViewHolder, position: Int) {
        val userProfile = userList[position]
        holder.userName.text = "TEST"
        //holder.userImage.setImageResource(userProfile.imageResourceId)

        //holder.itemView.setOnClickListener {
          //  onClick(userProfile)
        //}
    }

    override fun getItemCount(): Int = userList.size
}
