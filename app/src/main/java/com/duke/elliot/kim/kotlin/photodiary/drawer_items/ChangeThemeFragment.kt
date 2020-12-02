package com.duke.elliot.kim.kotlin.photodiary.drawer_items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.duke.elliot.kim.kotlin.photodiary.MainActivity
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.base.BaseFragment
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentChangeThemeBinding
import com.duke.elliot.kim.kotlin.photodiary.utility.showToast
import kotlinx.coroutines.*
import petrov.kristiyan.colorpicker.ColorPicker

class ChangeThemeFragment: BaseFragment() {

    private lateinit var binding: FragmentChangeThemeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_change_theme,
            container,
            false
        )

        applyPrimaryThemeColor(binding.toolbar)
        setSimpleBackButton(binding.toolbar)

        val themeColors = requireContext().resources.getIntArray(R.array.theme_colors).toList()
        val hexColors = themeColors.map { String.format("#%06X", 0xFFFFFF and it) } as ArrayList

        binding.currentThemeColor.setCardBackgroundColor(MainActivity.themeColorPrimary)
        binding.changeThemeColor.setOnClickListener {
            val colorPicker = ColorPicker(requireActivity())
            colorPicker.setOnChooseColorListener(object : ColorPicker.OnChooseColorListener {
                override fun onChooseColor(position: Int, color: Int) {
                    MainActivity.themeColorPrimary = color
                    MainActivity.themeColorSecondary = getSecondaryThemeColor(requireContext(), color)

                    saveThemeColor(requireContext(), MainActivity.themeColorPrimary, MainActivity.themeColorSecondary)
                    binding.currentThemeColor.setCardBackgroundColor(MainActivity.themeColorPrimary)
                    applyPrimaryThemeColor(binding.toolbar)
                }

                override fun onCancel() {  }
            })
                .setTitle(getString(R.string.change_theme_color))
                .setColumns(6)
                .setColorButtonMargin(2, 2, 2, 2)
                .setColorButtonDrawable(R.drawable.background_white_rounded_corners)
                .setColors(hexColors)
                .setDefaultColorButton(MainActivity.themeColorPrimary)
                .show()
        }

        binding.switchNightMode.isClickable = false
        binding.switchNightMode.isFocusable = false
        binding.nightMode.setOnClickListener {
            binding.switchNightMode.toggle()

            val nightMode =
                if (binding.switchNightMode.isChecked)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO

            saveNightMode(requireContext(), nightMode)
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
           binding.textSystemNightModeTitle.text = getString(R.string.system_night_mode)
           binding.textSystemNightModeMessage.text = getString(R.string.system_night_mode_message)
        } else {
            binding.textSystemNightModeTitle.text = getString(R.string.battery_saver_mode)
            binding.textSystemNightModeMessage.text = getString(R.string.battery_saver_mode_message)
        }

        binding.switchSystemNightMode.isClickable = false
        binding.switchSystemNightMode.isFocusable = false
        binding.systemNightMode.setOnClickListener {
            binding.switchSystemNightMode.toggle()

            if (binding.switchSystemNightMode.isChecked) {
                val nightMode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                } else
                    AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY

                saveNightMode(requireContext(), nightMode)
                binding.nightMode.isEnabled = false
                binding.switchNightMode.isChecked = false
            } else {
                binding.nightMode.isEnabled = true

                if (binding.switchNightMode.isChecked) {
                    saveNightMode(requireContext(), AppCompatDelegate.MODE_NIGHT_YES)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    saveNightMode(requireContext(), AppCompatDelegate.MODE_NIGHT_NO)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        when (getNightMode(requireContext())) {
            AppCompatDelegate.MODE_NIGHT_YES -> {
                binding.switchNightMode.isChecked = true
                binding.switchSystemNightMode.isChecked = false
            }
            AppCompatDelegate.MODE_NIGHT_NO -> {
                binding.switchNightMode.isChecked = false
                binding.switchSystemNightMode.isChecked = false
            }
            else -> {
                binding.switchNightMode.isChecked = false
                binding.switchSystemNightMode.isChecked = true
                binding.nightMode.isEnabled = false
            }
        }

    }
}