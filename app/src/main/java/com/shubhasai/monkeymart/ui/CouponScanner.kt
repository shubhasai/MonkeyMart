package com.shubhasai.monkeymart.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

/** Opens the camera to scan a coupon code at checkout. */
object CouponScanner {
    fun hasCameraAccess(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun open(context: Context, onPermissionDenied: () -> Unit, onResult: (String) -> Unit) {
        try {
            val cameras = context.getSystemService(CameraManager::class.java)
            val cameraId = cameras.cameraIdList.firstOrNull() ?: return onResult("No camera on this device")
            cameras.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    camera.close()
                    onResult("Scanner ready: point the camera at your coupon")
                }

                override fun onDisconnected(camera: CameraDevice) = camera.close()

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    onResult("Camera error $error")
                }
            }, Handler(Looper.getMainLooper()))
        } catch (e: SecurityException) {
            onPermissionDenied()
        } catch (e: CameraAccessException) {
            onResult("Camera unavailable")
        }
    }
}

@Composable
fun ScanCouponButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var status by remember { mutableStateOf("") }
    val askCamera = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            CouponScanner.open(context, onPermissionDenied = { status = "Camera permission is needed to scan coupons" }) { status = it }
        } else {
            status = "Camera permission is needed to scan coupons"
        }
    }

    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(
            onClick = {
                if (CouponScanner.hasCameraAccess(context)) {
                    CouponScanner.open(context, onPermissionDenied = {
                        status = "Camera permission is needed to scan coupons"
                        askCamera.launch(Manifest.permission.CAMERA)
                    }) { status = it }
                } else {
                    askCamera.launch(Manifest.permission.CAMERA)
                }
            },
            modifier = Modifier.testTag("scan_coupon"),
        ) { Text("Scan coupon") }
        Text(status, modifier = Modifier.weight(1f).testTag("coupon_status"))
    }
}
