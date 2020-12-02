package com.duke.elliot.kim.kotlin.photodiary.drawer_items

import android.content.Context
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.duke.elliot.kim.kotlin.photodiary.R

const val PREFERENCES_THEME_OPTIONS = "photodiary_preferences_theme_options"
const val KEY_PRIMARY_THEME_COLOR = "photodiary_key_primary_theme_color"
const val KEY_SECONDARY_THEME_COLOR = "photodiary_key_secondary_theme_color"
const val KEY_NIGHT_MODE = "photodiary_key_dark_mode"

fun saveThemeColor(context: Context, primary: Int, secondary: Int) {
    val sharedPreferences = context.getSharedPreferences(PREFERENCES_THEME_OPTIONS, Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putInt(KEY_PRIMARY_THEME_COLOR, primary)
    editor.putInt(KEY_SECONDARY_THEME_COLOR, secondary)
    editor.apply()
}

@ColorInt
fun loadPrimaryThemeColor(context: Context): Int {
    val sharedPreferences =
        context.getSharedPreferences(PREFERENCES_THEME_OPTIONS, Context.MODE_PRIVATE)
    val color = sharedPreferences.getInt(KEY_PRIMARY_THEME_COLOR, -1)

    if (color == -1)
        return ContextCompat.getColor(context, R.color.colorDefaultThemePrimary)

    return color
}

@ColorInt
fun loadSecondaryThemeColor(context: Context): Int {
    val sharedPreferences =
        context.getSharedPreferences(PREFERENCES_THEME_OPTIONS, Context.MODE_PRIVATE)
    val color = sharedPreferences.getInt(KEY_SECONDARY_THEME_COLOR, -1)

    if (color == -1)
        return ContextCompat.getColor(context, R.color.colorDefaultThemeSecondary)

    return color
}

@ColorInt
fun getSecondaryThemeColor(context: Context, color: Int): Int {
    when(color) {
        getColor(context, R.color.colorRed200),
        getColor(context, R.color.colorRed500) ->
            return getColor(context, R.color.colorRed50)
        getColor(context, R.color.colorPink200),
        getColor(context, R.color.colorPink500) ->
            return getColor(context, R.color.colorPink50)
        getColor(context, R.color.colorPurple200),
        getColor(context, R.color.colorPurple500) ->
            return getColor(context, R.color.colorPurple50)
        getColor(context, R.color.colorDeepPurple200),
        getColor(context, R.color.colorDeepPurple500) ->
            return getColor(context, R.color.colorDeepPurple50)
        getColor(context, R.color.colorIndigo200),
        getColor(context, R.color.colorIndigo500) ->
            return getColor(context, R.color.colorIndigo50)
        getColor(context, R.color.colorBlue200),
        getColor(context, R.color.colorBlue500) ->
            return getColor(context, R.color.colorBlue50)
        getColor(context, R.color.colorLightBlue200),
        getColor(context, R.color.colorLightBlue500) ->
            return getColor(context, R.color.colorLightBlue50)
        getColor(context, R.color.colorCyan200),
        getColor(context, R.color.colorCyan500) ->
            return getColor(context, R.color.colorCyan50)
        getColor(context, R.color.colorTeal200),
        getColor(context, R.color.colorTeal500) ->
            return getColor(context, R.color.colorTeal50)
        getColor(context, R.color.colorGreen200),
        getColor(context, R.color.colorGreen500) ->
            return getColor(context, R.color.colorGreen50)
        getColor(context, R.color.colorLightGreen200),
        getColor(context, R.color.colorLightGreen500) ->
            return getColor(context, R.color.colorLightGreen50)
        getColor(context, R.color.colorLime200),
        getColor(context, R.color.colorLime500) ->
            return getColor(context, R.color.colorLime50)
        getColor(context, R.color.colorYellow200),
        getColor(context, R.color.colorYellow500) ->
            return getColor(context, R.color.colorYellow50)
        getColor(context, R.color.colorAmber200),
        getColor(context, R.color.colorAmber500) ->
            return getColor(context, R.color.colorAmber50)
        getColor(context, R.color.colorOrange200),
        getColor(context, R.color.colorOrange500) ->
            return getColor(context, R.color.colorOrange50)
        getColor(context, R.color.colorDeepOrange200),
        getColor(context, R.color.colorDeepOrange500) ->
            return getColor(context, R.color.colorDeepOrange50)
        getColor(context, R.color.colorBrown200),
        getColor(context, R.color.colorBrown500) ->
            return getColor(context, R.color.colorBrown50)
        getColor(context, R.color.colorBlueGrey200),
        getColor(context, R.color.colorBlueGrey500) ->
            return getColor(context, R.color.colorBlueGrey50)
    }

    return getColor(context, R.color.colorDefaultThemeSecondary)
}

private fun getColor(context: Context, id: Int) = ContextCompat.getColor(context, id)

fun getNightMode(context: Context): Int {
    val sharedPreferences = context.getSharedPreferences(PREFERENCES_THEME_OPTIONS, Context.MODE_PRIVATE)

    return sharedPreferences.getInt(KEY_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_NO)
}

fun saveNightMode(context: Context, nightMode: Int) {
    val sharedPreferences = context.getSharedPreferences(PREFERENCES_THEME_OPTIONS, Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    editor.putInt(KEY_NIGHT_MODE, nightMode)
    editor.apply()
}