package com.duke.elliot.kim.kotlin.photodiary.base

import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.duke.elliot.kim.kotlin.photodiary.MainActivity
import com.duke.elliot.kim.kotlin.photodiary.utility.ProgressDialogFragment

open class BaseFragment: Fragment() {
    private lateinit var progressBar: ProgressDialogFragment

    protected fun setSimpleBackButton(toolbar: Toolbar) {
        (requireActivity() as MainActivity).setSupportActionBar(toolbar)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
    }

    protected fun showProgressBar() {
        progressBar = ProgressDialogFragment.instance
        progressBar.show(requireActivity().supportFragmentManager, progressBar.tag)
    }

    protected fun dismissProgressBar() {
        if (::progressBar.isInitialized)
            progressBar.dismiss()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> findNavController().popBackStack()
        }

        return super.onOptionsItemSelected(item)
    }

    protected fun applyPrimaryThemeColor(vararg views: View) {
        for (view in views) {
            view.setBackgroundColor(MainActivity.themeColorPrimary)
            view.invalidate()
        }
    }

    protected fun applySecondaryThemeColor(vararg views: View) {
        for (view in views) {
            view.setBackgroundColor(MainActivity.themeColorSecondary)
            view.invalidate()
        }
    }
}