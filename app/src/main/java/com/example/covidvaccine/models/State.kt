package com.example.covidvaccine.models

import kotlinx.serialization.Serializable

@Serializable
class State(
    val state_id: Int,
    val state_name: String
)