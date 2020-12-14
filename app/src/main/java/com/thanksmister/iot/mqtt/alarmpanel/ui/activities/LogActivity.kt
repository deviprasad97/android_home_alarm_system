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

package com.thanksmister.iot.mqtt.alarmpanel.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.widget.Toast
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity

import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.ui.fragments.LogFragment
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class LogActivity : BaseActivity() {

    private val inactivityHandler: Handler = Handler()
    private val inactivityCallback = Runnable {
        Toast.makeText(this@LogActivity, getString(R.string.toast_screen_timeout), Toast.LENGTH_LONG).show()
        dialogUtils.clearDialogs()
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (supportActionBar != null) {
            supportActionBar!!.show()
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.setTitle(R.string.activity_logs_title)
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.contentFrame, LogFragment.newInstance(), LOGS_FRAGMENT).commit()
        }

        setContentView(R.layout.activity_logs)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        inactivityHandler.postDelayed(inactivityCallback, 60000)
    }

    override fun onDestroy() {
        super.onDestroy()
        inactivityHandler.removeCallbacks(inactivityCallback)
    }

    override fun onUserInteraction() {
        inactivityHandler.removeCallbacks(inactivityCallback)
        inactivityHandler.postDelayed(inactivityCallback, 60000)
    }

    companion object {
        const val LOGS_FRAGMENT = "com.thanksmister.fragment.LOGS_FRAGMENT"
        fun createStartIntent(context: Context): Intent {
            return Intent(context, LogActivity::class.java)
        }
    }
}