package com.example.votingapp

import android.view.Display.Mode
import android.widget.Filter
import java.util.Locale

//used to filter data from recyclerView | search pdf from pdf list in recyclerView
class FilterCandidate : Filter {

    private var filterList:ArrayList<ModelCandidate>

    private var adapterCandidate: adapterCandidate

    constructor(
        filterList: ArrayList<ModelCandidate>,
        adapterCandidate: adapterCandidate
    ) : super() {
        this.filterList = filterList
        this.adapterCandidate = adapterCandidate
    }


    override fun performFiltering(constraint: CharSequence?): FilterResults {

        var constraint = constraint
        val results = FilterResults()

        if (!constraint.isNullOrEmpty()) {
            // if query is either empty or null
            // convert to UpperCase to make query not case sensitive
            constraint = constraint.toString().uppercase(Locale.getDefault())
            // to hold list of filtered ads based on query
            val filteredModels = ArrayList<ModelCandidate>()
            for (i in filterList.indices) {
                //apply filter if query matches to any of brand, category condition, title , then add it to filterModels
                if (
                    filterList[i].name.uppercase(Locale.getDefault()).contains(constraint)
                ) {
                    // query matches to  category add to filtered list

                    filteredModels.add(filterList[i])

                }
            }

            //prepare filtered list and item count
            results.count = filteredModels.size
            results.values = filteredModels


        }

        else{

            //query is either empty or null, prepare original/complete list and item count

            results.count = filterList.size
            results.values = filterList
        }

        return results

    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {

        //apply filter changes
       adapterCandidate.candidateArrayList = results.values as ArrayList<ModelCandidate>

        //notify changes

       adapterCandidate.notifyDataSetChanged()


    }


}