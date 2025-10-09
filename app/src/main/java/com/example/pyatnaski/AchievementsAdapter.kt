package com.example.pyatnaski

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

data class AchievementItem(val name: String, val isUnlocked: Boolean)

class AchievementsAdapter(context: Context, private val achievements: List<AchievementItem>) :
    ArrayAdapter<AchievementItem>(context, 0, achievements) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_achievement, parent, false)

        val achievement = achievements[position]
        val nameTextView = view.findViewById<TextView>(R.id.textViewAchievementName)
        val checkImageView = view.findViewById<ImageView>(R.id.imageViewAchievementCheck)

        nameTextView.text = achievement.name
        checkImageView.visibility = if (achievement.isUnlocked) View.VISIBLE else View.INVISIBLE

        return view
    }
}
