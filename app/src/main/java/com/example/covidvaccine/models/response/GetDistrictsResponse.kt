package com.example.covidvaccine.models.response

import com.example.covidvaccine.models.District
import kotlinx.serialization.Serializable

@Serializable
data class GetDistrictsResponse(
    val districts: List<District> = listOf(),
    val ttl: Int
)
