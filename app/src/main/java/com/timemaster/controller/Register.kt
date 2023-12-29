package com.timemaster.controller

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.timemaster.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.timemaster.model.AppUser


class Register : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        // get reference to elements
        val button = findViewById<Button>(R.id.btn_register)
        val entEmail = findViewById<EditText>(R.id.tx_register_email)
        val entPassword = findViewById<EditText>(R.id.tx_register_password)
        val login = findViewById<TextView>(R.id.tv_login)

        // set on click listener for register button
        button.setOnClickListener {
            when {
                TextUtils.isEmpty(entEmail.text.toString().trim { it <= ' '}) -> {
                    Toast.makeText(
                        this@Register,
                        "Please enter email",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(entPassword.text.toString().trim { it <= ' '}) -> {
                    Toast.makeText(
                        this@Register,
                        "Please enter password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    //addProfile()
                    registerUser()
                }
            }
        }

        // set on click listener to login text
        login.setOnClickListener {
            startActivity(Intent(this@Register, Login::class.java))
        }
    }

    /**
     * Create instance and register user with credentials to online database
     */
    private fun registerUser() {
        // get elements
        val entEmail = findViewById<EditText>(R.id.tx_register_email)
        val entPassword = findViewById<EditText>(R.id.tx_register_password)

        // set values to user input
        val email: String = entEmail.text.toString().trim { it <= ' ' }
        val password: String = entPassword.text.toString().trim { it <= ' ' }

        // Create a new user with Firebase Authentication
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                // If registration is successful
                if (task.isSuccessful) {
                    // Firebase registered user
                    val firebaseUser: FirebaseUser = task.result!!.user!!

                    // Send email verification
                    firebaseUser.sendEmailVerification()

                    // success alert
                    Toast.makeText(
                        this@Register,
                        "Success. A verification email has been sent.",
                        Toast.LENGTH_SHORT
                    ).show()

                    // You can add code here to navigate to another activity or perform additional actions
                    val intent = Intent(this@Register, Login::class.java)

                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    intent.putExtra("user_id", FirebaseAuth.getInstance().currentUser!!.uid)
                    intent.putExtra("email_id", email)
                    startActivity(intent)
                    finish()

                } else {
                    // If registration is unsuccessful, display an error message
                    Toast.makeText(
                        this@Register,
                        "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    /**
     * Add user profile to local database
     */
    private fun addProfile() {

        val databaseController = AppUserDatabase(this)

        // get elements
        val entEmail = findViewById<EditText>(R.id.tx_register_email)

        // set values to user input
        val profileUserName = entEmail.text.toString().trim()

        // if fields are not empty, add profile to local database
        if (profileUserName.isNotEmpty()) {
            val status =
                databaseController.addProfile(AppUser(0,profileUserName))
            if (status > -1) {

                // start new activity and clear old activity of main task
                val intent = Intent(this, Main::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
        } else {
            // show message
            Toast.makeText(
                applicationContext,
                "Email or password cannot be blank",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
