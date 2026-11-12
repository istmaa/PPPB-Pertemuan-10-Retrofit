package com.example.konekin.model

import com.google.gson.annotations.SerializedName

data class EmployeeData(
    @SerializedName("age")
    val age: Int? = null,

    @SerializedName("salary")
    val salary: Int? = null
)
