package com.example.covidvaccine.models

import kotlinx.serialization.Serializable

@Serializable
class District(
    val district_id:Int,
    val district_name: String
)
