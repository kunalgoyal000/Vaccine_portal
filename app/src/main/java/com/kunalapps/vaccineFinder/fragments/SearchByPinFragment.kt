package com.kunalapps.vaccineFinder.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TextView.OnEditorActionListener
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kunalapps.vaccineFinder.App
import com.kunalapps.vaccineFinder.R
import com.kunalapps.vaccineFinder.adapters.SessionsAdapter
import com.kunalapps.vaccineFinder.models.Failure
import com.kunalapps.vaccineFinder.models.FailureString
import com.kunalapps.vaccineFinder.models.Session
import com.kunalapps.vaccineFinder.models.Success
import com.kunalapps.vaccineFinder.networking.NetworkStatusChecker
import com.kunalapps.vaccineFinder.utils.getTodayDate
import com.kunalapps.vaccineFinder.utils.getTomorrowDate
import kotlinx.android.synthetic.main.fragment_search_by_pin.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Collections.emptyList


@RequiresApi(Build.VERSION_CODES.M)
class SearchByPinFragment : Fragment() {

    private var pincodes: List<String> = listOf()
    private lateinit var adapter: ArrayAdapter<String>
    private var tempSessionsList: List<Session> = listOf()
    private var finalSessionsList: List<Session> = listOf()
    private lateinit var sessionsAdapter: SessionsAdapter
    private var doseCheckedItem = -1
    private var ageCheckedItem = -1
    private var costCheckedItem = -1
    private var vaccineCheckedItem = -1
    private val remoteApi = App.remoteApi
    private val networkStatusChecker by lazy {
        context?.let { NetworkStatusChecker(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pincodes = App.getRecentSearches()
        setUpSuggestionsAdapter()

        if (networkStatusChecker?.isConnectedToInternet() == true) {
            showTextView(getString(R.string.start_a_new_search), false)
        } else {
            showTextView(getString(R.string.no_internet_connection), false)
        }

        sessionsAdapter = SessionsAdapter(activity, finalSessionsList,doseCheckedItem)
        recyclerViewPin.layoutManager = LinearLayoutManager(activity)
        recyclerViewPin.adapter = sessionsAdapter

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
        adapter = context?.let { ArrayAdapter(it, R.layout.search_term, App.getRecentSearches()) }!!
        edittext.threshold = 1 //will start working from first character
        edittext.setAdapter(adapter) //setting the adapter data into the AutoCompleteTextView
        edittext.setDropDownBackgroundDrawable(context?.let {
            ContextCompat.getDrawable(
                it,
                R.drawable.background_form
            )
        })
        edittext.setOnItemClickListener { _, _, _, _ ->
            fetchDataFromApiAndShow(false)
        }
    }

    private fun fetchDataFromApiAndShow(fromSearch: Boolean) {
        val pincode = edittext.text.toString()
        if (networkStatusChecker?.isConnectedToInternet() == true) {
            if (pincode.isNotEmpty() && pincode.length == 6) {

                pincodes = pincodes + pincode
                App.setRecentSearches(pincodes.distinct())
                setUpSuggestionsAdapter()

                showProgressBar()
                hideKeyboard()
                edittext.dismissDropDown()

                GlobalScope.launch(Dispatchers.Main) {
                    val todayResults = remoteApi.getSessionsByPin(pincode, getTodayDate())
                    val tomorrowResults = remoteApi.getSessionsByPin(pincode, getTomorrowDate())
                    progressBarPin.visibility = GONE
                    if (!fromSearch) {
                        resetFilters()
                    }
                    if (todayResults is Success && tomorrowResults is Success) {
                        val list = getFinalSessionsList(todayResults, tomorrowResults, fromSearch);
                        if (list.isNotEmpty()) {
                            sessionsAdapter.setSessions(list,doseCheckedItem)
                            sessionsAdapter.notifyDataSetChanged()
                        } else {
                            showTextView(getString(R.string.no_slots_available), fromSearch)
                        }
                    } else if (todayResults is Success) {
                        val list = getFinalSessionsList(todayResults, null, fromSearch);
                        if (list.isNotEmpty()) {
                            sessionsAdapter.setSessions(list,doseCheckedItem)
                            sessionsAdapter.notifyDataSetChanged()
                        } else {
                            showTextView(getString(R.string.no_slots_available), fromSearch)
                        }
                    } else if (tomorrowResults is Success) {
                        val list = getFinalSessionsList(null, tomorrowResults, fromSearch);
                        if (list.isNotEmpty()) {
                            sessionsAdapter.setSessions(list,doseCheckedItem)
                            sessionsAdapter.notifyDataSetChanged()
                        } else {
                            showTextView(getString(R.string.no_slots_available), fromSearch)
                        }
                    } else if (todayResults is FailureString) {
                        showTextView(todayResults.errorstring, false);
                    } else if (todayResults is Failure) {
                        if ((todayResults.error) is IOException) {
                            showTextView(getString(R.string.no_internet_connection), false)
                        } else {
                            showTextView(getString(R.string.some_error_occurred_please_try_again), false)
                        }
                    }
                }
            } else {
                //if pincode length is smaller than 6 or empty
            }
        } else {
            showTextView(getString(R.string.no_internet_connection), false)
        }
    }

    private fun resetFilters() {
        dosePinFilter.text = getString(R.string.dose)
        agePinFilter.text = getString(R.string.age)
        costPinFilter.text = getString(R.string.cost)
        vaccinePinFilter.text = getString(R.string.vaccine)
        doseCheckedItem = -1
        ageCheckedItem = -1
        costCheckedItem = -1
        vaccineCheckedItem = -1
    }

    private fun getFinalSessionsList(
        todayResults: Success<List<Session>>?,
        tomorrowResults: Success<List<Session>>?,
        fromSearch: Boolean
    ): List<Session> {
        var todayAvailableSessionsList: List<Session> = emptyList()
        var tomorrowAvailableSessionsList: List<Session> = emptyList()

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
                }
            }

        }

