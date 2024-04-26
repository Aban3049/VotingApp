package com.example.votingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    private var userType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spalsh)

        firebaseAuth = FirebaseAuth.getInstance()

        Handler().postDelayed({

            if (firebaseAuth.currentUser != null) {

                startActivity(Intent(this@SplashActivity, MainActivity::class.java))

            } else {
                startActivity(Intent(this@SplashActivity, LogInActivity::class.java))
            }

        }, 4000)


    }

}



