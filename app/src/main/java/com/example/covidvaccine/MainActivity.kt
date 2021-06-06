package com.example.covidvaccine

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.covidvaccine.adapters.VaccineAdapter
import com.example.covidvaccine.fragments.SearchByDistrictFragment
import com.example.covidvaccine.models.Success
import com.example.covidvaccine.networking.NetworkStatusChecker
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_search_by_pin.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : AppCompatActivity() {

    private val vaccineLists: List<String> = listOf("Search by Pin", "Search by District")

        private val remoteApi = App.remoteApi
    private val networkStatusChecker by lazy {
            NetworkStatusChecker(this)
    }
    private var vaccinePageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
             val `in`: InputMethodManager? = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                `in`?.hideSoftInputFromWindow(window.decorView.windowToken, 0)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val vaccineAdapter = VaccineAdapter(this, vaccineLists.size)
        vaccineViewPager.adapter = vaccineAdapter

        vaccineViewPager.registerOnPageChangeCallback(vaccinePageChangeCallback)

        TabLayoutMediator(tabLayout, vaccineViewPager) { tab, position ->
            tab.text = vaccineLists[position]
        }.attach()

        if (networkStatusChecker.isConnectedToInternet() && App.getStates().isEmpty()) {
            GlobalScope.launch(Dispatchers.Main) {
                val result = remoteApi.getStates()
                if (result is Success) {
                    App.setStates(result.data)
                }
            }
        }

//        vaccineViewPager.layoutDirection=ViewPager2.LAYOUT_DIRECTION_RTL
//        tabLayout.layoutDirection=ViewPager2.LAYOUT_DIRECTION_RTL
    }

    override fun onDestroy() {
        super.onDestroy()
        vaccineViewPager.unregisterOnPageChangeCallback(vaccinePageChangeCallback)
    }
}