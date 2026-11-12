package com.example.konekin.model

import com.google.gson.annotations.SerializedName

data class DeleteResponse(
    @SerializedName("message")
    val message: String? = null
)
