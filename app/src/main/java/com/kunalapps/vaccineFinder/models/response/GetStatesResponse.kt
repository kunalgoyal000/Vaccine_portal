package com.kunalapps.vaccineFinder.models.response

import com.kunalapps.vaccineFinder.models.State
import kotlinx.serialization.Serializable

@Serializable
data class GetStatesResponse(
    val states: List<State> = listOf(),
    val ttl: Int
)