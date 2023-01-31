package ru.atlapaltin.atlapaltinmessenger

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import ru.atlapaltin.atlapaltinmessenger.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener {
            binding.emailEdittextRegister.text.toString()
            binding.passwordEdittextRegister.text.toString()

            Log.d("MainActivity", "E-mail is:" + binding.emailEdittextRegister.text.toString())
            Log.d("MainActivity", "Password is:" + binding.passwordEdittextRegister.text.toString())
        }

        binding.alreadyHaveAccount.setOnClickListener {
            Log.d("MainActivity", "Switching to login screen")
            startActivity(Intent(this, LoginActivity::class.java))
        }



    }
}