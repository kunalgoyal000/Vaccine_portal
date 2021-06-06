package com.example.covidvaccine.adapters

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.covidvaccine.fragments.SearchByDistrictFragment
import com.example.covidvaccine.fragments.SearchByPinFragment

class VaccineAdapter(activity: AppCompatActivity, val itemsCount: Int) :
    FragmentStateAdapter(activity) {


    override fun getItemCount(): Int {
        return itemsCount;
    }

    override fun createFragment(position: Int): Fragment {

        lateinit var fragment: Fragment

        if (position == 0) {
            fragment = SearchByPinFragment.getInstance(position)
        } else if (position == 1) {
            fragment = SearchByDistrictFragment.getInstance(position)
        }
        return fragment;
    }
}