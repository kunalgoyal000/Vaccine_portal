package com.kunalapps.vaccineFinder.models

import kotlinx.serialization.Serializable

val COVISHIELD="COVISHIELD"
@Serializable
class Session(
    val center_id: Int,
    val name: String,
    val address: String,
    val state_name: String,
    val district_name: String,
    val block_name: String,
    val pincode: Int,
    val from: String,
    val to: String,
    val lat: Int,
    val long: Int,
    val fee_type: String,
    val session_id: String,
    val date: String,
    val available_capacity_dose1: Int,
    val available_capacity_dose2: Int,
    val available_capacity: Int,
    val fee: String,
    val allow_all_age: Boolean?=null,
    val min_age_limit: Int,
    val max_age_limit: Int?=null,
    val vaccine: String,
    val slots: List<String> = listOf(),
)