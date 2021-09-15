package com.kunalapps.vaccinePortal

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.kunalapps.vaccinePortal.adapters.VaccineAdapter
import com.kunalapps.vaccinePortal.models.Success
import com.kunalapps.vaccinePortal.networking.NetworkStatusChecker
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : AppCompatActivity() {

    private lateinit var vaccineLists: List<String>

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

        vaccineLists=listOf(getString(R.string.search_by_pin), getString(R.string.search_by_district))
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