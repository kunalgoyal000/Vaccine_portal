package com.example.covidvaccine.models.response

import com.example.covidvaccine.models.State
import kotlinx.serialization.Serializable

@Serializable
data class GetStatesResponse(
    val states: List<State> = listOf(),
    val ttl: Int
)