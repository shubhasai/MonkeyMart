package com.shubhasai.monkeymart.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CancellationException

/** Opens the camera to scan a coupon code at checkout. */
object CouponScanner {
    const val MSG_PERMISSION_NEEDED = "Camera permission is needed to scan coupons"
    const val MSG_CAMERA_UNAVAILABLE = "Camera is unavailable right now"

    /** Asks the OS every time: the user can revoke the permission in Settings at any moment. */
    fun hasCameraAccess(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    fun open(context: Context, onResult: (String) -> Unit) =
        open(hasCameraAccess(context), onResult) { report -> openCameraDevice(context, report) }

    internal fun open(hasPermission: Boolean, onResult: (String) -> Unit, openDevice: ((String) -> Unit) -> Unit) {
        if (!hasPermission) return onResult(MSG_PERMISSION_NEEDED)
        try {
            openDevice(onResult)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // SecurityException if permission was revoked, CameraAccessException if the camera is busy/disabled.
            onResult(MSG_CAMERA_UNAVAILABLE)
        }
    }

    @SuppressLint("MissingPermission")
    private fun openCameraDevice(context: Context, onResult: (String) -> Unit) {
        val cameras = context.getSystemService(CameraManager::class.java)
        val cameraId = cameras?.cameraIdList?.firstOrNull() ?: return onResult("No camera on this device")
        cameras.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                camera.close()
                onResult("Scanner ready: point the camera at your coupon")
            }

            override fun onDisconnected(camera: CameraDevice) {
                camera.close()
                onResult(MSG_CAMERA_UNAVAILABLE)
            }

            override fun onError(camera: CameraDevice, error: Int) {
                camera.close()
                onResult("Camera error $error")
            }
        }, Handler(Looper.getMainLooper()))
    }
}

@Composable
fun ScanCouponButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var status by rememberSaveable { mutableStateOf("") }
    val askCamera = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            CouponScanner.open(context) { status = it }
        } else {
            status = CouponScanner.MSG_PERMISSION_NEEDED
        }
    }

    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(
            onClick = {
                if (CouponScanner.hasCameraAccess(context)) {
                    CouponScanner.open(context) { status = it }
                } else {
                    askCamera.launch(Manifest.permission.CAMERA)
                }
            },
            modifier = Modifier.testTag("scan_coupon"),
        ) { Text("Scan coupon") }
        Text(status, modifier = Modifier.weight(1f).testTag("coupon_status"))
    }
}
