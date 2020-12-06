package com.duke.elliot.kim.kotlin.photodiary.drawer_items.backup

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.duke.elliot.kim.kotlin.photodiary.MainActivity
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.base.BaseFragment
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDatabase
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentBackupBinding
import com.duke.elliot.kim.kotlin.photodiary.utility.OkCancelDialogFragment
import com.duke.elliot.kim.kotlin.photodiary.utility.ProgressDialogFragment
import com.duke.elliot.kim.kotlin.photodiary.utility.showToast
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        setSimpleBackButton(binding.toolbar)

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
                    BACKUP_DATA ->  {
                        backup()
                    }
                    RESTORE_BACKUP_DATA -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            withContext(Dispatchers.Main) {
                                showProgressBar()
                            }

                            BackupUtil.restoreFromInternalZipFile(
                                requireContext(),
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).path + BACKUP_DIR_PATH + BACKUP_FILE_NAME
                            )

                            withContext(Dispatchers.Main) {
                                dismissProgressBar()
                                (requireActivity() as MainActivity).recreateNoAnimation()
                            }
                        }
                    }
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


    private fun backup() {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                showProgressBar()
            }

            val diaries = DiaryDatabase.getInstance(requireContext()).diaryDao().getAllValues()
            val mediaAbsolutePaths = mutableListOf<String>()
            for (diary in diaries) {
                mediaAbsolutePaths += diary.getAllMediaAbsolutePaths()
            }

            val result =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    BackupUtil.backupToInternalZipFileQ(requireContext(), mediaAbsolutePaths)
                else {
                    @Suppress("DEPRECATION")
                    val backupDirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).path + BACKUP_DIR_PATH

                    BackupUtil.backupToInternalZipFile(
                        requireContext(),
                        mediaAbsolutePaths,
                        backupDirPath,
                        BACKUP_FILE_NAME
                    )
                }
            if (result) {
                showToast(requireContext(), "true")
            } else {
                showToast(requireContext(), "false")
            }

            withContext(Dispatchers.Main) {
                dismissProgressBar()
            }
        }
    }
}