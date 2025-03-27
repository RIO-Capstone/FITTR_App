package com.example.fittr_app.ui.profile

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fittr_app.R
import com.example.fittr_app.ui.auth.AuthActivity
import com.example.fittr_app.ui.registration.RegistrationActivity


class UserProfileAdapter(private val userList: List<UserProfile>, private val onClick: (UserProfile) -> Unit) : RecyclerView.Adapter<UserProfileAdapter.UserProfileViewHolder>() {

    // ViewHolder class
    class UserProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.userName)
        val userImage: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun getItemViewType(position: Int): Int {
        return if (userList[position].isAddUserButton) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserProfileViewHolder {
        val itemView = if (viewType == 1) {
            LayoutInflater.from(parent.context).inflate(R.layout.item_add_user, parent, false)
        } else {
            LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        }
        return UserProfileViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserProfileViewHolder, position: Int) {
        val userProfile = userList[position]

        if (userProfile.isAddUserButton) {
            holder.userName.text = "Add User"
            holder.userImage.setImageResource(R.drawable.ic_add_user) // Use an add icon
        } else {
            holder.userName.text = userProfile.name
            holder.userImage.setImageResource(userProfile.imageResourceId)
        }

        holder.itemView.setOnClickListener {
            if (userProfile.isAddUserButton) {
                val context = holder.itemView.context
                val intent = Intent(context, RegistrationActivity::class.java)
                context.startActivity(intent)
            } else {
                onClick(userProfile)
            }
        }
    }


    override fun getItemCount(): Int = userList.size
}
