package com.example.konekin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.konekin.databinding.ActivityUserDetailBinding
import com.example.konekin.databinding.DialogEmployeeFormBinding
import com.example.konekin.model.DeleteResponse
import com.example.konekin.model.Employee
import com.example.konekin.model.EmployeeData
import com.example.konekin.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserDetailBinding
    private var currentEmployeeId: String? = null
    private var currentEmployee: Employee? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = ContextCompat.getColor(this, R.color.purple_primary)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        window.navigationBarColor = ContextCompat.getColor(this, R.color.bg_pink)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = true

        binding = ActivityUserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentEmployeeId = intent.getStringExtra("EMPLOYEE_ID")
        if (currentEmployeeId.isNullOrEmpty()) {
            Toast.makeText(this, "ID Karyawan tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchEmployeeDetail(currentEmployeeId!!)

        binding.btnEdit.setOnClickListener {
            showEditDialog()
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun fetchEmployeeDetail(id: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.cardDetail.visibility = View.INVISIBLE

        ApiClient.getInstance().getEmployeeDetail(id).enqueue(object : Callback<Employee> {
            override fun onResponse(call: Call<Employee>, response: Response<Employee>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    val employee = response.body()!!
                    currentEmployee = employee
                    binding.cardDetail.visibility = View.VISIBLE

                    binding.tvName.text = employee.name
                    binding.tvId.text = employee.id ?: id
                    binding.tvAge.text = employee.data?.age?.let { "$it tahun" } ?: "-"
                    binding.tvSalary.text = employee.data?.salary?.let { "$$it" } ?: "-"
                } else {
                    Toast.makeText(
                        this@UserDetailActivity,
                        "Gagal mengambil detail karyawan (${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Employee>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    this@UserDetailActivity,
                    "Koneksi bermasalah: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun showEditDialog() {
        val dialogBinding = DialogEmployeeFormBinding.inflate(LayoutInflater.from(this))
        dialogBinding.tvFormTitle.text = "Edit Karyawan"

        dialogBinding.etName.setText(currentEmployee?.name.orEmpty())
        dialogBinding.etAge.setText(currentEmployee?.data?.age?.toString().orEmpty())
        dialogBinding.etSalary.setText(currentEmployee?.data?.salary?.toString().orEmpty())

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

                val updatedEmployee = Employee(
                    name = name,
                    data = EmployeeData(age = age, salary = salary)
                )

                updateEmployeeData(updatedEmployee)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateEmployeeData(employee: Employee) {
        val id = currentEmployeeId ?: return
        binding.progressBar.visibility = View.VISIBLE

        ApiClient.getInstance().updateEmployee(id, employee).enqueue(object : Callback<Employee> {
            override fun onResponse(call: Call<Employee>, response: Response<Employee>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(this@UserDetailActivity, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    fetchEmployeeDetail(id)
                } else {
                    Toast.makeText(
                        this@UserDetailActivity,
                        "Gagal memperbarui data (${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Employee>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@UserDetailActivity, "Koneksi bermasalah: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus karyawan ${currentEmployee?.name.orEmpty()}?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteEmployeeData()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteEmployeeData() {
        val id = currentEmployeeId ?: return
        binding.progressBar.visibility = View.VISIBLE

        ApiClient.getInstance().deleteEmployee(id).enqueue(object : Callback<DeleteResponse> {
            override fun onResponse(call: Call<DeleteResponse>, response: Response<DeleteResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    Toast.makeText(this@UserDetailActivity, "Karyawan berhasil dihapus", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@UserDetailActivity,
                        "Gagal menghapus karyawan (${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@UserDetailActivity, "Koneksi bermasalah: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
