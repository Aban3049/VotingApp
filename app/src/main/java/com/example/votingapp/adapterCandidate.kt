package com.example.votingapp

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.utils.Utils
import com.bumptech.glide.Glide
import com.example.votingapp.databinding.RowCandidatesBinding
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class adapterCandidate : RecyclerView.Adapter<adapterCandidate.HolderCandidate>,Filterable {

    private var context: Context

     var candidateArrayList: ArrayList<ModelCandidate>

    private var filterList: ArrayList<ModelCandidate>


    private var filter: FilterCandidate? = null


    private lateinit var binding: RowCandidatesBinding

    constructor(context: Context, candidateArrayList: ArrayList<ModelCandidate>) {
        this.context = context
        this.candidateArrayList = candidateArrayList
        this.filterList = candidateArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCandidate {
        binding = RowCandidatesBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderCandidate(binding.root)
    }

    override fun getItemCount(): Int {
        return candidateArrayList.size
    }

    override fun onBindViewHolder(holder: HolderCandidate, position: Int) {


        val modelCandidate = candidateArrayList[position]

        val name = modelCandidate.name
        val partyName = modelCandidate.partyName
        val timestamp = modelCandidate.timestamp
        val formattedDate = Utills.formatTimestamp(timestamp)
        val totalVoteCount = modelCandidate.voteCount
        val imageUrl = modelCandidate.profileImageURl





        binding.voteBtn.setOnClickListener {
           val intent = Intent(context,AddVoteActivity::class.java)
            intent.putExtra("candidateId",modelCandidate.id)
            context.startActivity(intent)

        }










    holder.nameTv.text = name
    holder.partyNameTv.text = partyName
    holder.dateTv.text = formattedDate
    holder.totalVoteCount.text = totalVoteCount.toString()

    loadImage(imageUrl,context,binding.imageIv)



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
        Log.e(TAG, "onDataChanged", e)
    }

}


inner class HolderCandidate(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterCandidate(filterList, this)
        }

        return filter as FilterCandidate
    }


}