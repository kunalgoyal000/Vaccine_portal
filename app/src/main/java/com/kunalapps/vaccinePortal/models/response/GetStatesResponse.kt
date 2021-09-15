package com.kunalapps.vaccinePortal.models.response

import com.kunalapps.vaccinePortal.models.State
import kotlinx.serialization.Serializable

@Serializable
data class GetStatesResponse(
    val states: List<State> = listOf(),
    val ttl: Int
)