        finalSessionsList = todayAvailableSessionsList + tomorrowAvailableSessionsList


        if (fromSearch && (doseCheckedItem != -1 || ageCheckedItem != -1 || costCheckedItem != -1 || vaccineCheckedItem != -1)) {
            var finalList: List<Session> = emptyList()
            if (doseCheckedItem != -1) {
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

            if (ageCheckedItem != -1) {
                var newList: List<Session> = emptyList()
                for (doses in finalList) {
                    newList = newList + getFilteredListAccordingToAge(doses)
                }
                finalList = newList
            }

            if (costCheckedItem != -1) {
                var newList: List<Session> = emptyList()
                for (doses in finalList) {
                    newList = newList + getFilteredListAccordingToCost(doses)
                }
                finalList = newList
            }


            if (vaccineCheckedItem != -1) {
                var newList: List<Session> = emptyList()
                for (doses in finalList) {
                    newList = newList + getFilteredListAccordingToVaccine(doses)
                }
                finalList = newList
            }

            setResultCountTextView(finalList)
            return finalList
        }

        setResultCountTextView(finalSessionsList)
        return finalSessionsList

    }

    private fun setResultCountTextView(list: List<Session>) {
        if (list.size == 1) {
            resultPinCount.text = list.size.toString() + " " + getString(R.string.centre)
        } else {
            resultPinCount.text = list.size.toString() + " "+ getString(R.string.centres)
        }
    }

    private fun showTextView(errorstring: String, showFilterLayout: Boolean) {
        recyclerViewPin.visibility = GONE
        when (showFilterLayout) {
            true -> filtersPinLayout.visibility = VISIBLE
            false -> filtersPinLayout.visibility = GONE
        }
        progressBarPin.visibility = GONE
        textViewPin.text = errorstring
        textViewPin.visibility = VISIBLE
    }

    private fun showProgressBar() {
        progressBarPin.visibility = VISIBLE;
        textViewPin.visibility = GONE
        recyclerViewPin.visibility = GONE
        filtersPinLayout.visibility = GONE
    }

