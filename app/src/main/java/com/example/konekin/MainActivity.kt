package com.example.konekin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.konekin.databinding.ActivityMainBinding
import com.example.konekin.databinding.DialogEmployeeFormBinding
import com.example.konekin.model.Employee
import com.example.konekin.model.EmployeeData
import com.example.konekin.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val employeeList = ArrayList<Employee>()
    private val employeeNames = ArrayList<String>()
    private lateinit var listAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = ContextCompat.getColor(this, R.color.purple_primary)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        window.navigationBarColor = ContextCompat.getColor(this, R.color.bg_pink)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = true

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listAdapter = ArrayAdapter(
            this,
            R.layout.item_employee,
            R.id.tv_nama,
            employeeNames
        )
        binding.lvNama.adapter = listAdapter

        binding.lvNama.setOnItemClickListener { _, _, position, _ ->
            if (position in employeeList.indices) {
                val selectedEmployee = employeeList[position]
                val intent = Intent(this, UserDetailActivity::class.java).apply {
                    putExtra("EMPLOYEE_ID", selectedEmployee.id)
                }
                startActivity(intent)
            }
        }

        binding.btnTambah.setOnClickListener {
            showAddEmployeeDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        loadEmployees()
    }

    private fun loadEmployees() {
        ApiClient.getInstance().getAllEmployees().enqueue(object : Callback<List<Employee>> {
            override fun onResponse(call: Call<List<Employee>>, response: Response<List<Employee>>) {
                if (response.isSuccessful && response.body() != null) {
                    employeeList.clear()
                    employeeNames.clear()

                    val employees = response.body()!!
                    for (emp in employees) {
                        employeeList.add(emp)
                        employeeNames.add(emp.name)
                    }
                    listAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Gagal memuat karyawan dari server (${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Employee>>, t: Throwable) {
                Toast.makeText(
                    this@MainActivity,
                    "Koneksi internet bermasalah: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun showAddEmployeeDialog() {
        val dialogBinding = DialogEmployeeFormBinding.inflate(LayoutInflater.from(this))
        dialogBinding.tvFormTitle.text = "Tambah Karyawan"

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setPositiveButton("Simpan") { _, _ ->
                val name = dialogBinding.etName.text.toString().trim()
                val ageText = dialogBinding.etAge.text.toString().trim()
                val salaryText = dialogBinding.etSalary.text.toString().trim()

                if (name.isEmpty() || ageText.isEmpty() || salaryText.isEmpty()) {
                    Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val age = ageText.toIntOrNull()
                val salary = salaryText.toIntOrNull()

                if (age == null || salary == null) {
                    Toast.makeText(this, "Usia dan Gaji harus berupa angka valid", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val newEmployee = Employee(
                    name = name,
                    data = EmployeeData(age = age, salary = salary)
                )

                createEmployee(newEmployee)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun createEmployee(employee: Employee) {
        ApiClient.getInstance().createEmployee(employee).enqueue(object : Callback<Employee> {
            override fun onResponse(call: Call<Employee>, response: Response<Employee>) {
                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(this@MainActivity, "Karyawan baru berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    loadEmployees()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Gagal menambahkan karyawan (${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Employee>, t: Throwable) {
                Toast.makeText(
                    this@MainActivity,
                    "Koneksi internet bermasalah: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
