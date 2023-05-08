package ru.atlapaltin.atlapaltinmessenger.app.activities

import android.Manifest
import android.content.pm.PackageManager
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import ru.atlapaltin.atlapaltinmessenger.databinding.ActivityMainBinding

class UserRegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private companion object {
        private val REQUIRED_PERMISSIONS: Array<String> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            { arrayOf(Manifest.permission.CAMERA) }
            else { arrayOf(Manifest.permission.CAMERA) }
    }

    private val pickImageFromGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.also { uri ->
                binding.avatar.setImageURI(uri)
            }
        }
    }

    private val pickPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                ImagePicker.RESULT_ERROR -> {
                    Snackbar.make(
                        binding.root,
                        ImagePicker.getError(it.data),
                        Snackbar.LENGTH_LONG
                    ).show()
                }

                Activity.RESULT_OK -> {
                    val uri: Uri? = it.data?.data
                    binding.avatar.setImageURI(uri)
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //инициализация xml UI экрана регистарции (activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //кнопка регистрации (xml activity_main) + слушатель нажатий
        binding.registerButton.setOnClickListener {
            registrationMethod()
        }
        //переход на экран входа (LoginActivity) с существующим аккаунтом
        //(ссылка на текст в xml activity_main ("alreadyHaveAccount")
        binding.alreadyHaveAccount.setOnClickListener {

            //регистрируем в журнале событие перехода на экран входа LoginActivity
            Log.d("RegistrationActivity", "Switching to login screen")
            startActivity(Intent(this, LoginActivity::class.java))
        }

        //слушатель нажатий на кнопку камеры на экране регистрации
        //（ссылка на кнопку cameraButton в xml activity_main）
        binding.cameraButton.setOnClickListener {
            //регистрируем в журнале нажатия на кнопку выбора аватарки
            Log.d("MainActivity", "cameraButton clicked")
            //"НЕ"запускаем метод фотографирования
            //takePictureMethod()
            checkPermissions()
            ImagePicker.with(this)
                .cropSquare()
                .compress(512)

                //Запуск камеры
                .provider(ImageProvider.CAMERA)
                .createIntent(pickPhotoLauncher::launch) //Запуск интента
        }

        binding.selectAvatarFromGallery.setOnClickListener {
            //регистрируем в журнале нажатия на кнопку выбора аватарки selectAvatarFromGallery
            Log.d("MainActivity", "selectAvatarFromGallery button clicked")
            //выбираем изображение или фото из галереи смратфона

            pickImageFromGallery()

            //Можно также воспользоваться ImagePicker:
//                .provider(ImageProvider.GALLERY)
//                .galleryMimeTypes(
//                    arrayOf(
//                        "image/png",
//                        "image/jpeg",
//                    )
//                )
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        pickImageFromGallery.launch(intent)
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            if (map.values.all { it } && map.values.isNotEmpty()) {
                ImagePicker()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Permissions denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun checkPermissions() {
        val isAllGranted = REQUIRED_PERMISSIONS.all { permissions ->
            ContextCompat.checkSelfPermission(
                applicationContext, permissions
            ) == PackageManager.PERMISSION_GRANTED
        }
        if (isAllGranted) {
            ImagePicker()
            Toast.makeText(
                applicationContext,
                "Permissions granted",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            permissionLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }

    //метод регистрации пользователя
    private fun registrationMethod() {
        //поле ввода email (xml activity_main)
        val registrationEmail = binding.emailEdittextRegister.text.toString()
        //поле ввода пароля (xml activity_main)
        val registrationPassword = binding.passwordEdittextRegister.text.toString()

        //если нажали кнопку при пустых полях мейла и пароля,
        //то просто выводим сообщение системы и продолжаем слушать попытки регистрации
        if (registrationEmail.isEmpty() || registrationPassword.isEmpty()) {
            Toast.makeText(
                this,
                "Please enter your registration email and password",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        //регистрируем ввод данных в поля email и пароля (для журнала событий)
        Log.d("MainActivity", "E-mail is:$registrationEmail")
        Log.d("MainActivity", "Password is: + $registrationPassword")

        //создание пользователя через Firebase
        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(registrationEmail, registrationPassword)
            .addOnCompleteListener {
                //проверяем, успешно ли создан пользователь
                //(если не успешно, то возвращаемся к созданию пользователя и слушаем
                // следующую попытку создания)
                if (!it.isSuccessful) return@addOnCompleteListener
                //а если пользователь создан успешно, то делаем запись ID пользователя в журнале
                Log.d("Main", "Successfully created user with UID: ${it.result.user?.uid}")
            }
            //отслеживаем неудачные попытки создания пользователя，
            // выводим сообщение и записываем в журнал событий
            .addOnFailureListener {
                Log.d("Main", "Failed to create user: ${it.message}")
                Toast.makeText(
                    this,
                    "Please enter your registration email and password",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}