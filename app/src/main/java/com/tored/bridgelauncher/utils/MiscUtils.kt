package com.tored.bridgelauncher.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Environment
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import com.tored.bridgelauncher.annotations.Display
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation

fun <E> MutableList<E>.addAll(vararg items: E)
{
    this.addAll(items)
}

fun Context.checkStoragePerms(): Boolean
{
    return if (CurrentAndroidVersion.supportsScopedStorage())
    {
        // we need a special permission on Android 11 and up
        Environment.isExternalStorageManager()
    }
    else
    {
        ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
}

fun Context.startExtStorageManagerPermissionActivity(): Unit
{
    if (CurrentAndroidVersion.supportsScopedStorage() && !Environment.isExternalStorageManager())
    {
        try
        {
            startActivity(
                Intent(
                    android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    android.net.Uri.parse("package:${packageName}")
                )
            )
        }
        catch (ex: Exception)
        {
            Toast.makeText(this, "Could not navigate to settings to grant access to all files.", android.widget.Toast.LENGTH_LONG).show()
        }
    }
}

typealias ComposableContent = @Composable () -> Unit

fun <TClass, TProp> displayNameFor(prop: KProperty1<TClass, TProp>): String
{
    val ann = prop.findAnnotation<Display>()
    return ann?.name ?: prop.name
}


fun Exception.messageOrDefault(): String
{
    return message.defaultIfNullOrEmpty(this.javaClass.name)
}

fun String?.defaultIfNullOrEmpty(default: String): String
{
    return if (isNullOrEmpty()) default else this
}

fun Context.showErrorToast(ex: Exception)
{
    showErrorToast(ex.messageOrDefault())
}

fun Context.showErrorToast(message: String?)
{
    Toast.makeText(this, message ?: "Exception with no message.", Toast.LENGTH_LONG).show()
}

fun Context.getIsSystemInNightMode(): Boolean
{
    return if (CurrentAndroidVersion.supportsNightMode())
    {
        resources.configuration.isNightModeActive
    }
    else
    {
        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
}

/** Quote - wraps the given string in quotation marks. */
fun q(s: String?) = "\"$s\""

object EncodingStrings
{
    const val UTF8 = "utf-8"
}