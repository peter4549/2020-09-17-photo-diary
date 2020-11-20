package com.duke.elliot.kim.kotlin.photodiary.calendar

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentRegisterAnniversaryDialogBinding
import com.duke.elliot.kim.kotlin.photodiary.utility.convertDpToPx
import com.duke.elliot.kim.kotlin.photodiary.utility.scaleHideDown
import com.duke.elliot.kim.kotlin.photodiary.utility.scaleShowUp
import com.duke.elliot.kim.kotlin.photodiary.utility.toDateFormat
import java.sql.Date
import java.time.LocalDate
import java.time.ZoneId

class RegisterAnniversaryDialogFragment: DialogFragment() {

    private lateinit var anniversary: AnniversaryModel
    private lateinit var binding: FragmentRegisterAnniversaryDialogBinding
    private lateinit var localDate: LocalDate
    private lateinit var buttonClickListener: OnButtonClickListener
    private lateinit var relativeLayouts: Array<RelativeLayout>
    private lateinit var colors: Array<Int>
    private lateinit var layoutParams: ViewGroup.LayoutParams
    private var anniversaryFlag = REGISTER_ANNIVERSARY
    private var px24 = 0
    private var pickedColorIndex = 0

    interface OnButtonClickListener {
        fun onOkClick(anniversary: AnniversaryModel)
    }

    fun setAnniversaryFlag(anniversaryFlag: Int) {
        this.anniversaryFlag = anniversaryFlag
    }

    fun setButtonClickListener(buttonClickListener: OnButtonClickListener) {
        this.buttonClickListener = buttonClickListener
    }

    fun setLocalDate(localDate: LocalDate) {
        this.localDate = localDate
    }

    // Edit
    fun setAnniversary(anniversary: AnniversaryModel) {
        this.anniversary = anniversary
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_register_anniversary_dialog,
            container,
            false
        )

        val defaultZoneId = ZoneId.systemDefault()
        val date = Date.from(localDate.atStartOfDay(defaultZoneId).toInstant())

        binding.textDate.text = date.time.toDateFormat(getString(R.string.year_month_day_format))
        binding.textButtonOk.setOnClickListener {
            if (anniversaryFlag == REGISTER_ANNIVERSARY)
                buttonClickListener.onOkClick(createAnniversary())
            else if (anniversaryFlag == UPDATE_ANNIVERSARY)
                buttonClickListener.onOkClick(anniversary.apply {
                    this.title = binding.editTextAnniversaryName.text.toString()
                    this.color = colors[pickedColorIndex]
                    this.annual =  binding.checkBoxAnnual.isChecked
                })

            val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.root.windowToken, 0)
            dismiss()
        }

        binding.textButtonCancel.setOnClickListener {
            dismiss()
        }

        px24 = convertDpToPx(requireContext(), 24F).toInt()
        layoutParams = ViewGroup.LayoutParams(px24, px24)
        initColorPicker()

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    private fun initColorPicker() {
        relativeLayouts = arrayOf(
            binding.red,
            binding.orange, binding.yellow, binding.green,
            binding.blue, binding.indigo, binding.violet,
        )

        colors = arrayOf(
            getColor(R.color.rainbowRed),
            getColor(R.color.rainbowOrange),
            getColor(R.color.rainbowYellow),
            getColor(R.color.rainbowGreen),
            getColor(R.color.rainbowBlue),
            getColor(R.color.rainbowIndigo),
            getColor(R.color.rainbowViolet)
        )

        val image = ImageView(requireContext())
        image.layoutParams = layoutParams
        image.setImageResource(R.drawable.ic_sharp_check_circle_24)
        image.setColorFilter(ContextCompat.getColor(requireContext(), R.color.anniversaryColorPickerWhite))

        relativeLayouts[pickedColorIndex].addView(image)
        image.scaleShowUp(0F, 1F, 200L)

        for ((index, _) in relativeLayouts.withIndex()) {
            relativeLayouts[index].setOnClickListener {
                showCheckCircle(index)
            }
        }
    }

    @ColorInt
    private fun getColor(colorRes: Int) = ContextCompat.getColor(requireContext(), colorRes)

    private fun showCheckCircle(index: Int) {
        if (index == pickedColorIndex)
            return

        val image = ImageView(requireContext())
        image.layoutParams = layoutParams
        image.setImageResource(R.drawable.ic_sharp_check_circle_24)
        image.setColorFilter(ContextCompat.getColor(requireContext(), R.color.anniversaryColorPickerWhite))

        relativeLayouts[index].addView(image)
        image.scaleShowUp(0F, 1F, 200L)
        relativeLayouts[pickedColorIndex].getChildAt(0).scaleHideDown(0F, 1F, 200L)
        relativeLayouts[pickedColorIndex].removeAllViews()

        pickedColorIndex = index
    }

    private fun createAnniversary(): AnniversaryModel {
        val title = binding.editTextAnniversaryName.text.toString()
        val annual = binding.checkBoxAnnual.isChecked
        val color = colors[pickedColorIndex]

        return AnniversaryModel(
            year = localDate.year,
            month = localDate.monthValue,
            day = localDate.dayOfMonth,
            title = title,
            color = color,
            annual = annual
        )
    }
}