package com.kunalapps.vaccinePortal.models

import kotlinx.serialization.Serializable

@Serializable
class State(
    val state_id: Int,
    val state_name: String
)