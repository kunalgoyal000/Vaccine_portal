package com.example.covidvaccine.fragments

import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.covidvaccine.R
import kotlinx.android.synthetic.main.fragment_search_by_district.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.covidvaccine.App
import com.example.covidvaccine.adapters.SessionsAdapter
import com.example.covidvaccine.models.*
import com.example.covidvaccine.networking.NetworkStatusChecker
import com.example.covidvaccine.utils.getTodayDate
import com.example.covidvaccine.utils.getTomorrowDate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Collections.emptyList


class SearchByDistrictFragment : Fragment() {

    private var selectedStateId:Int = -1
    private var selectedDistrictId:Int = -1
    private lateinit var tempSessionsList: List<Session>
    private lateinit var finalSessionsList: List<Session>
    private lateinit var districtList:List<District>
    private lateinit var progressDialog:ProgressDialog
    private var doseCheckedItem=-1
    private var ageCheckedItem=-1
    private lateinit var sessionsAdapter: SessionsAdapter
    private val remoteApi = App.remoteApi
    private val networkStatusChecker by lazy {
        context?.let { NetworkStatusChecker(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_by_district, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog= ProgressDialog(context)
        if(networkStatusChecker?.isConnectedToInternet() == true) {
            showtextView("Select a state to proceed",false)
        }else{
            showtextView(getString(R.string.no_internet_connection), false)
        }

        tempSessionsList= emptyList()
        finalSessionsList= emptyList()
        districtList= emptyList()
        sessionsAdapter= SessionsAdapter(activity,finalSessionsList)
        recyclerViewDistrict.layoutManager= LinearLayoutManager(activity)
        recyclerViewDistrict.adapter=sessionsAdapter

        setUpClickListeners()
    }

    private fun setUpClickListeners() {
        var list= arrayOf<CharSequence>()
        App.getStates().forEach {
            list+=(it.state_name)
        }


        stateTextView.setOnClickListener {
            if (networkStatusChecker?.isConnectedToInternet() == true) {
                val builder = context?.let { it1 -> MaterialAlertDialogBuilder(it1) }
                builder?.setTitle("Select state")
                builder?.setItems(list) { dialog, which ->
                    dialog.dismiss()
                    stateTextView.text = App.getStates()[which].state_name
                    if (App.getStates()[which].state_id != selectedStateId) {
                        districtTextView.text = "Select district"
                        finalSessionsList = emptyList()
                        sessionsAdapter.setSessions(finalSessionsList)
                        sessionsAdapter.notifyDataSetChanged()
                        showtextView("Select a state to proceed", false)
                        getDistricts(App.getStates()[which].state_id)
                    } else {
                        showDistrictsDialog(districtList)
                    }
                    selectedStateId = App.getStates()[which].state_id
                }
                val dialog = builder?.create()
                dialog?.show()
            }else{
                showtextView(getString(R.string.no_internet_connection), false)
            }
        }

        districtTextView.setOnClickListener {
            if (networkStatusChecker?.isConnectedToInternet() == true) {
                if (districtList.isNotEmpty()) {
                    showDistrictsDialog(districtList)
                } else {
                    Toast.makeText(context, "Please select a state first", Toast.LENGTH_SHORT)
                        .show()
                }
            }else{
                showtextView(getString(R.string.no_internet_connection), false)
            }
        }

        doseDistrictFilter.setOnClickListener{
            val list= arrayOf<CharSequence>("Dose 1","Dose 2")
            val builder = context?.let { it1 ->  MaterialAlertDialogBuilder(it1) }
            builder?.setTitle("Select Dose")
            builder?.setSingleChoiceItems(list,doseCheckedItem) { dialog, which ->
                dialog.dismiss()
                doseCheckedItem=which
                doseDistrictFilter.text=list[which]
                fetchDataFromApiAndShow(selectedDistrictId,true)
            }
            val dialog = builder?.create()
            dialog?.show()
        }

        ageDistrictFilter.setOnClickListener{
            val list= arrayOf<CharSequence>("18-44","45+")
            val builder = context?.let { it1 ->  MaterialAlertDialogBuilder(it1) }
            builder?.setTitle("Select Age Group")
            builder?.setSingleChoiceItems(list,ageCheckedItem) { dialog, which ->
                dialog.dismiss()
                ageCheckedItem=which
                ageDistrictFilter.text=list[which]
                fetchDataFromApiAndShow(selectedDistrictId,true)
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
                    val response=result.data
                    districtList=response
                    showDistrictsDialog(response)
                }else if(result is Failure || result is FailureString){
                    hideProgressDialogDueToError()
                }
            }
        }else{
            hideProgressDialogDueToError()
        }
    }

    private fun hideProgressDialogDueToError() {
        districtList= emptyList()
        progressDialog.dismiss()
        showtextView(getString(R.string.no_internet_connection), false)
    }

    private fun showProgressDialog() {
        progressDialog.setTitle("Loading")
        progressDialog.setMessage("Fetching districts")
        progressDialog.setCancelable(true)
        progressDialog.show()
    }

    private fun showDistrictsDialog(response: List<District>) {
        var list= arrayOf<CharSequence>()
        response.forEach {
            list+=(it.district_name)
        }
        val builder = context?.let { it1 ->  MaterialAlertDialogBuilder(it1) }
        builder?.setTitle("Select district")
                builder?.setItems(list) { dialog, which ->
            dialog.dismiss()
            districtTextView.text=response[which].district_name
            selectedDistrictId=response[which].district_id
            fetchDataFromApiAndShow(selectedDistrictId,false)
        }
        val dialog = builder?.create()
        dialog?.show()
    }

    private fun fetchDataFromApiAndShow(districtId:Int,fromSearch: Boolean) {
        if(networkStatusChecker?.isConnectedToInternet() == true){
                showprogressBar()

                GlobalScope.launch(Dispatchers.Main) {
                    val todayResults = remoteApi.getSessionsByDistricts(districtId, getTodayDate())
                    val tomorrowResults = remoteApi.getSessionsByDistricts(districtId, getTomorrowDate())
                    progressBarDistrict.visibility= View.GONE
                    if(!fromSearch) {
                        resetFilters()
                    }
                    if (todayResults is Success && tomorrowResults is Success) {
                        val list=getFinalSessionsList(todayResults,tomorrowResults,fromSearch);
                        if(list.isNotEmpty()) {
                            sessionsAdapter.setSessions(list)
                            sessionsAdapter.notifyDataSetChanged()
                        }else{
                            showtextView(getString(R.string.no_slots_available),fromSearch)
                        }
                    }else if (todayResults is Success) {
                        val list=getFinalSessionsList(todayResults,null,fromSearch);
                        if(list.isNotEmpty()) {
                            sessionsAdapter.setSessions(list)
                            sessionsAdapter.notifyDataSetChanged()
                        }else{
                            showtextView(getString(R.string.no_slots_available),fromSearch)
                        }
                    } else if (tomorrowResults is Success) {
                        val list=getFinalSessionsList(null,tomorrowResults,fromSearch);
                        if(list.isNotEmpty()) {
                            sessionsAdapter.setSessions(list)
                            sessionsAdapter.notifyDataSetChanged()
                        }else{
                            showtextView(getString(R.string.no_slots_available),fromSearch)
                        }
                    }else if(todayResults is FailureString){
                        showtextView(todayResults.errorstring, false);
                    }else if(todayResults is Failure){
                        if((todayResults.error) is IOException) {
                            showtextView(getString(R.string.no_internet_connection), false)
                        }else{
                            showtextView("Some error occured.Please try again", false)
                        }
                    }
                }
        }else{
            showtextView(getString(R.string.no_internet_connection), false)
        }
    }

    private fun resetFilters() {
        doseDistrictFilter.text="Dose"
        ageDistrictFilter.text="Age"
        doseCheckedItem=-1
        ageCheckedItem=-1
    }

    private fun getFinalSessionsList(todayResults: Success<List<Session>>?, tomorrowResults: Success<List<Session>>?,fromSearch: Boolean): List<Session> {
        var todayAvailableSessionsList:List<Session> = emptyList()
        var tomorrowAvailableSessionsList:List<Session> = emptyList()

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

        finalSessionsList=todayAvailableSessionsList+tomorrowAvailableSessionsList

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

    private fun getFilteredListAccordingToAge(session: Session): List<Session> {

        var filteredAgeList: List<Session> = emptyList()
        if (ageCheckedItem == 0 && session.min_age_limit == 18) {
            filteredAgeList = filteredAgeList + session
        } else if (ageCheckedItem == 1 && session.min_age_limit == 45) {
            filteredAgeList = filteredAgeList + session
        }

        return filteredAgeList
    }

    private fun setResultCountTextView(list: List<Session>) {
        if(list.size==1) {
            resultDistrictCount.text = list.size.toString() + " centre"
        }else{
            resultDistrictCount.text = list.size.toString() + " centres"
        }
    }

    private fun showtextView(errorstring: String, showFilterLayout: Boolean) {
        recyclerViewDistrict.visibility= View.GONE
        when(showFilterLayout){
            true->filtersDistrictLayout.visibility= View.VISIBLE
            false->filtersDistrictLayout.visibility= View.GONE
        }
        progressBarDistrict.visibility= View.GONE
        textViewDistrict.text=errorstring
        textViewDistrict.visibility= View.VISIBLE
    }

    private fun showRecyclerView() {
        progressBarDistrict.visibility= View.GONE
        filtersDistrictLayout.visibility= View.VISIBLE
        recyclerViewDistrict.visibility= View.VISIBLE
    }

    private fun showprogressBar() {
        progressBarDistrict.visibility= View.VISIBLE;
        filtersDistrictLayout.visibility= View.GONE
        textViewDistrict.visibility= View.GONE
        recyclerViewDistrict.visibility= View.GONE
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