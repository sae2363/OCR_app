package com.example.test_gui

import android.R
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.scale
import coil.compose.rememberImagePainter
import com.example.test_gui.ui.theme.Test_guiTheme
import java.io.File
import kotlin.concurrent.thread
import com.example.test_gui.OCR_main


class MainActivity : ComponentActivity() {
    var myBitmap2 by mutableStateOf<Bitmap?>(null)
    val ocr = OCR_main()
    var s by mutableStateOf("")
    var s2 by mutableStateOf("")
    var myThread = null
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
                    Greeting("Android")
                    ShowImg(myBitmap2)
                    button2(x = 10, y = 30)
                    button3(x = 10, y = 50)
                    button4(x = 10, y = 70)
                    TextBox(10, 200, "result\n$s2\n$s");
                }
            }
        }

    }

    val REQUEST_IMAGE_CAPTURE = 1

    //clickImageId = findViewById(R.id.click_image)
    fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
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

        Surface(
            modifier = Modifier
                //.fillMaxWidth()
                .size(10.dp,10.dp)
                .padding(10.dp)
                .let {
                    if (img == null) {
                        it.absoluteOffset(10.dp, 400.dp)
                    } else {
                        it.absoluteOffset(10.dp, 100.dp)
                    }
                }


        ) {
            if (img != null) {
                Image(
                    bitmap = img!!.asImageBitmap(),
                    contentDescription = "Image",
                    modifier = Modifier.size(10.dp,10.dp),
                )
            } else {
                Text(
                    "No image available", modifier = Modifier
                        .padding(16.dp)
                )
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Match the request 'pic id with requestCode
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // BitMap is data structure of image file which store the image in memory
            var photo = data!!.extras!!["data"] as Bitmap?
            val tempUri: Uri? = getImageUri(applicationContext, photo)
            /*val finalFile: File = File(getRealPathFromURI(tempUri))
            if (finalFile.exists()) {
                val myBitmap = BitmapFactory.decodeFile(finalFile.getAbsolutePath())
            }*/
            myBitmap2 = photo
        }
    }

    fun getImageUri(inContext: Context, inImage: Bitmap?): Uri? {
        val OutImage = Bitmap.createScaledBitmap(inImage!!, 1000, 1000, true)
        val path =
            MediaStore.Images.Media.insertImage(inContext.contentResolver, OutImage, "Title", null)
        return Uri.parse(path)
    }

    fun getRealPathFromURI(uri: Uri?): String? {
        var path = ""
        if (contentResolver != null) {
            val cursor = contentResolver.query(uri!!, null, null, null, null)
            if (cursor != null) {
                cursor.moveToFirst()
                val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                path = cursor.getString(idx)
                cursor.close()
            }
        }
        return path
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
                    ShowImg(myBitmap2)
                }
            }
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
                Text("hi 3")
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

            }
        }
    }


    @Composable
    fun TextBox(x: Int, y: Int, s: String) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .absoluteOffset(x.dp,y.dp)
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
            TextBox(10, 70, "hi")
        }
    }
}