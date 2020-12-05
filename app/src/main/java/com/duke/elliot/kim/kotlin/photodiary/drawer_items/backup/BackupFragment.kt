package com.duke.elliot.kim.kotlin.photodiary.drawer_items.backup

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.base.BaseFragment
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentBackupBinding
import com.duke.elliot.kim.kotlin.photodiary.utility.OkCancelDialogFragment
import com.duke.elliot.kim.kotlin.photodiary.utility.showToast
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

private const val BACKUP_DATA = 0
private const val RESTORE_BACKUP_DATA = 1

class BackupFragment: BaseFragment() {
    private lateinit var binding: FragmentBackupBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_backup, container, false)

        binding.backupToInternalStorage.setOnClickListener {
            createPermissionListener(BACKUP_DATA)
        }

        binding.restoreFromInternalStorage.setOnClickListener {
            createPermissionListener(RESTORE_BACKUP_DATA)
        }

        return binding.root
    }

    private fun createPermissionListener(mode: Int) {
        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse) {
                when(mode) {
                    BACKUP_DATA ->  BackupUtil.exportDatabaseFile(requireContext())
                    RESTORE_BACKUP_DATA -> BackupUtil.importDatabaseFile(requireContext())
                }
            }

            override fun onPermissionDenied(response: PermissionDeniedResponse) {
                showToast(requireContext(), "DDDD")
                // TODO make ok dialog.

            }

            override fun onPermissionRationaleShouldBeShown(
                permission: PermissionRequest?,
                token: PermissionToken?
            ) {
                token?.let { showPermissionRationale(it) }
            }
        }

        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(permissionListener)
            .check()
    }

    fun showPermissionRationale(token: PermissionToken) {
        val requestPermissionDialog = OkCancelDialogFragment().apply {
            setDialogParameters(
                binding.root.context.getString(R.string.permission_request),
                binding.root.context.getString(R.string.write_external_storage_permission_request_message)
            ) {
                token.continuePermissionRequest()
            }

            setCancelClickEvent {
                token.cancelPermissionRequest()
            }
        }

        requestPermissionDialog.show(requireActivity().supportFragmentManager, requestPermissionDialog.tag)
    }
}