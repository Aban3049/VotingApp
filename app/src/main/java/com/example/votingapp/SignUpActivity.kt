package com.example.votingapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.votingapp.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog


    private companion object {
        private const val TAG = "SIGN_UP_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivitySignUpBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setMessage("Please Wait...")

        binding.SignUpBtn.setOnClickListener {
            validateData()
        }
        binding.logInTv.setOnClickListener {
            startActivity(Intent(this, LogInActivity::class.java))
        }
//        binding.verifyEmailTv.setOnClickListener {
//            verifyAccount()
//        }
    }

    private var email = ""
    private var password = ""
    private var name = ""
    private var cnic = ""

    private fun validateData() {

        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        name = binding.nameEt.text.toString().trim()
        cnic = binding.cnicEt.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEt.error = "Invalid Email Pattern"
            binding.emailEt.requestFocus()
        } else if (password.isEmpty()) {
            binding.passwordEt.error = "Enter Password"
            binding.passwordEt.requestFocus()
        } else if (cnic.isEmpty()) {
            binding.cnicEt.error = "Invalid Field"
            binding.cnicEt.requestFocus()
        } else if (cnic.length < 13) {
            binding.cnicEt.error = "Invalid Field"
            binding.cnicEt.requestFocus()
        } else if (cnic.length > 13) {
            binding.cnicEt.error = "Invalid Field"
            binding.cnicEt.requestFocus()
        } else if (name.isEmpty()) {
            binding.nameEt.error = "Field Required"
            binding.nameEt.requestFocus()
        } else {
            registerUser()
        }

    }

    private fun registerUser() {
        progressDialog.setMessage("Creating Account")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                progressDialog.setMessage("Registered Successfully")
                Toast.makeText(this, "Registered Success", Toast.LENGTH_LONG).show()
                updateUserInfo()
                Log.d(TAG, "register user: Register Success")

            }.addOnFailureListener { e ->
                Log.e(TAG, "register user $e")
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed to create Account due to ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

    }


    //
    private fun updateUserInfo() {
        Log.d(TAG, "updateUserInfo")
        progressDialog.setMessage("Saving User Info")

        val timeStamp = System.currentTimeMillis()
        val registerUserEmail = firebaseAuth.currentUser!!.email
        val registeredUserUid = firebaseAuth.uid

        val hashMap = HashMap<String, Any>()
        hashMap["name"] = "$name"
        hashMap["profileImageURl"] = ""
        hashMap["userType"] = "Email"
        hashMap["timestamp"] = timeStamp
        hashMap["email"] = "$registerUserEmail"
        hashMap["uid"] = "$registeredUserUid"
        hashMap["cnic"] = "$cnic"
        hashMap["userType"] = "user"
        hashMap["voteGiven"] = "0"

        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(registeredUserUid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateUserInfo: User registered...")
                progressDialog.setMessage("Uploading Info...")
                // Start MainActivity
                startActivity(Intent(this, LogInActivity::class.java))

            }.addOnFailureListener { e ->
                Log.e(TAG, "updateUserInfo", e)
                Toast.makeText(
                    this,
                    "Failed to save user info due to ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }


    }

//    private fun verifyAccount() {
//        Log.d(TAG, "verifying Account: ")
//
//        try {
//            progressDialog.setMessage("Sending verification link to email:")
//            progressDialog.show()
//            firebaseAuth.currentUser!!.sendEmailVerification().addOnSuccessListener {
//                Log.d(TAG, "Account Verification link: sent successfully")
//                progressDialog.dismiss()
//
//
//            }.addOnFailureListener { e ->
//                Log.e(TAG, "Failed to send verification link:", e)
//                progressDialog.dismiss()
//                Toast.makeText(
//                    this,
//                    "Failed to send verification link due to ${e.message}", Toast.LENGTH_LONG
//                )
//
//            }
//        } catch (e: Exception) {
//          Toast.makeText(this,"Failed due to ${e.message}",Toast.LENGTH_LONG)
//
//        }
//
//
//    }


}