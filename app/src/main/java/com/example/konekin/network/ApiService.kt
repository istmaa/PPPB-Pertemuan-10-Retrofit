package com.example.konekin.network

import com.example.konekin.model.DeleteResponse
import com.example.konekin.model.Employee
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @GET("collections/employees/objects")
    fun getAllEmployees(): Call<List<Employee>>

    @GET("collections/employees/objects/{id}")
    fun getEmployeeDetail(@Path("id") id: String): Call<Employee>

    @POST("collections/employees/objects")
    fun createEmployee(@Body employee: Employee): Call<Employee>

    @PUT("collections/employees/objects/{id}")
    fun updateEmployee(@Path("id") id: String, @Body employee: Employee): Call<Employee>

    @DELETE("collections/employees/objects/{id}")
    fun deleteEmployee(@Path("id") id: String): Call<DeleteResponse>
}
