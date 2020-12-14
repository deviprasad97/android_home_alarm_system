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

package com.thanksmister.iot.mqtt.alarmpanel.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.modules.CameraCallback
import com.thanksmister.iot.mqtt.alarmpanel.modules.CameraReader
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.CameraSourcePreview
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import android.util.DisplayMetrics



/**
 * Created by Michael Ritchie on 6/28/18.
 */
class DetectionViewModel @Inject
constructor(application: Application, private val configuration: Configuration,
            private val cameraReader: CameraReader) : AndroidViewModel(application) {

    private val cameras = MutableLiveData<ArrayList<String>>()

    fun getCameras(): LiveData<ArrayList<String>> {
        return cameras
    }

    private fun setCameras(cameras: ArrayList<String>) {
        this.cameras.value = cameras
    }

    init {
        Timber.d("init")
        //getCameraList()
    }

    //prevents memory leaks by disposing pending observable objects
    public override fun onCleared() {
        cameraReader.stopCamera()
    }

    fun startCameraPreview(callback: CameraCallback, preview: CameraSourcePreview?) {
        Timber.d("startCameraPreview")
        if (configuration.hasCameraDetections()) {
            cameraReader.startCameraPreview(callback, configuration, preview)
        } else if (configuration.cameraEnabled || configuration.captureCameraImage()) {
            cameraReader.startCameraPreviewSolo(callback, configuration, preview)
        }
    }

    companion object {

    }
}