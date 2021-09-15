package com.kunalapps.vaccinePortal.models.response

import com.kunalapps.vaccinePortal.models.Session
import kotlinx.serialization.Serializable

@Serializable
data class GetSessionsByPinResponse(
    val sessions: List<Session> = listOf()
)