import android.util.Log
import com.google.api.client.http.ByteArrayContent
import com.google.api.services.drive.Drive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class DriveServiceHelper(private val driveService: Drive) {
    private val TAG = "DriveServiceHelper"

    suspend fun createFile(fileName: String, fileContent: ByteArray): String? {
        return withContext(Dispatchers.IO) {
            try {
                val fileMetadata = com.google.api.services.drive.model.File()
                fileMetadata.name = fileName

                val mediaContent = ByteArrayContent.fromString("text/csv", String(fileContent))
                val file = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()

                file?.id
            } catch (e: IOException) {
                Log.e(TAG, "Erro ao criar arquivo", e)
                null
            }
        }
    }
}