    private fun hideKeyboard() {
        val `in`: InputMethodManager? =
            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        `in`?.hideSoftInputFromWindow(edittext.windowToken, 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_by_pin, container, false);
    }

    private fun setUpClickListeners() {
        search.setOnClickListener {
            fetchDataFromApiAndShow(false)
        }

        refreshPinData.setOnClickListener {
            fetchDataFromApiAndShow(true)
        }

        dosePinFilter.setOnClickListener {
            val list = arrayOf<CharSequence>(getString(R.string.dose_one_numeric), getString(R.string.dose_two_numeric))
            val builder = context?.let { it1 -> MaterialAlertDialogBuilder(it1) }
            builder?.setTitle(getString(R.string.select_dose))
            builder?.setSingleChoiceItems(list, doseCheckedItem) { dialog, which ->
                dialog.dismiss()
                doseCheckedItem = which
                dosePinFilter.text = list[which]
                fetchDataFromApiAndShow(true)
            }
            val dialog = builder?.create()
            dialog?.show()
        }

        agePinFilter.setOnClickListener {
            val list = arrayOf<CharSequence>(getString(R.string.eighteen_and_fortyfour), getString(R.string.fortyfive_plus))
            val builder = context?.let { it1 -> MaterialAlertDialogBuilder(it1) }
            builder?.setTitle(getString(R.string.select_age_group))
            builder?.setSingleChoiceItems(list, ageCheckedItem) { dialog, which ->
                dialog.dismiss()
                ageCheckedItem = which
                agePinFilter.text = list[which]
                fetchDataFromApiAndShow(true)
            }
            val dialog = builder?.create()
            dialog?.show()
        }

        costPinFilter.setOnClickListener {
            val list = arrayOf<CharSequence>(getString(R.string.free), getString(R.string.paid))
            val builder = context?.let { it1 -> MaterialAlertDialogBuilder(it1) }
            builder?.setTitle(getString(R.string.select_cost_type))
            builder?.setSingleChoiceItems(list, costCheckedItem) { dialog, which ->
                dialog.dismiss()
                costCheckedItem = which
                costPinFilter.text = list[which]
                fetchDataFromApiAndShow(true)
            }
            val dialog = builder?.create()
            dialog?.show()
        }

        vaccinePinFilter.setOnClickListener {
            val list = arrayOf<CharSequence>(getString(R.string.covishield), getString(R.string.covaxin), getString(R.string.sputnik_v))
            val builder = context?.let { it1 -> MaterialAlertDialogBuilder(it1) }
            builder?.setTitle(getString(R.string.select_vaccine_type))
            builder?.setSingleChoiceItems(list, vaccineCheckedItem) { dialog, which ->
                dialog.dismiss()
                vaccineCheckedItem = which
                vaccinePinFilter.text = list[which]
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

    private fun getFilteredListAccordingToCost(session: Session): List<Session> {
        var filteredAgeList: List<Session> = emptyList()
        if (costCheckedItem == 0 && session.fee_type == "Free") {
            filteredAgeList = filteredAgeList + session
        } else if (costCheckedItem == 1 && session.fee_type == "Paid") {
            filteredAgeList = filteredAgeList + session
        }

        return filteredAgeList
    }

    private fun getFilteredListAccordingToVaccine(session: Session): List<Session> {
        var filteredAgeList: List<Session> = emptyList()
        if (vaccineCheckedItem == 0 && session.vaccine == "COVISHIELD") {
            filteredAgeList = filteredAgeList + session
        } else if (vaccineCheckedItem == 1 && session.vaccine == "COVAXIN") {
            filteredAgeList = filteredAgeList + session
        } else if (vaccineCheckedItem == 2 && session.vaccine == "SPUTNIK V") {
            filteredAgeList = filteredAgeList + session
        }

        return filteredAgeList
    }


    private fun showRecyclerView() {
        progressBarPin.visibility = GONE
        filtersPinLayout.visibility = VISIBLE
        recyclerViewPin.visibility = VISIBLE
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