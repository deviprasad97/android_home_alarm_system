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

package com.thanksmister.iot.mqtt.alarmpanel.di


import androidx.lifecycle.ViewModel
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.network.AlarmPanelService
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.LiveCameraActivity
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.LogActivity
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.MainActivity
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.SettingsActivity
import com.thanksmister.iot.mqtt.alarmpanel.ui.fragments.*
import com.thanksmister.iot.mqtt.alarmpanel.viewmodel.*
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
internal abstract class AndroidBindingModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindsMessageViewModel(mainViewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(WeatherViewModel::class)
    abstract fun bindsWeatherViewModel(mainViewModel: WeatherViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SensorViewModel::class)
    abstract fun bindsSensorViewModel(mainViewModel: SensorViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DetectionViewModel::class)
    abstract fun bindsDetectionViewModel(mainViewModel: DetectionViewModel): ViewModel

    @ContributesAndroidInjector
    internal abstract fun alarmService(): AlarmPanelService

    @ContributesAndroidInjector
    internal abstract fun baseActivity(): BaseActivity

    @ContributesAndroidInjector
    internal abstract fun mainActivity(): MainActivity

    @ContributesAndroidInjector
    internal abstract fun logActivity(): LogActivity

    @ContributesAndroidInjector
    internal abstract fun settingsActivity(): SettingsActivity

    @ContributesAndroidInjector
    internal abstract fun cameraActivity(): LiveCameraActivity

    @ContributesAndroidInjector
    internal abstract fun baseFragment(): BaseFragment

    @ContributesAndroidInjector
    internal abstract fun mainFragment(): MainFragment

    @ContributesAndroidInjector
    internal abstract fun platformFragment(): PlatformFragment

    @ContributesAndroidInjector
    internal abstract fun informationFragment(): InformationFragment

    @ContributesAndroidInjector
    internal abstract fun controlsFragment(): ControlsFragment

    @ContributesAndroidInjector
    internal abstract fun aboutFragment(): AboutFragment

    @ContributesAndroidInjector
    internal abstract fun logFragment(): LogFragment

    @ContributesAndroidInjector
    internal abstract fun settingsFragment(): SettingsFragment

    @ContributesAndroidInjector
    internal abstract fun alarmSettingsFragment(): AlarmSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun notificationsSettingsFragment(): NotificationsSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun platformSettingsFragment(): PlatformSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun screenSettingsFragment(): ScreenSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun weatherSettingsFragment(): WeatherSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun cameraSettingsFragment(): CameraSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun mqttSettingsFragment(): MqttSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun deviceSensorsFragment(): DeviceSensorsSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun cameraCaptureSettingsFragment(): CaptureSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun faceSettingsFragment(): FaceSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun qrSettingsFragment(): QrSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun mjpegSettingsFragment(): MjpegSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun motionSettingsFragment(): MotionSettingsFragment
}