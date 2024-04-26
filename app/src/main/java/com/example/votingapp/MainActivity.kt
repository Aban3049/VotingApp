package com.example.votingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.votingapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding

    private lateinit var candidateArrayList: ArrayList<ModelCandidate>

    //AdapterAd class instance to set ot RecyclerView to show Ads list
    private lateinit var adapterCandidate: adapterCandidate

    private lateinit var firebaseAuth: FirebaseAuth

    private var candidateId = ""

    private companion object {
        private const val TAG = "LOAD_CANDIDATE_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityMainBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        try {
            candidateId = intent.getStringExtra("candidateId")!!
        } catch (e: Exception) {

        }

        Handler().postDelayed({
            binding.progressBar.visibility = View.GONE

        }, 3000)



        checkUser()
        loadCandidates()

        binding.addCandidate.setOnClickListener {
            startActivity(Intent(this@MainActivity,AddCandidate::class.java))
        }

        binding.searchEt.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                // call the filter when user start writing

                try {

                   adapterCandidate.filter.filter(s)

                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Failed to search due to ${e.message}", Toast.LENGTH_SHORT).show()
                }


            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })


    }


    private fun loadCandidates() {
        Log.d(TAG, "loadCandidates: ")

        candidateArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Candidates")
        ref.child(candidateId)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                candidateArrayList.clear()

                for (ds in snapshot.children) {

                    val modelCandidate = ds.getValue(ModelCandidate::class.java)
                    candidateArrayList.add(modelCandidate!!)
                }


                // setup adapter and set to recyclerView
                adapterCandidate = adapterCandidate(this@MainActivity, candidateArrayList)
                binding.candidateRv.adapter = adapterCandidate

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed due to ${error.message}")
            }
        })
    }

    private fun checkUser() {

        val uid = firebaseAuth.currentUser!!.uid

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userType = "${snapshot.child("userType").value}"
                    val profileImageUrl = "${snapshot.child("profileImageURl").value}"
                    Log.d(TAG, "onDataChange: $userType")
                    Log.d(TAG, "onDataChange: firebaseUid: $firebaseAuth")

                    if (userType == "user") {
                        binding.addCandidate.visibility = View.GONE
                    } else if (userType == "admin") {
                        binding.addCandidate.visibility = View.VISIBLE
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

    }


}