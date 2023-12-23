package com.example.test_gui

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.test_gui.ui.theme.Test_guiTheme
import kotlin.concurrent.thread


class MainActivity : ComponentActivity() {
    var myBitmap2 by mutableStateOf<Bitmap?>(null)
    val ocr = OCR_main()
    var s by mutableStateOf("")
    var s2 by mutableStateOf("")
    var myThread = null
    private lateinit var imageUri: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ocr.intClass(this)
        setContent {
            Test_guiTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    MakeB1(10, 10)
                    button2(x = 200, y = 10)
                    //Greeting("Android")
                    button3(x = 10, y = 30)
                    button4(x = 10, y = 70)
                    TextBox(10, 200, "result\n$s2\n$s");
                }
                Box(
                    modifier = Modifier
                        .absoluteOffset(10.dp, 400.dp)
                        .size(350.dp, 262.dp)
                        .padding(10.dp)
                        .background(Color.LightGray),
                )
                {
                    ShowImg(myBitmap2)
                }
            }
        }

    }

    val REQUEST_IMAGE_CAPTURE = 1

    //clickImageId = findViewById(R.id.click_image)
    private fun dispatchTakePictureIntent() {
        imageUri = createImageUri()
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }

    @Composable
    fun ShowImg(bitmap: Bitmap?) {
        var img by remember { mutableStateOf(bitmap) }
        DisposableEffect(img) {
            onDispose {
                // Clean-up code, if needed
            }
        }

        // Use LaunchedEffect to update img when bitmap changes externally
        LaunchedEffect(bitmap) {
            img = bitmap
        }
        if (img != null) {
            Image(
                bitmap = img!!.asImageBitmap(),
                contentDescription = "Image",
                modifier = Modifier
                    .padding(10.dp)
            )
        } else {
            Text(
                "No image available", modifier = Modifier.padding(16.dp)
            )
        }
    }

    fun createImageUri(): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New Picture")
            put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Thread.sleep(10);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val photo = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)

            // If you want to resize the bitmap, use BitmapFactory.decodeFile with options
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
                inSampleSize = 1 // Adjust the sample size as needed
            }

            BitmapFactory.decodeFile(getRealPathFromURI(imageUri), options)

            // Now options.outWidth and options.outHeight should contain the dimensions

            options.inJustDecodeBounds = false
            val myBitmap = BitmapFactory.decodeFile(getRealPathFromURI(imageUri), options)

            myBitmap2 = myBitmap

            if (myBitmap != null) {
                println("${myBitmap2?.height} ${myBitmap2?.width}")
            }
        }
    }

    fun getRealPathFromURI(contentUri: Uri?): String? {//newer one
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = managedQuery(contentUri, proj, null, null, null)
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }

    fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        //return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        val rotatedBitmap = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)

        // Find the minimum bounding rectangle after rotation
        val rect = Rect(0, 0, rotatedBitmap.width, rotatedBitmap.height)
        val matrixInverse = Matrix()
        matrix.invert(matrixInverse)
        val points = floatArrayOf(0f, 0f, rotatedBitmap.width.toFloat(), 0f, 0f, rotatedBitmap.height.toFloat(), rotatedBitmap.width.toFloat(), rotatedBitmap.height.toFloat())
        matrixInverse.mapPoints(points)
        rect.left = points[0].toInt()
        rect.top = points[1].toInt()
        rect.right = points[2].toInt()
        rect.bottom = points[5].toInt()

        // Crop the rotated bitmap to the minimum bounding rectangle
        return Bitmap.createBitmap(rotatedBitmap, rect.left, rect.top, rect.width(), rect.height())
    }




    @Composable
    fun MakeB1(x: Int, y: Int) {
        val interactionSource1 = remember { MutableInteractionSource() }
        val isPressed by interactionSource1.collectIsPressedAsState()
        Box(
            modifier = Modifier
                .size(10.dp, 10.dp)
                .padding(10.dp, 10.dp)
        ) {
            Button(
                onClick = {
                    val myThread = thread {
                        dispatchTakePictureIntent()
                    }
                },
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                modifier = Modifier.absoluteOffset(x.dp, y.dp),
                interactionSource = interactionSource1,
                colors = if (!isPressed) {
                    ButtonDefaults.buttonColors(
                        MaterialTheme.colorScheme.primary
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }

            ) {
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Take Picture")
            }
        }
        if (isPressed) {
            //UpdateImg();
        }
    }

    @Composable
    fun button3(x: Int, y: Int) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        Box(
            modifier = Modifier
                .size(10.dp, 10.dp)
                .padding(10.dp, 10.dp)
                .absoluteOffset(x.dp, y.dp)
        ) {
            Button(
                onClick = {},
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                modifier = Modifier.absoluteOffset(x.dp, y.dp),
                interactionSource = interactionSource,
                colors = if (!isPressed) {
                    ButtonDefaults.buttonColors(
                        Color.Blue
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        Color.Red
                    )
                }

            ) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = "Localized description",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                //Modifier.absoluteOffset((10).dp, (10).dp)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("hi 2")
                if (isPressed) {
                }
            }
        }
    }

    @Composable
    fun button2(x: Int, y: Int) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        Box(
            modifier = Modifier
                .size(10.dp, 10.dp)
                .padding(10.dp, 10.dp)
                .absoluteOffset(x.dp, y.dp)
        ) {
            Button(
                onClick = {},
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                //modifier = Modifier.absoluteOffset(x.dp, y.dp),
                interactionSource = interactionSource,
                colors = if (!isPressed) {
                    ButtonDefaults.buttonColors(
                        MaterialTheme.colorScheme.primary
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }

            ) {
                //Modifier.absoluteOffset((10).dp, (10).dp)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Process Image")
                if (isPressed) {
                    val myThread = thread {
                        s2 = "Not done"
                        s = ocr.go(myBitmap2)
                        s2 = "Done"
                        println(s);
                    }
                }
            }
        }
    }

    @Composable
    fun button4(x: Int, y: Int) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        Box(
            modifier = Modifier
                .size(10.dp, 10.dp)
                .padding(10.dp, 10.dp)
                .absoluteOffset(x.dp, y.dp)
        ) {
            Button(
                onClick = {},
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                modifier = Modifier.absoluteOffset(x.dp, y.dp),
                interactionSource = interactionSource,
                colors = if (!isPressed) {
                    ButtonDefaults.buttonColors(
                        Color.Blue
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        Color.Red
                    )
                }

            ) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = "Localized description",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                //Modifier.absoluteOffset((10).dp, (10).dp)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("hi 4")
                if (isPressed) {
                }
            }

        }
    }


    @Composable
    fun TextBox(x: Int, y: Int, s: String) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .absoluteOffset(x.dp, y.dp)
                .padding(8.dp)
        ) {
            Text(
                text = s,
                color = Color.Black,
            )
        }
    }

    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        Text(
            text = "Hello $name!", modifier = modifier
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        Test_guiTheme {
            //TextBox(10, 70, "hi")
            //ShowImg(myBitmap2, r)
        }
    }
}