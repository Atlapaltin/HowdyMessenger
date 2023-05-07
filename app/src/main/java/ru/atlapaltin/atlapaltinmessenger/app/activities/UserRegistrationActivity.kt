package ru.atlapaltin.atlapaltinmessenger.app.activities

import android.Manifest
import android.content.pm.PackageManager
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.atlapaltin.atlapaltinmessenger.BuildConfig
import ru.atlapaltin.atlapaltinmessenger.databinding.ActivityMainBinding
import java.io.File

class UserRegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imageUri: Uri
    private companion object {
        private val REQUIRED_PERMISSIONS: Array<String> = arrayOf (
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageStr = result.data?.extras?.getString("data") // получаем строку
            // декодируем строку в массив байт
            val imageBytes = Base64.decode(imageStr, Base64.DEFAULT)
            // декодируем строку в Bitmap
            val imageBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                // создаем файл в кэш-директории
                val imageFile = withContext(Dispatchers.IO) {
                    File.createTempFile(
                        "profile_image",
                        ".jpeg",
                        applicationContext.externalCacheDir
                    )
                }
                imageFile.outputStream().use {
                    // сохраняем Bitmap в файл
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                }
                // получаем Uri для файла
                withContext(Dispatchers.Main) {
                    imageUri = FileProvider.getUriForFile(
                        this@UserRegistrationActivity,
                        "${BuildConfig.APPLICATION_ID}.provider",
                        imageFile
                    )
                    binding.avatar.setImageURI(imageUri)
                }
            }
        }
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
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
            Log.d("RegistrationActivity", "Switching to login screen")
            startActivity(Intent(this, LoginActivity::class.java))
        }

        //слушатель нажатий на кнопку камеры на экране регистрации
        //（ссылка на кнопку cameraButton в xml activity_main）
        binding.cameraButton.setOnClickListener {
            //регистрируем нажатия на кнопку выбора аватарки
            Log.d("MainActivity", "cameraButton clicked")
            //запускаем метод фотографирования
            takePictureMethod()
        }

        binding.selectAvatarFromGallery.setOnClickListener {
            //регистрируем нажатия на кнопку выбора аватарки selectAvatarFromGallery
            Log.d("MainActivity", "selectAvatarFromGallery button clicked")
            //выбираем изображение или фото из галереи смратфона
            pickImageFromGallery ()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        pickImageFromGallery.launch(intent)
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun takePictureMethod() {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also {
                    takePicture.launch(takePictureIntent)
                }
            }
        }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            if (map.values.all {it} && map.values.isNotEmpty()) {
                takePictureMethod()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Permissions denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    private fun checkPermissions (){
        val isAllGranted = REQUIRED_PERMISSIONS.all{ permissions ->
            ContextCompat.checkSelfPermission(
                applicationContext, permissions
            ) == PackageManager.PERMISSION_GRANTED
            }
        if (isAllGranted) {
            takePictureMethod()
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

        //функции отслеживания ввода данных в поля emal и пароля (для журнала событий)
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