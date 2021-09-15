package com.kunalapps.vaccinePortal.models.response

import com.kunalapps.vaccinePortal.models.District
import kotlinx.serialization.Serializable

@Serializable
data class GetDistrictsResponse(
    val districts: List<District> = listOf(),
    val ttl: Int
)
