package ru.atlapaltin.atlapaltinmessenger.app.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import ru.atlapaltin.atlapaltinmessenger.BuildConfig

import ru.atlapaltin.atlapaltinmessenger.databinding.ActivityMainBinding
import java.io.File

class UserRegistrationActivity : AppCompatActivity() {
   private lateinit var binding: ActivityMainBinding
    private lateinit var imageUri: Uri
    val REQUEST_PICK_IMAGE = 0
    val REQUEST_IMAGE_CAPTURE = 1

    //переменная для выбора аватарки
    val takePicture = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        if (result.resultCode == RESULT_OK) {
            // Сохраняем снимок в хранилище внешнего устройства
            val imageStr = result.data?.extras?.getString("data") // получаем строку
            val imageBytes = Base64.decode(imageStr, Base64.DEFAULT) // декодируем строку в массив байт
            // создаем объект Bitmap из массива байт
            val imageBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            val imageFile = File.createTempFile("profile_image", ".jpeg",
                applicationContext.externalCacheDir)
            imageFile.outputStream().use {
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
            imageUri = FileProvider.getUriForFile(
                this, "${BuildConfig.APPLICATION_ID}.provider", imageFile)
            // Отображаем снимок
            binding.avatar.setImageURI(imageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED) {
            // Тут запускаем приложение
        } else {
            val CAMERA_PERMISSION_CODE = 100
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }

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

            //отслеживаем событие перехода на экран входа LoginActivity
            Log.d("MainActivity", "Switching to login screen")
            startActivity(Intent(this, LoginActivity::class.java))
        }

        //слушатель нажатий на кнопку камеры на экране регистрации
        //（ссылка на кнопку cameraButton в xml activity_main）
        binding.cameraButton.setOnClickListener {
            //регистрируем нажатия на кнопку выбора аватарки
            Log.d("MainActivity", "cameraButton clicked")

            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also {
                    // Запускаем камеру
                    takePicture.launch(takePictureIntent)
                }
            }
        }

        binding.selectAvatarFromGallery.setOnClickListener {
            //регистрируем нажатия на кнопку выбора аватарки selectAvatarFromGallery
            Log.d("MainActivity", "selectAvatarFromGallery button clicked")
            //выбираем изображение или фото из галереи смратфона
            pickImageFromGallery()
        }

        binding.cameraButton.setOnClickListener {
            // запускаем камеру
            takePictureMethod()
        }
    }

    //переменная для выбора фото из галереи смартфона
    private val pickImageFromGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.also { uri ->
                // Отображаем изображение
                binding.avatar.setImageURI(uri)
            }
        }
    }

    //функция для выбора фото из галереи смартфона
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        pickImageFromGallery.launch(intent)
    }


    @SuppressLint("QueryPermissionsNeeded")
    private fun takePictureMethod() {
        // Проверяем разрешения
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // запускаем камеру
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also {
                    takePicture.launch(takePictureIntent)
                }
            }
        } else {
            // Запрашиваем разрешения
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_IMAGE_CAPTURE)
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // разрешения получены
                takePictureMethod()
            } else {
                // разрешения не были получены
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
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