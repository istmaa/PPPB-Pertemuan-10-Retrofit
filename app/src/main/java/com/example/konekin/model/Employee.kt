package com.example.konekin.model

import com.google.gson.annotations.SerializedName

data class Employee(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String,

    @SerializedName("data")
    val data: EmployeeData? = null
)
