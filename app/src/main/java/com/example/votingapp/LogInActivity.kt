package com.example.votingapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.votingapp.databinding.ActivityLogInBinding
import com.google.firebase.auth.FirebaseAuth


class LogInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog : ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLogInBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog =  ProgressDialog(this)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setMessage("Please wait...")

//        binding.registerBtn.setOnClickListener {
//            startActivity(Intent(this, SignUpActivity::class.java))
//
//        }
        binding.logInBtn.setOnClickListener {
            validateData()
        }

        binding.forgetPassTv.setOnClickListener {
            sendPasswordRecoveryInstructions()
        }

    }

    private var email = ""
    private var password = ""

    private fun validateData() {

        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEt.requestFocus()
            binding.emailEt.error = "invalid Format"
        } else if (email.isEmpty()) {
            binding.emailEt.requestFocus()
            binding.emailEt.error = "Field Required"
        } else if (password.isEmpty()) {
            binding.passwordEt.requestFocus()
            binding.passwordEt.error = "Field Required"
        } else {
            logInUser()
        }

    }

    private fun logInUser() {
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        firebaseAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener {
            Toast.makeText(this, "Logged In Successfully", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this,MainActivity::class.java))
        }.addOnFailureListener {it ->
            Toast.makeText(this, "Failed to LogIn due to ${it.message}", Toast.LENGTH_SHORT).show()
        }


    }

    private fun sendPasswordRecoveryInstructions(){
//        Log.d(TAG,"sendPasswordRecoveryInstructions")

        // show progress
        progressDialog.setMessage("Sending Password reset link to $email")
        progressDialog.show()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEt.requestFocus()
            binding.emailEt.error = "invalid Format"
        } else if (email.isEmpty()) {
            binding.emailEt.requestFocus()
            binding.emailEt.error = "Field Required"

        }
        else{
            firebaseAuth.sendPasswordResetEmail(email).addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Link sent to $email",Toast.LENGTH_LONG).show()
            }.addOnFailureListener {e ->
//            Log.e(TAG,"sendPasswordRecoveryLink: ",e)
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to send due to ${e.message}",Toast.LENGTH_LONG).show()
            }
        }

    }


}