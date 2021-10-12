package com.kunalapps.vaccineFinder.fragments

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kunalapps.vaccineFinder.App
import com.kunalapps.vaccineFinder.R
import com.kunalapps.vaccineFinder.adapters.SessionsAdapter
import com.kunalapps.vaccineFinder.models.*
import com.kunalapps.vaccineFinder.networking.NetworkStatusChecker
import com.kunalapps.vaccineFinder.utils.getTodayDate
import com.kunalapps.vaccineFinder.utils.getTomorrowDate
import kotlinx.android.synthetic.main.fragment_search_by_district.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Collections.emptyList


class SearchByDistrictFragment : Fragment() {

    private var selectedStateId: Int = -1
    private var selectedDistrictId: Int = -1
    private lateinit var tempSessionsList: List<Session>
    private lateinit var finalSessionsList: List<Session>
    private lateinit var districtList: List<District>
    private lateinit var progressDialog: ProgressDialog
    private var doseCheckedItem = -1
    private var ageCheckedItem = -1
    private var costCheckedItem = -1
    private var vaccineCheckedItem = -1
    private lateinit var sessionsAdapter: SessionsAdapter
    private val remoteApi = App.remoteApi
    private val networkStatusChecker by lazy {
        context?.let { NetworkStatusChecker(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_by_district, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = ProgressDialog(context)
        if (networkStatusChecker?.isConnectedToInternet() == true) {
            showtextView(getString(R.string.select_a_state_to_proceed), false)
        } else {
            showtextView(getString(R.string.no_internet_connection), false)
        }

        tempSessionsList = emptyList()
        finalSessionsList = emptyList()
        districtList = emptyList()
        sessionsAdapter = SessionsAdapter(activity, finalSessionsList, doseCheckedItem)
        recyclerViewDistrict.layoutManager = LinearLayoutManager(activity)
        recyclerViewDistrict.adapter = sessionsAdapter

        setUpClickListeners()
    }

    private fun setUpClickListeners() {
        var list = arrayOf<CharSequence>()
        App.getStates().forEach {
            list += (it.state_name)
        }


        refreshDistrictData.setOnClickListener {
            fetchDataFromApiAndShow(selectedDistrictId, true)
        }

        stateTextView.setOnClickListener {
            if (networkStatusChecker?.isConnectedToInternet() == true) {
                val builder = context?.let { it1 -> MaterialAlertDialogBuilder(it1) }
                builder?.setTitle(getString(R.string.select_state))
                builder?.setItems(list) { dialog, which ->
                    dialog.dismiss()
                    stateTextView.text = App.getStates()[which].state_name
                    if (App.getStates()[which].state_id != selectedStateId) {
                        districtTextView.text = getString(R.string.select_district)
                        finalSessionsList = emptyList()
                        sessionsAdapter.setSessions(finalSessionsList, doseCheckedItem)
                        sessionsAdapter.notifyDataSetChanged()
                        showtextView(
                            getString(R.string.select_a_state_or_district_to_proceed),
                            false
                        )
                        selectedStateId = App.getStates()[which].state_id
                        getDistricts(App.getStates()[which].state_id)
                    } else {
                        showDistrictsDialog(districtList)
                    }
                }
                val dialog = builder?.create()
                dialog?.show()
            } else {
                showtextView(getString(R.string.no_internet_connection), false)
            }
        }

        districtTextView.setOnClickListener {
            if (networkStatusChecker?.isConnectedToInternet() == true) {
                if (districtList.isNotEmpty()) {
                    showDistrictsDialog(districtList)
                } else if (selectedStateId != -1) {
                    getDistricts(selectedStateId)
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.please_select_a_state_first),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            } else {
                showtextView(getString(R.string.no_internet_connection), false)
            }
        }

        doseDistrictFilter.setOnClickListener {
            val list = arrayOf<CharSequence>(
                getString(R.string.dose_one_numeric),
                getString(R.string.dose_two_numeric)
            )
            val builder = context?.let { it1 -> MaterialAlertDialogBuilder(it1) }
            builder?.setTitle(getString(R.string.select_dose))
            builder?.setSingleChoiceItems(list, doseCheckedItem) { dialog, which ->
                dialog.dismiss()
                doseCheckedItem = which
                doseDistrictFilter.text = list[which]
                fetchDataFromApiAndShow(selectedDistrictId, true)
            }
            val dialog = builder?.create()
            dialog?.show()
        }

        ageDistrictFilter.setOnClickListener {
            val list = arrayOf<CharSequence>(
                getString(R.string.eighteen_and_fortyfour),
                getString(R.string.fortyfive_plus)
            )
            val builder = context?.let { it1 -> MaterialAlertDialogBuilder(it1) }
            builder?.setTitle(getString(R.string.select_age_group))
            builder?.setSingleChoiceItems(list, ageCheckedItem) { dialog, which ->
                dialog.dismiss()
                ageCheckedItem = which
                ageDistrictFilter.text = list[which]
                fetchDataFromApiAndShow(selectedDistrictId, true)
            }
            val dialog = builder?.create()
            dialog?.show()
        }

        costDistrictFilter.setOnClickListener {
            val list = arrayOf<CharSequence>(getString(R.string.free), getString(R.string.paid))
            val builder = context?.let { it1 -> MaterialAlertDialogBuilder(it1) }
            builder?.setTitle(getString(R.string.select_cost_type))
            builder?.setSingleChoiceItems(list, costCheckedItem) { dialog, which ->
                dialog.dismiss()
                costCheckedItem = which
                costDistrictFilter.text = list[which]
                fetchDataFromApiAndShow(selectedDistrictId, true)
            }
            val dialog = builder?.create()
            dialog?.show()
        }

        vaccineDistrictFilter.setOnClickListener {
            val list = arrayOf<CharSequence>(
                getString(R.string.covishield),
                getString(R.string.covaxin),
                getString(R.string.sputnik_v)
            )
            val builder = context?.let { it1 -> MaterialAlertDialogBuilder(it1) }
            builder?.setTitle(getString(R.string.select_vaccine_type))
            builder?.setSingleChoiceItems(list, vaccineCheckedItem) { dialog, which ->
                dialog.dismiss()
                vaccineCheckedItem = which
                vaccineDistrictFilter.text = list[which]
                fetchDataFromApiAndShow(selectedDistrictId, true)
            }
            val dialog = builder?.create()
            dialog?.show()
        }

    }

    private fun getDistricts(stateId: Int) {
        if (networkStatusChecker?.isConnectedToInternet() == true) {
            showProgressDialog()
            GlobalScope.launch(Dispatchers.Main) {
                val result = remoteApi.getDistricts(stateId)
                if (result is Success) {
                    progressDialog.dismiss()
                    val response = result.data
                    districtList = response
                    showDistrictsDialog(response)
                } else if (result is Failure || result is FailureString) {
                    hideProgressDialogDueToError()
                }
            }
        } else {
            hideProgressDialogDueToError()
        }
    }

    private fun hideProgressDialogDueToError() {
        districtList = emptyList()
        progressDialog.dismiss()
        showtextView(getString(R.string.no_internet_connection), false)
    }

    private fun showProgressDialog() {
        progressDialog.setTitle(getString(R.string.loading))
        progressDialog.setMessage(getString(R.string.fetching_districts))
        progressDialog.setCancelable(true)
        progressDialog.show()
    }

    private fun showDistrictsDialog(response: List<District>) {
        var list = arrayOf<CharSequence>()
        response.forEach {
            list += (it.district_name)
        }
        val builder = context?.let { it1 -> MaterialAlertDialogBuilder(it1) }
        builder?.setTitle(getString(R.string.select_district))
        builder?.setItems(list) { dialog, which ->
            dialog.dismiss()
            districtTextView.text = response[which].district_name
            selectedDistrictId = response[which].district_id
            fetchDataFromApiAndShow(selectedDistrictId, false)
        }
        val dialog = builder?.create()
        dialog?.show()
    }

    private fun fetchDataFromApiAndShow(districtId: Int, fromSearch: Boolean) {
        if (networkStatusChecker?.isConnectedToInternet() == true) {
            showprogressBar()

            GlobalScope.launch(Dispatchers.Main) {
                val todayResults = remoteApi.getSessionsByDistricts(districtId, getTodayDate())
                val tomorrowResults =
                    remoteApi.getSessionsByDistricts(districtId, getTomorrowDate())
                progressBarDistrict.visibility = View.GONE
                if (!fromSearch) {
                    resetFilters()
                }
                if (todayResults is Success && tomorrowResults is Success) {
                    val list = getFinalSessionsList(todayResults, tomorrowResults, fromSearch);
                    if (list.isNotEmpty()) {
                        sessionsAdapter.setSessions(list, doseCheckedItem)
                        sessionsAdapter.notifyDataSetChanged()
                    } else {
                        showtextView(getString(R.string.no_slots_available), fromSearch)
                    }
                } else if (todayResults is Success) {
                    val list = getFinalSessionsList(todayResults, null, fromSearch);
                    if (list.isNotEmpty()) {
                        sessionsAdapter.setSessions(list, doseCheckedItem)
                        sessionsAdapter.notifyDataSetChanged()
                    } else {
                        showtextView(getString(R.string.no_slots_available), fromSearch)
                    }
                } else if (tomorrowResults is Success) {
                    val list = getFinalSessionsList(null, tomorrowResults, fromSearch);
                    if (list.isNotEmpty()) {
                        sessionsAdapter.setSessions(list, doseCheckedItem)
                        sessionsAdapter.notifyDataSetChanged()
                    } else {
                        showtextView(getString(R.string.no_slots_available), fromSearch)
                    }
                } else if (todayResults is FailureString) {
                    showtextView(todayResults.errorstring, false);
                } else if (todayResults is Failure) {
                    if ((todayResults.error) is IOException) {
                        showtextView(getString(R.string.no_internet_connection), false)
                    } else {
                        showtextView(
                            getString(R.string.some_error_occurred_please_try_again),
                            false
                        )
                    }
                }
            }
        } else {
            showtextView(getString(R.string.no_internet_connection), false)
        }
    }

    private fun resetFilters() {
        doseDistrictFilter.text = getString(R.string.dose)
        ageDistrictFilter.text = getString(R.string.age)
        costDistrictFilter.text = getString(R.string.cost)
        vaccineDistrictFilter.text = getString(R.string.vaccine)
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

    private fun getFilteredListAccordingToAge(session: Session): List<Session> {

        var filteredAgeList: List<Session> = emptyList()
        if (ageCheckedItem == 0 && session.min_age_limit == 18) {
            filteredAgeList = filteredAgeList + session
        } else if (ageCheckedItem == 1 && session.min_age_limit == 45) {
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

    private fun getFilteredListAccordingToCost(session: Session): List<Session> {
        var filteredAgeList: List<Session> = emptyList()
        if (costCheckedItem == 0 && session.fee_type == "Free") {
            filteredAgeList = filteredAgeList + session
        } else if (costCheckedItem == 1 && session.fee_type == "Paid") {
            filteredAgeList = filteredAgeList + session
        }

        return filteredAgeList
    }

    private fun setResultCountTextView(list: List<Session>) {
        if (list.size == 1) {
            resultDistrictCount.text = list.size.toString() + " " + getString(R.string.centre)
        } else {
            resultDistrictCount.text = list.size.toString() + " " + getString(R.string.centres)
        }
    }

    private fun showtextView(errorstring: String, showFilterLayout: Boolean) {
        recyclerViewDistrict.visibility = View.GONE
        when (showFilterLayout) {
            true -> filtersDistrictLayout.visibility = View.VISIBLE
            false -> filtersDistrictLayout.visibility = View.GONE
        }
        progressBarDistrict.visibility = View.GONE
        textViewDistrict.text = errorstring
        textViewDistrict.visibility = View.VISIBLE
    }

    private fun showRecyclerView() {
        progressBarDistrict.visibility = View.GONE
        filtersDistrictLayout.visibility = View.VISIBLE
        recyclerViewDistrict.visibility = View.VISIBLE
    }

    private fun showprogressBar() {
        progressBarDistrict.visibility = View.VISIBLE;
        filtersDistrictLayout.visibility = View.GONE
        textViewDistrict.visibility = View.GONE
        recyclerViewDistrict.visibility = View.GONE
    }

    companion object {
        const val ARG_POSITION = "position"

        fun getInstance(position: Int): Fragment {
            val searchByDistrictFragment = SearchByDistrictFragment()
            val bundle = Bundle()
            bundle.putInt(ARG_POSITION, position)
            searchByDistrictFragment.arguments = bundle
            return searchByDistrictFragment
        }
    }


}