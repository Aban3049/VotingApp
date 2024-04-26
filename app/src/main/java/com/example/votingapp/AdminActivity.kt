package com.example.votingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.votingapp.databinding.ActivityAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding


    private lateinit var candidateArrayList: ArrayList<ModelAdmin>

    //AdapterAd class instance to set ot RecyclerView to show Ads list
    private lateinit var adapterCandidate: AdapterAdmin

    private lateinit var firebaseAuth: FirebaseAuth

    private var candidateId = ""

    private companion object {
        private const val TAG = "LOAD_CANDIDATE_ADMIN_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityAdminBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        firebaseAuth = FirebaseAuth.getInstance()

        try {
            candidateId = intent.getStringExtra("candidateId")!!
        } catch (e: Exception) {

        }

        binding.addCandidate.setOnClickListener {

            startActivity(Intent(this@AdminActivity,AddCandidate::class.java))
        }


        loadCandidates()


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

                    val modelCandidate = ds.getValue(ModelAdmin::class.java)
                    candidateArrayList.add(modelCandidate!!)
                }


                // setup adapter and set to recyclerView
                adapterCandidate = AdapterAdmin(this@AdminActivity, candidateArrayList)
                binding.candidateRv.adapter = adapterCandidate

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed due to ${error.message}")
            }
        })
    }

}