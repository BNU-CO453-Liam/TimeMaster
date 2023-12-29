package com.timemaster.controller

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.timemaster.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * Activity where users can log in to the app
 */
class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // get reference to elements
        val button = findViewById<Button>(R.id.btn_login)
        val entEmail = findViewById<EditText>(R.id.tx_login_email)
        val entPassword = findViewById<EditText>(R.id.tx_login_password)
        val register = findViewById<TextView>(R.id.tv_register)
        val forgotPassword = findViewById<TextView>(R.id.tv_forgot_password)

        // set on click listener
        button.setOnClickListener {
            when {
                TextUtils.isEmpty(entEmail.text.toString().trim { it <= ' '}) -> {
                    Toast.makeText(
                        this@Login,
                        "Please enter email",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(entPassword.text.toString().trim { it <= ' '}) -> {
                    Toast.makeText(
                        this@Login,
                        "Please enter password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    val email: String = entEmail.text.toString().trim { it <= ' ' }
                    val password: String = entPassword.text.toString().trim { it <= ' ' }

                    // Create instance and login user with credentials
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(
                            OnCompleteListener<AuthResult> { task ->

                                // If login is successful
                                if (task.isSuccessful) {

                                    // Firebase user
                                    val user: FirebaseUser = task.result!!.user!!

                                    if (user.isEmailVerified) {
                                        // re-direct to the main screen
                                        val intent = Intent(this@Login, Main::class.java)

                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        intent.putExtra("user_id", FirebaseAuth.getInstance().currentUser!!.uid)
                                        intent.putExtra("email_id", email)
                                        startActivity(intent)
                                        finish()
                                        Toast.makeText(
                                            this@Login,
                                            "You are logged in successfully.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            this@Login,
                                            "Please verify email.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    // If login unsuccessful display error
                                    Toast.makeText(
                                        this@Login,
                                        task.exception!!.message.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                }
            }
        }

        // on click of register button
        register.setOnClickListener {
                startActivity(Intent(this@Login, Register::class.java))
        }

        // Click event of forgot password
        forgotPassword.setOnClickListener {
            showDialog()
        }
    }

    /**
     * Show a dialog for the user to enter email
     */
    private fun showDialog() {

        val builder = AlertDialog.Builder(this)
        val inflater: LayoutInflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.edit_text, null)
        val etEmail = dialogLayout.findViewById<EditText>(R.id.et_editText)

        with(builder) {
            setTitle("Reset Password")
            setPositiveButton("Send") { _, _ ->
                val inputEmail = etEmail.text.toString().trim { it <= ' ' }

                Firebase.auth.sendPasswordResetEmail(inputEmail)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this@Login,
                                "An email has been sent successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@Login,
                                "Invalid email",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
            setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            setView(dialogLayout)
            show()
        }
    }
}