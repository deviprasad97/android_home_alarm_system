/*
 * Copyright (c) 2018 ThanksMister LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thanksmister.iot.mqtt.alarmpanel.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.CheckBoxPreference
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import android.text.TextUtils
import android.view.View
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class CaptureSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject lateinit var configuration: Configuration
    @Inject lateinit var dialogUtils: DialogUtils

    private var tolPreference: EditTextPreference? = null
    private var fromPreference: EditTextPreference? = null
    private var domainPreference: EditTextPreference? = null
    private var keyPreference: EditTextPreference? = null
    private var activePreference: CheckBoxPreference? = null
    private var telegramTokenPreference: EditTextPreference? = null
    private var telegramChatIdPreference: EditTextPreference? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_capture)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        telegramChatIdPreference = findPreference(Configuration.PREF_TELEGRAM_CHAT_ID) as EditTextPreference
        telegramTokenPreference = findPreference(Configuration.PREF_TELEGRAM_TOKEN) as EditTextPreference
        tolPreference = findPreference(Configuration.PREF_MAIL_TO) as EditTextPreference
        fromPreference = findPreference(Configuration.PREF_MAIL_FROM) as EditTextPreference
        domainPreference = findPreference(Configuration.PREF_MAIL_URL) as EditTextPreference
        keyPreference = findPreference(Configuration.PREF_MAIL_API_KEY) as EditTextPreference
        activePreference = findPreference(Configuration.PREF_CAMERA_CAPTURE) as CheckBoxPreference

        activePreference!!.isChecked = configuration.hasCameraCapture()

        if (!TextUtils.isEmpty(configuration.getMailTo())) {
            tolPreference!!.setDefaultValue(configuration.getMailTo())
            tolPreference!!.summary = configuration.getMailTo()
        }

        if (!TextUtils.isEmpty(configuration.getMailFrom())) {
            fromPreference!!.setDefaultValue(configuration.getMailFrom())
            fromPreference!!.summary = configuration.getMailFrom()
        }

        if (!TextUtils.isEmpty(configuration.getMailGunUrl())) {
            domainPreference!!.setDefaultValue(configuration.getMailGunUrl())
            domainPreference!!.summary = configuration.getMailGunUrl()
        }

        if (!TextUtils.isEmpty(configuration.getMailGunApiKey())) {
            keyPreference!!.setDefaultValue(configuration.getMailGunApiKey())
            keyPreference!!.summary = configuration.getMailGunApiKey()
        }

        if (!TextUtils.isEmpty(configuration.telegramChatId)) {
            telegramChatIdPreference!!.setDefaultValue(configuration.telegramChatId)
            telegramChatIdPreference!!.summary = configuration.telegramChatId
        }

        if (!TextUtils.isEmpty(configuration.telegramToken)) {
            telegramTokenPreference!!.setDefaultValue(configuration.telegramToken)
            telegramTokenPreference!!.summary = configuration.telegramToken
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {

        when (key) {
            Configuration.PREF_MAIL_TO -> {
                val value = tolPreference!!.text
                configuration.setMailTo(value)
                tolPreference!!.summary = value
            }
            Configuration.PREF_MAIL_FROM -> {
                val value = fromPreference!!.text
                configuration.setMailFrom(value)
                fromPreference!!.summary = value
            }
            Configuration.PREF_MAIL_URL -> {
                val value = domainPreference!!.text
                configuration.setMailGunUrl(value)
                domainPreference!!.summary = value
            }
            Configuration.PREF_MAIL_API_KEY -> {
                val value = keyPreference!!.text
                configuration.setMailGunApiKey(value)
                keyPreference!!.summary = value
            }
            Configuration.PREF_TELEGRAM_CHAT_ID -> {
                val value = telegramChatIdPreference!!.text
                configuration.telegramChatId = value
                telegramChatIdPreference!!.summary = value
            }
            Configuration.PREF_TELEGRAM_TOKEN -> {
                val value = telegramTokenPreference!!.text
                configuration.telegramToken = value
                telegramTokenPreference!!.summary = value
            }
            Configuration.PREF_CAMERA_CAPTURE -> {
                val checked = activePreference!!.isChecked
                configuration.setHasCameraCapture(checked)
            }
        }
    }
}