package com.kunalapps.vaccineFinder.models.response

import com.kunalapps.vaccineFinder.models.Session
import kotlinx.serialization.Serializable

@Serializable
data class GetSessionsByPinResponse(
    val sessions: List<Session> = listOf()
)