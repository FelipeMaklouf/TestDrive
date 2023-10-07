package com.example.testdrive

import DriveServiceHelper
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.extensions.android.json.AndroidJsonFactory
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private lateinit var driveService: Drive
    private lateinit var mDriveServiceHelper: DriveServiceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        driveService = initializeDriveService()
        mDriveServiceHelper = DriveServiceHelper(driveService)

        val uploadButton = findViewById<Button>(R.id.upload_btn)
        uploadButton.setOnClickListener {
            val result = uploadFile()
            println(result)
        }
    }

    private fun initializeDriveService(): Drive {
        val credentials = GoogleCredential.fromStream(resources.openRawResource(R.raw.testedrive))
            .createScoped(listOf(DriveScopes.DRIVE_FILE))

        return Drive.Builder(AndroidHttp.newCompatibleTransport(), AndroidJsonFactory(), credentials)
            .setApplicationName("testeDrive")
            .build()
    }

    private fun uploadFile() {

        val localFilePath = "/storage/emulated/0/Documents/Armadeira/Log/03-10-2023.csv"
        val folderId = "1es7Y3nsY_Rn4f6acW1czp4sVvJgjawU_"

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val file1 = File(localFilePath)
                Log.d("ablasla", "teste $file1")
                val content = FileInputStream(file1)
                val outputStream = ByteArrayOutputStream()

                val buffer = ByteArray(1024)
                var bytesRead: Int

                while (content.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }

                val fileMetadata = com.google.api.services.drive.model.File()
                fileMetadata.name = file1.name
                val parentList = mutableListOf<String>()
                parentList.add(folderId)
                fileMetadata.parents = parentList

                val mediaContent = ByteArrayContent.fromString("text/csv", String(outputStream.toByteArray()))
                val uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()

                withContext(Dispatchers.Main) {
                    if (uploadedFile != null) {
                        showMessage("Upload concluído com sucesso. Arquivo ID: ${uploadedFile.id}")
                    } else {
                        showMessage("Falha ao fazer o upload do arquivo")
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showMessage("Erro durante o upload do arquivo")
                }
            }
        }
    }

    private fun showMessage(message: String) {
        Log.d(TAG, message)
        val loginStatusTextView = findViewById<TextView>(R.id.result_text)
        loginStatusTextView.text = message
    }
}
