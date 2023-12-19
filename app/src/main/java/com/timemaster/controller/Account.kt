package com.timemaster.controller

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.timemaster.R
import com.timemaster.model.TaskDbHelper

class Account : AppCompatActivity() {

    private lateinit var taskDbHelper: TaskDbHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account)

        val etProfileEmail = findViewById<EditText>(R.id.et_profile_email)
        val updateBtn = findViewById<Button>(R.id.btn_update_profile)
        val deleteBtn = findViewById<Button>(R.id.btn_delete_profile)
        val resetPassword = findViewById<TextView>(R.id.reset_password)

        val email = FirebaseAuth.getInstance().currentUser!!.email.toString()
        val switchDataSharing = findViewById<Switch>(R.id.switch1)



        // set email field hint
        etProfileEmail.hint = email

        // Click event of update button
        updateBtn.setOnClickListener {
            updateFirebase()
        }

        // Click event of delete button
        deleteBtn.setOnClickListener {

            // profile database handler
            val databaseHandlerProfile = AppUserDatabase(this)

            val builder = AlertDialog.Builder(this)
            //set title for alert dialog
            builder.setTitle("Delete Profile")
            //set message for alert dialog
            builder.setMessage("Are you sure you wants to delete your profile?")
            builder.setIcon(android.R.drawable.ic_dialog_alert)

            //performing positive action
            builder.setPositiveButton("Yes") { dialogInterface, _ ->

                // drop and re-create user table
                databaseHandlerProfile.reCreateTable()

                // dismiss dialogue
                dialogInterface.dismiss()

                // delete firebase user
                deleteFirebase()

                // re direct to login
                startActivity(Intent(this@Account, Login::class.java))
                finish()
            }
            //perform negative action
            builder.setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.dismiss() // Dialog will be dismissed
            }
            // Create the AlertDialog
            val alertDialog: AlertDialog = builder.create()
            // Set other dialog properties
            alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
            alertDialog.show()
        }



        // Click event of password reset button
        resetPassword.setOnClickListener {

            //val email = FirebaseAuth.getInstance().currentUser!!.email.toString()
            Firebase.auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@Account,
                            "An email has been sent successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        // Check if data sharing is enabled
        switchDataSharing.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Retrieve tasks from SQLite database
                val dbHelper = TaskDbHelper(this)
                val tasks = dbHelper.getAllTasks()

                // Synchronize data with Firestore
                dbHelper.addTasksToFirestore(this, tasks, true)
            } else {
                // Data sharing is disabled, remove tasks from Firestore
                val dbHelper = TaskDbHelper(this)

                // Call the function with shareData set to false
                dbHelper.addTasksToFirestore(this, emptyList(), false)
            }
        }

    }

    /**
     * Update firebase email
     */
    private fun updateFirebase() {

        // get elements
        val etProfileEmail = findViewById<EditText>(R.id.et_profile_email)

        // set text of elements
        val newEmail = etProfileEmail.text.toString().trim { it <= ' ' }

        // get current firebase user
        val user = FirebaseAuth.getInstance().currentUser

        // update firebase user email
        user!!.verifyBeforeUpdateEmail("$newEmail")
            .addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    Toast.makeText(applicationContext, "Profile updated",
                        Toast.LENGTH_SHORT)
                        .show()

                    // sign out user
                    FirebaseAuth.getInstance().signOut()

                    // re-direct to login and clear previous activities
                    val intent = Intent(this@Account, Login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
            .addOnFailureListener { e ->
                // Handle failure, e.g., show an error message
                Toast.makeText(
                    applicationContext,
                    "Failed to update email: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    /**
     * delete firebase user
     */
    private fun deleteFirebase() {
        val user = FirebaseAuth.getInstance().currentUser

        user!!.delete()
            .addOnSuccessListener {
                // show message
                Toast.makeText(
                    applicationContext,
                    "Profile deleted successfully.",
                    Toast.LENGTH_LONG
                ).show()

            }
    }
}
