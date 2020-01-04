package io.github.takusan23.androidwebserver

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStorageDirectory
import androidx.appcompat.app.AppCompatActivity
import fi.iki.elonen.NanoHTTPD
import kotlinx.android.synthetic.main.activity_main.*
import android.media.MediaMetadata.METADATA_KEY_ALBUM_ART_URI
import android.media.MediaMetadata.METADATA_KEY_MEDIA_URI
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.provider.Telephony.Carriers.PORT
import java.io.*


class MainActivity : AppCompatActivity() {

    lateinit var appCompatActivity: AppCompatActivity

    val PORT = 8080
    val READ_REQUEST_CODE = 114

    lateinit var inputStream: InputStream

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // SAF 召喚
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }

        startActivityForResult(intent, READ_REQUEST_CODE)


        // IPアドレス取得
        // https://techbooster.org/android/device/1376/
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val ip_addr_i = wifiInfo.getIpAddress()
        val ip_addr =
            (ip_addr_i shr 0 and 0xFF).toString() + "." + (ip_addr_i shr 8 and 0xFF) + "." + (ip_addr_i shr 16 and 0xFF) + "." + (ip_addr_i shr 24 and 0xFF)
        textview.text = "$ip_addr:$PORT"

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == READ_REQUEST_CODE) {
            val uri = data?.data
            inputStream = contentResolver.openInputStream(uri!!)!!
            val webServer = HttpServer(this)
            webServer.start()
        }
    }

    private inner class HttpServer(private val context: Context) : NanoHTTPD(PORT) {
        override fun serve(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
            return serveVideo()
        }

        private fun serveVideo(): NanoHTTPD.Response {
            var fis: InputStream? = null
            try {
                val path = getExternalFilesDir(null)?.path + "/test.mp3"
                fis = FileInputStream(path)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            return NanoHTTPD.newChunkedResponse(Response.Status.OK, "audio/mp3", fis)
        }
    }

}

