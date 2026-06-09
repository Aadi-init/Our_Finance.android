package com.altf4.ourfinance.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

object IconSwitcher {

    private const val TAG = "IconSwitcher"
    private const val ALIAS_LIGHT = "com.altf4.ourfinance.MainActivityLight"
    private const val ALIAS_DARK = "com.altf4.ourfinance.MainActivityDark"

    /**
     * Resolves and updates the current home screen launcher icon component configuration.
     *
     * @param context Application context.
     * @param isDynamicSwitchEnabled True if the icon matches the app's internal theme mode.
     * @param isAppInDarkMode True if the app's native layout design is in dark mode.
     */
    fun updateAppIcon(context: Context, isDynamicSwitchEnabled: Boolean, isAppInDarkMode: Boolean) {
        // Business Logic Rules:
        // 1. If switch is DISABLED -> Always resolve to the Dark Icon component.
        // 2. If switch is ENABLED  -> Match the app's native theme mode layout.
        val targetAlias = if (isDynamicSwitchEnabled) {
            if (isAppInDarkMode) ALIAS_DARK else ALIAS_LIGHT
        } else {
            ALIAS_DARK
        }

        val aliasToDisable = if (targetAlias == ALIAS_LIGHT) ALIAS_DARK else ALIAS_LIGHT

        applyComponentState(context, targetAlias, aliasToDisable)
    }

    private fun applyComponentState(context: Context, aliasToEnable: String, aliasToDisable: String) {
        try {
            val packageManager = context.packageManager
            val packageName = context.packageName

            val componentToEnable = ComponentName(packageName, aliasToEnable)
            val componentToDisable = ComponentName(packageName, aliasToDisable)

            // Skip operations if states are already cleanly configured
            val currentEnableState = packageManager.getComponentEnabledSetting(componentToEnable)
            if (currentEnableState != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                // 1. Turn on target launcher layout profile
                packageManager.setComponentEnabledSetting(
                    componentToEnable,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
            }

            val currentDisableState = packageManager.getComponentEnabledSetting(componentToDisable)
            if (currentDisableState != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                // 2. Clear out unused alternate launcher layout profile
                packageManager.setComponentEnabledSetting(
                    componentToDisable,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
            Log.d(TAG, "Successfully mapped launcher configuration to: $aliasToEnable")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to dynamically adjust application component bindings", e)
        }
    }
}
