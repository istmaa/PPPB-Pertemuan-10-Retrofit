package com.example.konekin

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.konekin.databinding.ActivityMainBinding
import com.example.konekin.model.Users
import com.example.konekin.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = ContextCompat.getColor(this, R.color.purple_primary)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        window.navigationBarColor = ContextCompat.getColor(this, R.color.bg_pink)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = true

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val client = ApiClient.getInstance()
        val response = client.getAllUsers()
        val userList = ArrayList<String>()

        response.enqueue(object : Callback<Users> {
            override fun onResponse(call: Call<Users>, response: Response<Users>) {
                if (response.isSuccessful && response.body() != null) {
                    val users = response.body()?.data
                    if (users != null) {
                        for (user in users) {
                            userList.add(user.employeeName)
                        }
                    }
                    val listAdapter = ArrayAdapter(
                        this@MainActivity,
                        R.layout.item_employee,
                        R.id.tv_nama,
                        userList
                    )
                    binding.lvNama.adapter = listAdapter
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("MainActivity", "Server error ${response.code()}: $errorBody")
                    if (response.code() == 429) {
                        Toast.makeText(
                            this@MainActivity,
                            "Server sibuk (HTTP 429 Too Many Requests). Coba beberapa saat lagi.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Gagal memuat data dari server (Kode: ${response.code()})",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<Users>, t: Throwable) {
                Log.e("MainActivity", "Network failure: ${t.message}", t)
                Toast.makeText(
                    this@MainActivity,
                    "Koneksi internet bermasalah: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
