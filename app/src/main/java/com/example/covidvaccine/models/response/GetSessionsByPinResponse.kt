package com.example.covidvaccine.models.response

import com.example.covidvaccine.models.Session
import com.example.covidvaccine.models.State
import kotlinx.serialization.Serializable

@Serializable
data class GetSessionsByPinResponse(
    val sessions: List<Session> = listOf()
)