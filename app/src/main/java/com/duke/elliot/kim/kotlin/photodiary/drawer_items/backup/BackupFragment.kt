package com.duke.elliot.kim.kotlin.photodiary.drawer_items.backup

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.duke.elliot.kim.kotlin.photodiary.MainActivity
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.base.BaseFragment
import com.duke.elliot.kim.kotlin.photodiary.database.DIARY_DATABASE_NAME
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDatabase
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentBackupBinding
import com.duke.elliot.kim.kotlin.photodiary.drive.DriveServiceHelper
import com.duke.elliot.kim.kotlin.photodiary.utility.OkCancelDialogFragment
import com.duke.elliot.kim.kotlin.photodiary.utility.showToast
import com.duke.elliot.kim.kotlin.photodiary.utility.toPath
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.*

private const val BACKUP_TO_INTERNAL_STORAGE = 0
private const val RESTORE_FROM_INTERNAL_STORAGE = 1
private const val BACKUP_TO_GOOGLE_DRIVE = 2
private const val RESTORE_FROM_GOOGLE_DRIVE = 3

private const val REQUEST_CODE_GOOGLE_SIGN_IN = 1403

class BackupFragment: BaseFragment() {

    private lateinit var binding: FragmentBackupBinding
    private lateinit var driveServiceHelper: DriveServiceHelper
    private lateinit var viewModel: BackupViewModel
    private var mode = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_backup, container, false)

        setSimpleBackButton(binding.toolbar)
        setBackupInformationUI()

        val viewModelFactory = BackupViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(viewModelStore, viewModelFactory)[BackupViewModel::class.java]

        binding.dataBackup.setOnClickListener {
            mode = BACKUP_TO_GOOGLE_DRIVE
            createPermissionListener(BACKUP_TO_GOOGLE_DRIVE)
        }

        binding.restoreData.setOnClickListener {
            mode = RESTORE_FROM_GOOGLE_DRIVE
            createPermissionListener(RESTORE_FROM_GOOGLE_DRIVE)
        }

        binding.backupToInternalStorage.setOnClickListener {
            createPermissionListener(BACKUP_TO_INTERNAL_STORAGE)
        }

        binding.restoreFromInternalStorage.setOnClickListener {
            createPermissionListener(RESTORE_FROM_INTERNAL_STORAGE)
        }

        return binding.root
    }

    private fun createPermissionListener(mode: Int) {
        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse) {
                when(mode) {
                    BACKUP_TO_GOOGLE_DRIVE -> requestGoogleSignIn()
                    RESTORE_FROM_GOOGLE_DRIVE -> requestGoogleSignIn()
                    BACKUP_TO_INTERNAL_STORAGE -> backup()
                    RESTORE_FROM_INTERNAL_STORAGE -> restore()
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

        requestPermissionDialog.show(
            requireActivity().supportFragmentManager,
            requestPermissionDialog.tag
        )
    }


    private fun backup() {
        CoroutineScope(Dispatchers.Default).launch {
            showProgressBar()

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

            if (result)
                showToast(requireContext(), getString(R.string.data_backup))
            else
                showToast(requireContext(), getString(R.string.data_backup_failed))

            dismissProgressBar()
        }
    }

    private fun restore() {
        CoroutineScope(Dispatchers.IO).launch {
            showProgressBar()

            val result =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    BackupUtil.restoreFromInternalZipFileQ(requireContext())
                else
                    @Suppress("DEPRECATION")
                    BackupUtil.restoreFromInternalZipFile(
                        requireContext(),
                        Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).path +
                                BACKUP_DIR_PATH + BACKUP_FILE_NAME
                    )

            if (result)
                showToast(requireContext(), getString(R.string.data_backup))
            else
                showToast(requireContext(), getString(R.string.data_backup_failed))

            dismissProgressBar()
            (requireActivity() as MainActivity).recreateNoAnimation()
        }
    }

    private fun requestGoogleSignIn() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        val client = GoogleSignIn.getClient(requireActivity(), signInOptions)

        startActivityForResult(client.signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_GOOGLE_SIGN_IN ->
                if (resultCode == RESULT_OK && data != null) {
                    setBackupInformationUI()
                    handleSignInResult(data)
                }
        }
    }

    private fun handleSignInResult(data: Intent) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
            .addOnSuccessListener { googleAccount: GoogleSignInAccount ->
                val credential = GoogleAccountCredential.usingOAuth2(
                    requireActivity(), Collections.singleton(DriveScopes.DRIVE_FILE)
                )

                credential.selectedAccount = googleAccount.account

                val googleDriveService = Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory(),
                    credential
                )
                    .setApplicationName("Drive API Migration")
                    .build()

                // The DriveServiceHelper encapsulates all REST API and SAF functionality.
                // Its instantiation is required before handling any onClick actions.
                driveServiceHelper = DriveServiceHelper(googleDriveService)

               // val kk =

                when(mode) {
                    BACKUP_TO_GOOGLE_DRIVE -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            showProgressBar()
                            driveServiceHelper.backup(
                                requireContext(),
                                viewModel.getAllMedia()
                            ) { result ->
                                dismissProgressBar()

                                if (result) {
                                    showToast(requireContext(), getString(R.string.data_backed_up))
                                } else {
                                    showToast(requireContext(), getString(R.string.data_backup_failed))
                                }
                            }
                        }
                    }
                    RESTORE_FROM_GOOGLE_DRIVE -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            val mediaList = viewModel.getAllMedia()
                            cacheCurrentDatabase()

                            showProgressBar()
                            driveServiceHelper.restore(requireContext()) { result, downloadedFilePaths ->
                                dismissProgressBar()

                                if (result) {
                                    deleteCachedDatabase()
                                    viewModel.deleteFiles(mediaList.map { it.uriString.toPath() }
                                        .requireNoNulls())
                                    showToast(requireContext(), getString(R.string.data_restored))
                                    (requireActivity() as MainActivity).recreateNoAnimation()

                                    // driveServiceHelper.showBackedUpFiles()
                                } else {
                                    restoreCurrentDatabase()
                                    downloadedFilePaths?.let { viewModel.deleteFiles(it) }
                                    showToast(requireContext(), getString(R.string.data_restore_failed))
                                }
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { e: Exception? ->
                Timber.e(e)
                showToast(requireContext(), getString(R.string.google_sign_in_failed))
            }
    }

    private fun cacheCurrentDatabase() {
        BackupUtil.copyFile(
            File(requireContext().getDatabasePath(DIARY_DATABASE_NAME).path),
            File(requireContext().cacheDir.path + "/$DIARY_DATABASE_NAME")
        )

        BackupUtil.copyFile(
            File(requireContext().getDatabasePath("$DIARY_DATABASE_NAME-shm").path),
            File(requireContext().cacheDir.path + "/$DIARY_DATABASE_NAME-shm")
        )

        BackupUtil.copyFile(
            File(requireContext().getDatabasePath("$DIARY_DATABASE_NAME-wal").path),
            File(requireContext().cacheDir.path + "/$DIARY_DATABASE_NAME-wal")
        )
    }

    private fun restoreCurrentDatabase() {
        BackupUtil.copyFile(
            File(requireContext().cacheDir.path + "/$DIARY_DATABASE_NAME"),
            File(requireContext().getDatabasePath(DIARY_DATABASE_NAME).path)
        )

        BackupUtil.copyFile(
            File(requireContext().cacheDir.path + "/$DIARY_DATABASE_NAME-shm"),
            File(requireContext().getDatabasePath("$DIARY_DATABASE_NAME-shm").path)
        )

        BackupUtil.copyFile(
            File(requireContext().cacheDir.path + "/$DIARY_DATABASE_NAME-wal"),
            File(requireContext().getDatabasePath("$DIARY_DATABASE_NAME-wal").path)
        )

        deleteCachedDatabase()
    }

    private fun deleteCachedDatabase() {
        File(requireContext().cacheDir.path + "/$DIARY_DATABASE_NAME").delete()
        File(requireContext().cacheDir.path + "/$DIARY_DATABASE_NAME-shm").delete()
        File(requireContext().cacheDir.path + "/$DIARY_DATABASE_NAME-wal").delete()
    }

    private fun setBackupInformationUI() {
        binding.backupAccount.text = getSignedInGoogleAccount() ?: ""
    }

    private fun getSignedInGoogleAccount() = GoogleSignIn.getLastSignedInAccount(activity)?.email
}