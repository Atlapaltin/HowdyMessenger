package ru.atlapaltin.atlapaltinmessenger

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.PackageManagerCompat.LOG_TAG
import com.google.firebase.auth.FirebaseAuth
import ru.atlapaltin.atlapaltinmessenger.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //инициализация xml UI экрана регистарции (activity_main)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //кнопка регистрации (xml activity_main) + слушатель нажатий
        binding.registerButton.setOnClickListener {
            registrationMethod()
        }
        //переход на экран входа (LoginActivity) с существующим аккаунтом
        //(ссылка на текст в xml activity_main ("alreadyHaveAccount")
        binding.alreadyHaveAccount.setOnClickListener {

            //отслеживаем событие перехода на экран входа LoginActivity
            Log.d("MainActivity", "Switching to login screen")
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun registrationMethod() {
        //поле ввода email (xml activity_main)
        val registrationEmail = binding.emailEdittextRegister.text.toString()
        //поле ввода пароля (xml activity_main)
        val registrationPassword = binding.passwordEdittextRegister.text.toString()

        //если нажали кнопку при пустых полях,
        //то просто выводим сообщение и продолжаем слушать попытки регистрации
        if (registrationEmail.isEmpty() || registrationPassword.isEmpty()) {
            Toast.makeText(
                this,
                "Please enter your registration email and password",
                Toast.LENGTH_SHORT).show()
            return
        }

        //функции отслеживания ввода данных в поля emal и пароля (для журнала событий)
        Log.d("MainActivity", "E-mail is:$registrationEmail")
        Log.d("MainActivity", "Password is: + $registrationPassword")

        //создание пользователя через Firebase
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(registrationEmail, registrationPassword)
            .addOnCompleteListener{
                //проверяем, успешно ли создан пользователь
                //(если не успешно, то возвращаемся к созданию пользователя и слушаем
                // следующую попытку создания)
                if (!it.isSuccessful) return@addOnCompleteListener
                //а если пользователь создан успешно, то делаем запись ID пользователя в журнале
                Log.d("Main", "Successfully created user with UID: ${it.result.user?.uid}")
            }
            //отслеживаем неудачные попытки создания пользователя，
            // выводим сообщение и записываем в журнал событий
            .addOnFailureListener{
                Log.d("Main", "Failed to create user: ${it.message}")
                Toast.makeText(
                    this,
                    "Please enter your registration email and password",
                    Toast.LENGTH_SHORT).show()
            }
    }
}