package com.tored.bridgelauncher.api2.server.endpoints

import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import com.tored.bridgelauncher.api2.server.HTTPStatusCode
import com.tored.bridgelauncher.api2.server.IBridgeServerEndpoint
import com.tored.bridgelauncher.api2.server.errorResponse
import com.tored.bridgelauncher.api2.server.jsonResponse
import com.tored.bridgelauncher.services.apps.InstalledAppsHolder
import com.tored.bridgelauncher.utils.q
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream

class AdaptiveIconLayersEndpoint(private val _apps: InstalledAppsHolder) : IBridgeServerEndpoint {
    companion object {
        const val QUERY_PACKAGE_NAME = "packageName"
        const val QUERY_ICON_PACK_PACKAGE_NAME = "iconPackPackageName"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun handle(req: WebResourceRequest): WebResourceResponse {
        val packageName = req.url.getQueryParameter(QUERY_PACKAGE_NAME)
        if (packageName.isNullOrEmpty()) {
            return errorResponse(HTTPStatusCode.BadRequest, "Missing packageName query param")
        }

        val app = _apps.packageNameToInstalledAppMap[packageName]
        if (app == null) {
            return errorResponse(HTTPStatusCode.NotFound, "App not found: ${q(packageName)}")
        }

        val icon = app.defaultIcon
        if (icon is AdaptiveIconDrawable) {
            val fg = icon.foreground
            val bg = icon.background
            val fgPng = drawableToPngBase64(fg)
            val bgPng = drawableToPngBase64(bg)
            val result = AdaptiveIconLayersResponse(fgPng, bgPng)
            return jsonResponse(Json.encodeToString(result))
        } else {
            val fgPng = drawableToPngBase64(icon)
            val bgPng = drawableToPngBase64(ColorDrawable(0xFFFFFFFF.toInt()))
            val result = AdaptiveIconLayersResponse(fgPng, bgPng)
            return jsonResponse(Json.encodeToString(result))
        }
    }

    private fun drawableToPngBase64(drawable: Drawable): String {
        val bitmap = drawable.toBitmap()
        val stream = ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
        return android.util.Base64.encodeToString(stream.toByteArray(), android.util.Base64.NO_WRAP)
    }

    @Serializable
    data class AdaptiveIconLayersResponse(val foreground: String, val background: String)
}
