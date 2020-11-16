package com.duke.elliot.kim.kotlin.photodiary.export

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentKakaoTalkOptionDialogBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class KakaoTalkOptionBottomSheetDialogFragment: BottomSheetDialogFragment() {

    private lateinit var binding: FragmentKakaoTalkOptionDialogBottomSheetBinding
    private lateinit var kakaoTalkOptionClickListener: KakaoTalkOptionClickListener

    interface KakaoTalkOptionClickListener {
        fun onSendImagesClick()
        fun onSendVideoClick()
        fun onSendAudioClick()
        fun onSendTextClick()
    }

    fun setKakaoTalkOptoinClickListener(kakaoTalkOptionClickListener: KakaoTalkOptionClickListener) {
        this.kakaoTalkOptionClickListener = kakaoTalkOptionClickListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_kakao_talk_option_dialog_bottom_sheet, container, false)

        binding.sendImages.setOnClickListener {
            kakaoTalkOptionClickListener.onSendImagesClick()
        }

        binding.sendVideo.setOnClickListener {
            kakaoTalkOptionClickListener.onSendVideoClick()
        }

        binding.sendAudio.setOnClickListener {
            kakaoTalkOptionClickListener.onSendAudioClick()
        }

        binding.sendText.setOnClickListener {
            kakaoTalkOptionClickListener.onSendTextClick()
            dismiss()
        }

        return binding.root
    }
}