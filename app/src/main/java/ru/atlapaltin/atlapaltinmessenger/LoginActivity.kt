package ru.atlapaltin.atlapaltinmessenger

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import ru.atlapaltin.atlapaltinmessenger.databinding.ActivityLoginBinding

class LoginActivity: AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //инициализация xml UI экрана входа с существующим аккаунтом (activity_login)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //кнопка входа (xml activity_login) + слушатель нажатий
        binding.loginButton.setOnClickListener {
            //поле ввода email (xml activity_login)
            val signInEmail = binding.loginEmailAddress.text.toString()
            //поле ввода пароля (xml activity_login)
            val signInPassword = binding.loginPassword.text.toString()

            //отслеживание полей ввода email и пароля для журнала событий
            Log.d("Login", "Attempt login with email/password: $signInEmail/***")

            //авторизация пользователя через Firebase
            FirebaseAuth.getInstance().signInWithEmailAndPassword(signInEmail, signInPassword)
//          .addOnCompleteListener()
//          .add

        }

        //возврат к регистрации (текст backToRegistrationLink
        // в xml activity_login) + слушатель нажатия по этому тексту
        binding.backToRegistrationLink.setOnClickListener{
            finish()
        }

    }
}