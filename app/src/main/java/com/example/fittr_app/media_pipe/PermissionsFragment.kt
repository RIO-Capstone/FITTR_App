package com.example.fittr_app.media_pipe

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.example.fittr_app.R

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_IMAGES)

class PermissionsFragment : Fragment() {

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGranted: Map<String,Boolean> ->
            if (isGranted.values.all { it }) { // similar to python's all function --> Checking if ALL permissions are True (granted)
                Toast.makeText(
                    context,
                    "Permissions granted",
                    Toast.LENGTH_LONG
                ).show()
                navigateToGallery()
            } else {
                Toast.makeText(
                    context,
                    "Permission request denied",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionsIfNeeded()
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermissionsIfNeeded() {
        // Filter out permissions that are not yet granted
        val missingPermissions = PERMISSIONS_REQUIRED.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            // Request the missing permissions
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        } else {
            // All permissions are already granted
            navigateToGallery()
        }
    }

    private fun navigateToGallery() {
        lifecycleScope.launchWhenStarted {
            Navigation.findNavController(requireActivity(), R.id.fragment_container)
                .navigate(R.id.action_permissions_to_gallery)
        }
    }


//    private fun navigateToCamera() {
//        lifecycleScope.launchWhenStarted {
//            Navigation.findNavController(
//                requireActivity(),
//                R.id.fragment_container
//            ).navigate(
//                R.id.action_permissions_to_camera
//            )
//        }
//    }

    companion object {

        /** Convenience method used to check if all permissions required by this app are granted */
        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
