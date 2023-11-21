package com.timemaster.controller

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import com.timemaster.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Menu(private val context: Context) {

    fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.dropdown_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_item1 -> {
                    onItemClick(1)
                    true
                }
                R.id.menu_item2 -> {
                    onItemClick(2)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun onItemClick(item: Int) {
        when (item) {
            1 -> {
                // Code to start ActivityA when item 1 is clicked
                val intent = Intent(context, Account::class.java)
                context.startActivity(intent)
            }
            2 -> {
                // Code to start ActivityB when item 2 is clicked
                Firebase.auth.signOut()
                val intent = Intent(context, Login::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            }
        }
    }
}