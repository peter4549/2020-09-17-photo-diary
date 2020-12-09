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
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDatabase
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentBackupBinding
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryWritingViewModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryWritingViewModelFactory
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.MediaModel
import com.duke.elliot.kim.kotlin.photodiary.drive.DriveServiceHelper
import com.duke.elliot.kim.kotlin.photodiary.utility.FileUtilities
import com.duke.elliot.kim.kotlin.photodiary.utility.OkCancelDialogFragment
import com.duke.elliot.kim.kotlin.photodiary.utility.showToast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
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
import kotlinx.coroutines.*
import timber.log.Timber
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
    private var fileId = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_backup, container, false)

        setSimpleBackButton(binding.toolbar)

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
                showToast(requireContext(), "true")
            else
                showToast(requireContext(), "false")

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
                showToast(requireContext(), "true")
            else
                showToast(requireContext(), "false")

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
                        CoroutineScope(Dispatchers.Default).launch {
                            showProgressBar()

                            driveServiceHelper.backupDatabaseFiles(requireContext())?.let {

                                val diaries = DiaryDatabase.getInstance(requireContext()).diaryDao().getAllValues()
                                val mediaList = mutableListOf<MediaModel>()
                                for (diary in diaries) {
                                    mediaList += diary.mediaArray
                                }
                                driveServiceHelper.backupMediaFiles(mediaList).let {
                                    if (it)
                                        showToast(requireContext(), "데이터파일 백업 성공.")
                                    else
                                        showToast(requireContext(), "데이터파일 백업 실패.")
                                }
                            } ?: run {
                                showToast(requireContext(), "데이터파일 백업 실패.")
                            }

                            dismissProgressBar()
                        }

                        /*
                        CoroutineScope(Dispatchers.IO).launch {
                            driveServiceHelper.backupDatabaseFiles(requireContext())
                        }

                         */
                    }
                    RESTORE_FROM_GOOGLE_DRIVE -> {
                        showProgressBar()
                        driveServiceHelper.restore(requireContext()) { result, downloadedFilePaths ->
                            dismissProgressBar()

                            if (result) { // 성공.
                                (requireActivity() as MainActivity).recreateNoAnimation()
                                // TODO: 현재 데이터 파일 제거. 마찬가지로 미디어 목록갖고있어야함..
                                // TODO: origin files 경로를 갖고 있어야한다. 오리진 파일들을 제거.
                            } else {
                                /** 실패한 경우 */

                                downloadedFilePaths?.let { viewModel.deleteFiles(it) }
                                // origin 파일들은 건드릴 필요 없다.
                                // db파일만 다시 붙여 넣으면 된다.
                                restoreCurrentDatabase()
                            }
                        }

                        CoroutineScope(Dispatchers.IO).launch {

                            /*
                            driveServiceHelper.readFile(fileId).addOnSuccessListener { ii ->
                                showToast(requireContext(), "SSS " + ii)
                            }.addOnFailureListener {
                                showToast(requireContext(), "Falss ${fileId} " + it.message.toString())
                            }

                             */

                            driveServiceHelper.showAppFiles()


                            /*
                            val s = driveServiceHelper.queryFiles()?.addOnSuccessListener {
                                showToast(requireContext(), it.toString())
                                val kk = it?.files
                                var s = 0

                                for (file in kk ?: listOf()) {
                                    println("zzzzzzz $s: ${file.id} ${file.name}")
                                    s += 1

                                    val k = file

                                    driveServiceHelper.readFile(fileId).addOnSuccessListener { ii ->
                                        showToast(requireContext(), "SSS " + ii)
                                    }.addOnFailureListener {
                                        showToast(requireContext(), "Falss ${file.name} ${file.id} " + it.message.toString())
                                    }
                                }


                            }?.addOnFailureListener {
                                showToast(requireContext(), it.message.toString())
                            }

                             */

                        }
                    }
                }
                // showToast(requireContext(), "$kk")
            }
            .addOnFailureListener { e: Exception? ->
                Timber.e(e)
                showToast(requireContext(), "실패.")
            }


    }

    private fun initDriveServiceHelper(googleSignInOptions: GoogleSignInOptions) {
        val credential = GoogleAccountCredential.usingOAuth2(
            requireActivity(), Collections.singleton(DriveScopes.DRIVE_FILE)
        )

        credential.selectedAccount = googleSignInOptions.account

        val googleDriveService = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential
        )
            .setApplicationName("Drive API Migration")
            .build()

        driveServiceHelper = DriveServiceHelper(googleDriveService)
    }

    private fun cacheCurrentDatabase() {

    }

    private fun restoreCurrentDatabase() {

    }
}