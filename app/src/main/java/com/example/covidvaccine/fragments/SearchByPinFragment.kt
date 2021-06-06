package com.example.covidvaccine.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.covidvaccine.App
import com.example.covidvaccine.R
import com.example.covidvaccine.adapters.SessionsAdapter
import com.example.covidvaccine.models.Session
import com.example.covidvaccine.models.Success
import com.example.covidvaccine.networking.NetworkStatusChecker
import kotlinx.android.synthetic.main.fragment_search_by_pin.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager

import android.widget.TextView.OnEditorActionListener
import com.example.covidvaccine.models.Failure
import com.example.covidvaccine.models.FailureString
import java.io.IOException

import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import com.example.covidvaccine.utils.getTodayDate
import com.example.covidvaccine.utils.getTomorrowDate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Collections.emptyList


@RequiresApi(Build.VERSION_CODES.M)
class SearchByPinFragment : Fragment() {

    private var pincodes : List<String> = listOf()
    private lateinit var adapter: ArrayAdapter<String>
    private var tempSessionsList: List<Session> = listOf()
    private var finalSessionsList: List<Session> = listOf()
    private lateinit var sessionsAdapter: SessionsAdapter
    private var doseCheckedItem=-1
    private var ageCheckedItem=-1
    private val remoteApi = App.remoteApi
    private val networkStatusChecker by lazy {
        context?.let { NetworkStatusChecker(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pincodes = App.getRecentSearches()
        setUpSuggestionsAdapter()

        if(networkStatusChecker?.isConnectedToInternet() == true) {
            showTextView("Start a new search", false)
        }else{
            showTextView("No Internet Connection", false)
        }

        sessionsAdapter=SessionsAdapter(activity,finalSessionsList)
        recyclerViewPin.layoutManager=LinearLayoutManager(activity)
        recyclerViewPin.adapter=sessionsAdapter

        setUpClickListeners()

        edittext.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                fetchDataFromApiAndShow(false)
                return@OnEditorActionListener true
            }
            false
        })

    }

    private fun setUpSuggestionsAdapter() {
        adapter= context?.let { ArrayAdapter(it,R.layout.search_term,App.getRecentSearches()) }!!
        edittext.threshold = 1 //will start working from first character
        edittext.setAdapter(adapter) //setting the adapter data into the AutoCompleteTextView
        edittext.setDropDownBackgroundDrawable(context?.let { ContextCompat.getDrawable(it,R.drawable.background_form) })
        edittext.setOnItemClickListener { _, _, _, _ ->
            fetchDataFromApiAndShow(false)
        }
    }

    private fun fetchDataFromApiAndShow(fromSearch: Boolean) {
        val pincode = edittext.text.toString()
        if(networkStatusChecker?.isConnectedToInternet() == true){
            if(pincode.isNotEmpty() && pincode.length==6) {

                pincodes = pincodes + pincode
                App.setRecentSearches(pincodes.distinct())
                setUpSuggestionsAdapter()

                showProgressBar()
                hideKeyboard()
                edittext.dismissDropDown()

                GlobalScope.launch(Dispatchers.Main) {
                    val todayResults = remoteApi.getSessionsByPin(pincode,getTodayDate())
                    val tomorrowResults = remoteApi.getSessionsByPin(pincode, getTomorrowDate())
                    progressBarPin.visibility= GONE
                    if(!fromSearch) {
                        resetFilters()
                    }
                    if (todayResults is Success && tomorrowResults is Success) {
                        val list=getFinalSessionsList(todayResults,tomorrowResults,fromSearch);
                        if(list.isNotEmpty()) {
                            sessionsAdapter.setSessions(list)
                            sessionsAdapter.notifyDataSetChanged()
                        }else{
                            showTextView("No Data Found",fromSearch)
                        }
                    }else if (todayResults is Success) {
                        val list=getFinalSessionsList(todayResults,null,fromSearch);
                        if(list.isNotEmpty()) {
                            sessionsAdapter.setSessions(list)
                            sessionsAdapter.notifyDataSetChanged()
                        }else{
                            showTextView("No Data Found",fromSearch)
                        }
                    } else if (tomorrowResults is Success) {
                        val list=getFinalSessionsList(null,tomorrowResults,fromSearch);
                        if(list.isNotEmpty()) {
                            sessionsAdapter.setSessions(list)
                            sessionsAdapter.notifyDataSetChanged()
                        }else{
                            showTextView("No Data Found",fromSearch)
                        }
                    }else if(todayResults is FailureString){
                        showTextView(todayResults.errorstring, false);
                    }else if(todayResults is Failure){
                        if((todayResults.error) is IOException) {
                            showTextView("No Internet Connection", false)
                        }else{
                            showTextView("Some error occured.Please try again", false)
                        }
                    }
                }
            }else{
                //if pincode length is smaller than 6 or empty
            }
        }else{
            showTextView("No Internet Connection", false)
        }
    }

    private fun resetFilters() {
        dosePinFilter.text="Dose"
        agePinFilter.text="Age"
         doseCheckedItem=-1
         ageCheckedItem=-1
    }

    private fun getFinalSessionsList(
        todayResults: Success<List<Session>>?,
        tomorrowResults: Success<List<Session>>?,
        fromSearch: Boolean
    ): List<Session> {
        var todayAvailableSessionsList:List<Session> = emptyList()
        var todayNonAvailableSessionsList:List<Session> = emptyList()
        var tomorrowAvailableSessionsList:List<Session> = emptyList()
        var tomorrowNonAvailableSessionsList:List<Session> = emptyList()

        showRecyclerView()

        if (todayResults != null) {
            tempSessionsList = todayResults.data
            for (session in tempSessionsList) {
                if (session.fee_type == "Paid" && session.fee == "0") {
                    tempSessionsList = tempSessionsList - session
                }
            }

            for (session in tempSessionsList) {
                if (session.available_capacity > 0) {
                    todayAvailableSessionsList = todayAvailableSessionsList + session
                } else {
                    todayNonAvailableSessionsList = todayNonAvailableSessionsList + session
                }
            }
        }

        if (tomorrowResults != null) {
            tempSessionsList = tomorrowResults.data
            for (session in tempSessionsList) {
                if (session.fee_type == "Paid" && session.fee == "0") {
                    tempSessionsList = tempSessionsList - session
                }
            }

            for (session in tempSessionsList) {
                if (session.available_capacity > 0) {
                    tomorrowAvailableSessionsList = tomorrowAvailableSessionsList + session
                } else {
                    tomorrowNonAvailableSessionsList = tomorrowNonAvailableSessionsList + session
                }
            }

        }

        finalSessionsList=todayAvailableSessionsList+tomorrowAvailableSessionsList+ todayNonAvailableSessionsList + tomorrowNonAvailableSessionsList


        if(fromSearch && (doseCheckedItem!=-1 || ageCheckedItem !=-1)) {
            var finalList: List<Session> = emptyList()
            if(doseCheckedItem !=-1) {
                for (session in finalSessionsList) {
                    if (doseCheckedItem == 0 && session.available_capacity_dose1 > 0) {
                        finalList = finalList + session
                    } else if (doseCheckedItem == 1 && session.available_capacity_dose2 > 0) {
                        finalList = finalList + session
                    }
                }
            } else {
                finalList = finalSessionsList
            }

            if(ageCheckedItem !=-1) {
                var newList:List<Session> = emptyList()
                for (doses in finalList) {
                    newList = newList + getFilteredListAccordingToAge(doses)
                }
                finalList=newList
            }

            setResultCountTextView(finalList)
            return finalList
        }

        setResultCountTextView(finalSessionsList)
        return finalSessionsList

    }

    private fun setResultCountTextView(list: List<Session>) {
        if(list.size==1) {
            resultPinCount.text = list.size.toString() + " centre"
        }else{
            resultPinCount.text = list.size.toString() + " centres"
        }
    }

    private fun showTextView(errorstring: String, showFilterLayout: Boolean) {
        recyclerViewPin.visibility= GONE
        when(showFilterLayout){
            true->filtersPinLayout.visibility= VISIBLE
            false->filtersPinLayout.visibility= GONE
        }
        progressBarPin.visibility= GONE
        textViewPin.text=errorstring
        textViewPin.visibility= VISIBLE
    }

    private fun showProgressBar() {
        progressBarPin.visibility=VISIBLE;
        textViewPin.visibility= GONE
        recyclerViewPin.visibility= GONE
        filtersPinLayout.visibility= GONE
    }

    private fun hideKeyboard() {
        val `in`: InputMethodManager? =
            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        `in`?.hideSoftInputFromWindow(edittext.windowToken, 0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_by_pin, container, false);
    }

    private fun setUpClickListeners() {
        search.setOnClickListener(View.OnClickListener {
            fetchDataFromApiAndShow(false)
        })

        dosePinFilter.setOnClickListener{
            val list= arrayOf<CharSequence>("Dose 1","Dose 2")
            val builder = context?.let { it1 ->  MaterialAlertDialogBuilder(it1) }
            builder?.setTitle("Select Dose")
            builder?.setSingleChoiceItems(list,doseCheckedItem) { dialog, which ->
                dialog.dismiss()
                doseCheckedItem=which
                dosePinFilter.text=list[which]
                fetchDataFromApiAndShow(true)
            }
            val dialog = builder?.create()
            dialog?.show()
        }

        agePinFilter.setOnClickListener{
            val list= arrayOf<CharSequence>("18-44","45+")
            val builder = context?.let { it1 ->  MaterialAlertDialogBuilder(it1) }
            builder?.setTitle("Select Age Group")
            builder?.setSingleChoiceItems(list,ageCheckedItem) { dialog, which ->
                dialog.dismiss()
                ageCheckedItem=which
                agePinFilter.text=list[which]
                fetchDataFromApiAndShow(true)
            }
            val dialog = builder?.create()
            dialog?.show()
        }

    }

    private fun getFilteredListAccordingToAge(session: Session): List<Session> {

        var filteredAgeList: List<Session> = emptyList()
        if (ageCheckedItem == 0 && session.min_age_limit == 18) {
            filteredAgeList = filteredAgeList + session
        } else if (ageCheckedItem == 1 && session.min_age_limit == 45) {
            filteredAgeList = filteredAgeList + session
        }

        return filteredAgeList
    }


    private fun showRecyclerView() {
        progressBarPin.visibility= GONE
        filtersPinLayout.visibility= VISIBLE
        recyclerViewPin.visibility= VISIBLE
    }

    companion object {
        private const val ARG_POSITION = "position"

        fun getInstance(position: Int): Fragment {
            val searchByPinFragment = SearchByPinFragment()
            val bundle = Bundle()
            bundle.putInt(ARG_POSITION, position)
            searchByPinFragment.arguments = bundle
            return searchByPinFragment
        }
    }
}