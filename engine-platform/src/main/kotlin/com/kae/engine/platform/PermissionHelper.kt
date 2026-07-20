package com.kae.engine.platform

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionHelper(private val activity: Activity) {

    fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(permission: String, requestCode: Int) {
        if (!hasPermission(permission)) {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
        }
    }

    fun requestPermissions(permissions: Array<String>, requestCode: Int) {
        val ungrantedPermissions = permissions.filter { !hasPermission(it) }.toTypedArray()
        if (ungrantedPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, ungrantedPermissions, requestCode)
        }
    }

    fun shouldShowRationale(permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    companion object {
        const val REQUEST_CODE_PERMISSIONS = 1001
    }
}
