@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.example.pedulipadi

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pedulipadi.TensorFlowHelper.imageSize
import com.example.pedulipadi.ui.theme.DarkBlue
import com.example.pedulipadi.ui.theme.NormalBlue
import com.example.pedulipadi.ui.theme.PeduliPadiTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isSplashScreen = mutableStateOf(true)
        lifecycleScope.launch(Dispatchers.IO) {
            delay(1000)
            isSplashScreen.value = false
        }

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.isSplashScreen.value
            }
        }

        setContent {
            PeduliPadiTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "main_screen"
                ) {
                    composable("main_screen") {
                        ImagePicker(navController = navController)
                    }

                    composable("detail_screen/{leaf}/{confidence}") {backStackEntry ->

                        val leaf = backStackEntry.arguments?.getString("leaf") ?: ""
                        val confidence = backStackEntry.arguments?.getString("confidence")?.toFloatOrNull() ?: 0f

                        DetailScreenContent(leaf, confidence)
                    }
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ImagePicker(navController: NavHostController) {
    var bitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    var imageFromCamera by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var leafResult by remember { mutableStateOf("") }

    val context = LocalContext.current
    val activity = context as Activity
    val writeAndReadStoragePermission = 1

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { image: Bitmap? ->
            bitmap = image
            imageFromCamera = true
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                @Suppress("DEPRECATION")
                if (Build.VERSION.SDK_INT < 28)
                    bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                else {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    bitmap = ImageDecoder.decodeBitmap(
                        source
                    ) { decoder, _, _ ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        decoder.isMutableRequired = true
                    }
                }
                imageFromCamera = false
            }
        }
    )

    DisposableEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                permissionsToRequest.toTypedArray(),
                writeAndReadStoragePermission
            )
        }
        onDispose { }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if (bitmap == null) {
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .background(
                            color = Color.LightGray,
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "upload gambar untuk di prediksi",
                        modifier = Modifier,
                        color = Color.Black,
                        fontStyle = FontStyle.Italic)
                }
            }

            bitmap?.let {
                val scaledBitmap = Bitmap.createScaledBitmap(it, imageSize, imageSize, false)
                Image(
                    bitmap = scaledBitmap.asImageBitmap(),
                    contentDescription = "Image from the gallery",
                    Modifier.size(300.dp)
                )
                Spacer(modifier = Modifier.padding(20.dp))

                TensorFlowHelper.ClassifyImage(scaledBitmap) { leaf, confidence ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val percentageConfidence = (confidence * 100).roundToInt()
                        Text(text = "Hasil prediksi:")
                        Text(
                            text = leaf,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold)
                        Text(
                            text = "Tingkat keyakinan model: $percentageConfidence%")
                        leafResult = leaf

                        Button(
                            onClick = {
                                navController.navigate("detail_screen/$leafResult/$confidence")
                            },
                            modifier = Modifier
                                .width(200.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NormalBlue,
                                contentColor = NormalBlue
                            )
                        ) {
                            Text(text = "Lihat detail",
                                color = Color.White)
                        }
                    }
                }

                if (imageFromCamera) {
                    Button(
                        onClick = { showDialog = true },
                        modifier = Modifier
                            .width(200.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NormalBlue,
                            contentColor = NormalBlue
                        )
                    ) {
                        Text(
                            text = "Simpan gambar",
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.padding(20.dp))

            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier
                    .width(300.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkBlue,
                    contentColor = DarkBlue
                )
            ) {
                Text(text = "Pilih gambar",
                    color = Color.White)
            }

            Spacer(modifier = Modifier.padding(5.dp))

            Button(
                onClick = {
                    cameraLauncher.launch(null)
                }, modifier = Modifier
                    .width(300.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkBlue,
                    contentColor = DarkBlue
                )
            ) {
                Text(text = "Ambil foto",
                    color = Color.White)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Simpan gambar ke galeri") },
            text = { Text(text = "Apakah anda yakin?") },
            confirmButton = {
                Button(onClick = {
                    val filename = getUniqueFilename(context, leafResult)
                    if (bitmap != null) {
                        saveImageToGallery(context, bitmap, filename)
                    } else {
                        Toast.makeText(context, "Eror. Izinkan aplikasi untuk dapta mengakses penyimpanan", Toast.LENGTH_SHORT).show()
                    }
                    showDialog = false
                }) {
                    Text("Ya")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }


}

fun getUniqueFilename(context: Context, baseName: String): String {
    var counter = 0
    var filename = baseName
    while (isFileExist(context, filename)) {
        filename = "$baseName (${++counter})"
    }
    return filename
}

fun isFileExist(context: Context, filename: String): Boolean {
    val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
    val selection = "${MediaStore.Images.Media.DISPLAY_NAME} = ?"
    val selectionArgs = arrayOf(filename)
    val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
    val isExist = (cursor?.count ?: 0) > 0
    cursor?.close()
    return isExist
}

fun saveImageToGallery(context: Context, bitmapImage: Bitmap?, imageFileName: String): String {
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
    }

    val externalUri: Uri? =
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    Log.d("EXTERNAL URI", "$externalUri")

    return if (externalUri != null) {

        context.contentResolver.openOutputStream(externalUri).use { fos ->
            Log.d("FOS", "$fos")
            if (fos != null) {
                bitmapImage?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            }
        }
        Toast.makeText(context, "Gambar berhasil disimpan", Toast.LENGTH_SHORT).show()
        externalUri.toString()
    } else {
        Toast.makeText(context, "Gagal menyimpan gambar", Toast.LENGTH_SHORT).show()
        ""
    }
}
