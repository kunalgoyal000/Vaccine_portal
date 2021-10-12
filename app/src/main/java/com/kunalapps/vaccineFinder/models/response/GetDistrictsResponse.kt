package com.kunalapps.vaccineFinder.models.response

import com.kunalapps.vaccineFinder.models.District
import kotlinx.serialization.Serializable

@Serializable
data class GetDistrictsResponse(
    val districts: List<District> = listOf(),
    val ttl: Int
)
