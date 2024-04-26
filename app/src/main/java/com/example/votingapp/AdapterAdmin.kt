package com.example.votingapp

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.votingapp.databinding.RowAdminBinding
import com.example.votingapp.databinding.RowCandidatesBinding
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterAdmin : RecyclerView.Adapter<AdapterAdmin.HolderAdminCandidate> {


    private var context: Context

    private var adminArrayList: ArrayList<ModelAdmin>


    private lateinit var binding: RowAdminBinding

    constructor(context: Context, adminArrayList: ArrayList<ModelAdmin>) {
        this.context = context
        this.adminArrayList = adminArrayList
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdapterAdmin.HolderAdminCandidate {
        binding = RowAdminBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderAdminCandidate(binding.root)
    }

    override fun onBindViewHolder(holder: HolderAdminCandidate, position: Int) {


        val modelCandidate = adminArrayList[position]

        val name = modelCandidate.name
        val partyName = modelCandidate.partyName
        val timestamp = modelCandidate.timestamp
        val formattedDate = Utills.formatTimestamp(timestamp)
        val totalVoteCount = modelCandidate.voteCount
        val imageUrl = modelCandidate.profileImageURl





        binding.voteBtn.setOnClickListener {
            val intent = Intent(context, AddVoteActivity::class.java)
            intent.putExtra("candidateId", modelCandidate.id)
            context.startActivity(intent)

        }


        binding.deleteBtn.setOnClickListener {
            val ref = FirebaseDatabase.getInstance().getReference("Candidates")
            ref.child(modelCandidate.id).removeValue().addOnSuccessListener {
                Toast.makeText(context, "Candidate Deleted Successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to delete due to ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }








        holder.nameTv.text = name
        holder.partyNameTv.text = partyName
        holder.dateTv.text = formattedDate
        holder.totalVoteCount.text = totalVoteCount.toString()

        loadImage(imageUrl, context, binding.imageIv)


    }


    override fun getItemCount(): Int {
        return adminArrayList.size
    }


    private lateinit var firebaseAuth: FirebaseAuth


    private fun loadImage(
        imageUrl: String,
        context: Context,
        imageView: ShapeableImageView
    ) {
        //using url we can get image
        try {

            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_person_white)
                .into(imageView)


        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "onDataChanged", e)
        }

    }


    inner class HolderAdminCandidate(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //init row_candidate

        var imageIv = binding.imageIv
        var nameTv = binding.nameTv
        var partyNameTv = binding.partyNameTv
        var dateTv = binding.dateTv
        var voteBtn = binding.voteBtn
        var deleteBtn = binding.deleteBtn
        var totalVoteCount = binding.totalVotesTv
        var votesTv = binding.votesTv


    }

}