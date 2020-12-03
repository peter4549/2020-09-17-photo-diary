package com.duke.elliot.kim.kotlin.photodiary.drawer_items.lock_screen

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.beautycoder.pflockscreen.PFFLockScreenConfiguration
import com.beautycoder.pflockscreen.fragments.PFLockScreenFragment
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentSecurityQuestionsDialogBinding
import com.duke.elliot.kim.kotlin.photodiary.utility.showToast

const val SET_SECURITY_QUESTION = 0
const val AUTHORIZE_SECURITY_QUESTION = 1

class SecurityQuestionsDialogFragment: DialogFragment() {

    private lateinit var binding: FragmentSecurityQuestionsDialogBinding
    private lateinit var title: String
    private var mode: Int? = null
    private var containerViewId: Int? = null

    fun setTitle(title: String) {
        this.title = title
    }

    fun setMode(mode: Int) {
        this.mode = mode
    }

    fun setContainerViewId(containerViewId: Int) {
        this.containerViewId = containerViewId
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_security_questions_dialog, container, false)

        val securityQuestions = resources.getStringArray(R.array.security_questions)
        var selectedSecurityQuestionPosition = -1

        binding.textTitle.text = title

        binding.spinnerSecurityQuestions.adapter =
            ArrayAdapter(requireContext(), R.layout.item_security_questions_spinner, securityQuestions)

        binding.spinnerSecurityQuestions.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSecurityQuestionPosition = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {  }
        }

        binding.buttonPositive.setOnClickListener {
            val answer = binding.editTextAnswer.text.toString()
            val securityQuestionIndex = selectedSecurityQuestionPosition

            val securityQuestionIndexAnswerPair = LockScreenHelper.loadSecurityQuestionIndexAnswerPair(it.context)
            val correctSecurityQuestionIndex = securityQuestionIndexAnswerPair?.first ?: -1
            val correctAnswer = securityQuestionIndexAnswerPair?.second

            when(mode) {
                SET_SECURITY_QUESTION -> {
                    if (answer.isNotBlank()) {
                        LockScreenHelper.saveSecurityQuestionIndexAnswerPair(
                            it.context,
                            securityQuestionIndex,
                            answer
                        )
                        showToast(binding.root.context, getString(R.string.security_question_set))
                        dismiss()
                    }
                    else
                        showToast(binding.root.context, getString(R.string.enter_your_security_question_answer))
                }
                AUTHORIZE_SECURITY_QUESTION -> {
                    if (correctSecurityQuestionIndex == -1 || correctAnswer.isNullOrBlank())
                        showToast(it.context, getString(R.string.security_question_is_not_set))

                    if (answer.isNotBlank()) {
                        if (securityQuestionIndex == correctSecurityQuestionIndex &&
                            answer == correctAnswer) {
                            dismiss()
                            containerViewId?.let { id -> showCreateNewCodeLockScreen(id) }
                        } else {
                            binding.textInputLayoutAnswer.isErrorEnabled = true
                            binding.textInputLayoutAnswer.error = getString(R.string.security_question_do_not_match)
                        }
                    } else {
                        binding.textInputLayoutAnswer.isErrorEnabled = true
                        binding.textInputLayoutAnswer.error = getString(R.string.enter_your_security_question_answer)
                    }
                }
            }
        }

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    private fun showCreateNewCodeLockScreen(containerViewId: Int) {
        val activity = requireActivity()
        val pfLockScreenFragment = PFLockScreenFragment()
        val builder = PFFLockScreenConfiguration.Builder(activity)
            .setMode(PFFLockScreenConfiguration.MODE_CREATE)
            .setUseFingerprint(true)
            .setTitle(getString(R.string.please_enter_a_new_password))
            .setNextButton(getString(R.string.ok))

        pfLockScreenFragment.setConfiguration(builder.build())
        pfLockScreenFragment.setCodeCreateListener(object :
            PFLockScreenFragment.OnPFLockScreenCodeCreateListener {
            override fun onCodeCreated(encodedCode: String) {
                LockScreenHelper.saveEncodedPinCode(activity, encodedCode)
                showToast(activity, activity.getString(R.string.password_changed))
                activity.supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }

            override fun onNewCodeValidationFailed() {  }
        })

        activity.supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(0, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
            .replace(containerViewId, pfLockScreenFragment)
            .commit()
    }
}