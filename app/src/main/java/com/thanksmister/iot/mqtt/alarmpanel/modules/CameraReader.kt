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

package com.thanksmister.iot.mqtt.alarmpanel.modules

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.content.Context
import android.graphics.*
import android.hardware.Camera
import android.os.AsyncTask
import android.view.Surface
import android.view.WindowManager
import com.google.android.gms.vision.*
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor

import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.lang.ref.WeakReference
import javax.inject.Inject
import android.graphics.Bitmap
import android.renderscript.*
import com.google.android.gms.vision.CameraSource.CAMERA_FACING_BACK
import com.google.android.gms.vision.CameraSource.CAMERA_FACING_FRONT
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.CameraSourcePreview

import java.io.IOException

class CameraReader @Inject
constructor(private val context: Context) {

    private var cameraCallback: CameraCallback? = null
    private var faceDetector: FaceDetector? = null
    private var barcodeDetector: BarcodeDetector? = null
    private var motionDetector: MotionDetector? = null
    private var multiDetector: MultiDetector? = null
    private var streamDetector: StreamingDetector? = null
    private var cameraSource: CameraSource? = null
    private var faceDetectorProcessor: LargestFaceFocusingProcessor? = null
    private var barCodeDetectorProcessor: MultiProcessor<Barcode>? = null
    private var motionDetectorProcessor: MultiProcessor<Motion>? = null
    private var streamDetectorProcessor: MultiProcessor<Stream>? = null
    private val byteArray = MutableLiveData<ByteArray>()
    private var bitmapComplete = true;
    private var byteArrayCreateTask: ByteArrayTask? = null
    private var cameraOrientation: Int = 0
    private var cameraPreview: CameraSourcePreview? = null

    fun getJpeg(): LiveData<ByteArray> {
        return byteArray
    }

    private fun setJpeg(value: ByteArray) {
        this.byteArray.value = value
    }

    fun stopCamera() {

        if (byteArrayCreateTask != null) {
            byteArrayCreateTask!!.cancel(true)
            byteArrayCreateTask = null
        }

        if (cameraSource != null) {
            cameraSource!!.release()
            cameraSource = null
        }

        if (faceDetector != null) {
            faceDetector!!.release()
            faceDetector = null
        }

        if (barcodeDetector != null) {
            barcodeDetector!!.release()
            barcodeDetector = null
        }

        if (motionDetector != null) {
            motionDetector!!.release()
            motionDetector = null
        }

        if (streamDetector != null) {
            streamDetector!!.release()
            streamDetector = null
        }

        if (multiDetector != null) {
            multiDetector!!.release()
            multiDetector = null
        }

        if (faceDetectorProcessor != null) {
            faceDetectorProcessor!!.release()
            faceDetectorProcessor = null
        }

        if (barCodeDetectorProcessor != null) {
            barCodeDetectorProcessor!!.release()
            barCodeDetectorProcessor = null
        }

        if (motionDetectorProcessor != null) {
            motionDetectorProcessor!!.release()
            motionDetectorProcessor = null
        }

        if (streamDetectorProcessor != null) {
            streamDetectorProcessor!!.release()
            streamDetectorProcessor = null
        }
    }

    @SuppressLint("MissingPermission")
    fun startCamera(callback: CameraCallback, configuration: Configuration) {
        Timber.d("startCamera")
        Timber.d("FPS: " + configuration.cameraFPS)
        this.cameraCallback = callback
        if (configuration.cameraEnabled) {
            buildDetectors(configuration)
            if(multiDetector != null) {
                try {
                    cameraSource = initCamera(configuration.cameraId, configuration.cameraFPS, configuration.cameraWidth, configuration.cameraHeight)
                    cameraSource!!.start()
                } catch (e : Exception) {
                    Timber.e(e.message)
                    try {
                        if(configuration.cameraId == CAMERA_FACING_FRONT) {
                            cameraSource = initCamera(CAMERA_FACING_BACK, configuration.cameraFPS, configuration.cameraWidth, configuration.cameraHeight)
                            cameraSource!!.start()
                        } else {
                            cameraSource = initCamera(CAMERA_FACING_FRONT, configuration.cameraFPS, configuration.cameraWidth, configuration.cameraHeight)
                            cameraSource!!.start()
                        }
                    } catch (e : Exception) {
                        Timber.e(e.message)
                        cameraSource!!.stop()
                        cameraCallback?.onCameraError()
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Throws(IOException::class)
    fun startCameraPreview(callback: CameraCallback, configuration: Configuration, preview: CameraSourcePreview?) {
        Timber.d("startCameraPreview")
        Timber.d("FPS: " + configuration.cameraFPS)
        if (configuration.cameraEnabled && preview != null) {
            this.cameraCallback = callback
            this.cameraPreview = preview
            buildDetectors(configuration)
            if(multiDetector != null) {
                cameraSource = initCamera(configuration.cameraId, configuration.cameraFPS)
                cameraPreview!!.start(cameraSource, object : CameraSourcePreview.OnCameraPreviewListener {
                    override fun onCameraError() {
                        Timber.e("Camera Preview Error")
                        cameraSource = if(configuration.cameraId == CAMERA_FACING_FRONT) {
                            initCamera(CAMERA_FACING_BACK, configuration.cameraFPS)
                        } else {
                            initCamera(CAMERA_FACING_FRONT, configuration.cameraFPS)
                        }
                        if(cameraPreview != null) {
                            try {
                                cameraPreview!!.start(cameraSource, object : CameraSourcePreview.OnCameraPreviewListener {
                                    override fun onCameraError() {
                                        Timber.e("Camera Preview Error")
                                        cameraCallback!!.onCameraError()
                                    }
                                })
                            } catch (e: Exception) {
                                Timber.e(e.message)
                                cameraPreview!!.stop()
                                cameraSource!!.stop()
                                cameraCallback!!.onCameraError()
                            }
                        }
                    }
                })
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Throws(IOException::class)
    fun startCameraPreviewSolo(callback: CameraCallback, configuration: Configuration, preview: CameraSourcePreview?) {
        Timber.d("startCameraPreviewSolo")
        Timber.d("FPS: " + configuration.cameraFPS)
        if (configuration.cameraEnabled && preview != null) {
            this.cameraCallback = callback
            this.cameraPreview = preview
            buildCameraDetector(configuration)
            if(multiDetector != null) {
                cameraSource = initCamera(configuration.cameraId, configuration.cameraFPS)
                cameraPreview!!.start(cameraSource, object : CameraSourcePreview.OnCameraPreviewListener {
                    override fun onCameraError() {
                        Timber.e("Camera Preview Error")
                        cameraSource = if(configuration.cameraId == CAMERA_FACING_FRONT) {
                            initCamera(CAMERA_FACING_BACK, configuration.cameraFPS)
                        } else {
                            initCamera(CAMERA_FACING_FRONT, configuration.cameraFPS)
                        }
                        if(cameraPreview != null) {
                            try {
                                cameraPreview!!.start(cameraSource, object : CameraSourcePreview.OnCameraPreviewListener {
                                    override fun onCameraError() {
                                        Timber.e("Camera Preview Error")
                                        cameraCallback!!.onCameraError()
                                    }
                                })
                            } catch (e: Exception) {
                                Timber.e(e.message)
                                cameraPreview!!.stop()
                                cameraSource!!.stop()
                                cameraCallback!!.onCameraError()
                            }
                        }
                    }
                })
            }
        }
    }

    private fun buildCameraDetector(configuration: Configuration) {
        val info = Camera.CameraInfo()
        try{
            Camera.getCameraInfo(configuration.cameraId, info)
        } catch (e: RuntimeException) {
            Timber.e(e.message)
            cameraCallback!!.onCameraError()
            return
        }
        cameraOrientation = info.orientation
        val multiDetectorBuilder = MultiDetector.Builder()
        var detectorAdded = false
        if(configuration.cameraEnabled) {
            streamDetector = StreamingDetector.Builder().build()
            streamDetectorProcessor = MultiProcessor.Builder<Stream>(MultiProcessor.Factory<Stream> {
                object : Tracker<Stream>() {
                    override fun onUpdate(p0: Detector.Detections<Stream>?, stream: Stream?) {
                        super.onUpdate(p0, stream)
                    }
                }
            }).build()

            streamDetector!!.setProcessor(streamDetectorProcessor)
            multiDetectorBuilder.add(streamDetector)
            detectorAdded = true
        }

        if(detectorAdded) {
            multiDetector = multiDetectorBuilder.build()
        }
    }

    private fun buildDetectors(configuration: Configuration) {

        val info = Camera.CameraInfo()
        try{
            Camera.getCameraInfo(configuration.cameraId, info)
        } catch (e: RuntimeException) {
            Timber.e(e.message)
            cameraCallback!!.onCameraError()
            return
        }
        cameraOrientation = info.orientation
        val multiDetectorBuilder = MultiDetector.Builder()
        var detectorAdded = false

        if(configuration.cameraEnabled && (configuration.httpMJPEGEnabled || configuration.captureCameraImage())) {
            val renderScript = RenderScript.create(context)
            streamDetector = StreamingDetector.Builder().build()
            streamDetectorProcessor = MultiProcessor.Builder<Stream>(MultiProcessor.Factory<Stream> {
                object : Tracker<Stream>() {
                    override fun onUpdate(p0: Detector.Detections<Stream>?, stream: Stream?) {
                        super.onUpdate(p0, stream)
                        if (stream?.byteArray != null && bitmapComplete) {
                            byteArrayCreateTask = ByteArrayTask(context, renderScript, object : OnCompleteListener {
                                override fun onComplete(byteArray: ByteArray?) {
                                    bitmapComplete = true
                                    setJpeg(byteArray!!)
                                }
                            })
                            bitmapComplete = false
                            byteArrayCreateTask!!.execute(stream.byteArray, stream.width, stream.height, cameraOrientation, configuration.cameraRotate)
                        }
                    }
                }
            }).build()

            streamDetector!!.setProcessor(streamDetectorProcessor)
            multiDetectorBuilder.add(streamDetector)
            detectorAdded = true
        }

        if(configuration.cameraEnabled && configuration.cameraMotionEnabled) {
            motionDetector = MotionDetector.Builder(configuration.cameraMotionMinLuma, configuration.cameraMotionLeniency).build()
            motionDetectorProcessor = MultiProcessor.Builder<Motion>(MultiProcessor.Factory<Motion> {
                object : Tracker<Motion>() {
                    override fun onUpdate(p0: Detector.Detections<Motion>?, motion: Motion?) {
                        super.onUpdate(p0, motion)
                        if (cameraCallback != null && configuration.cameraMotionEnabled) {
                            if (Motion.MOTION_TOO_DARK == motion?.type) {
                                cameraCallback!!.onTooDark()
                            } else if (Motion.MOTION_DETECTED == motion?.type) {
                                Timber.d("motionDetected")
                                cameraCallback!!.onMotionDetected()
                            }
                        }
                    }
                }
            }).build()

            motionDetector!!.setProcessor(motionDetectorProcessor)
            multiDetectorBuilder.add(motionDetector)
            detectorAdded = true
        }

        if(configuration.cameraEnabled && configuration.cameraFaceEnabled) {
            faceDetector = FaceDetector.Builder(context)
                    .setProminentFaceOnly(true)
                    .setTrackingEnabled(false)
                    .setMode(FaceDetector.FAST_MODE)
                    .setClassificationType(FaceDetector.NO_CLASSIFICATIONS)
                    .setLandmarkType(FaceDetector.NO_LANDMARKS)
                    .build()

            faceDetectorProcessor = LargestFaceFocusingProcessor(faceDetector, object : Tracker<Face>() {
                override fun onUpdate(detections: Detector.Detections<Face>, face: Face) {
                    super.onUpdate(detections, face)
                    if (detections.detectedItems.size() > 0) {
                        if (cameraCallback != null && configuration.cameraFaceEnabled) {
                            Timber.d("faceDetected")
                            cameraCallback!!.onFaceDetected()
                        }
                    }
                }
            })

            faceDetector!!.setProcessor(faceDetectorProcessor)
            multiDetectorBuilder.add(faceDetector)
            detectorAdded = true
        }

        if(configuration.cameraEnabled && configuration.cameraQRCodeEnabled) {
            barcodeDetector = BarcodeDetector.Builder(context)
                    .setBarcodeFormats(Barcode.QR_CODE)
                    .build()
            barCodeDetectorProcessor = MultiProcessor.Builder<Barcode>(MultiProcessor.Factory<Barcode> {
                object : Tracker<Barcode>() {
                    override fun onUpdate(p0: Detector.Detections<Barcode>?, p1: Barcode?) {
                        super.onUpdate(p0, p1)
                        if (cameraCallback != null && configuration.cameraQRCodeEnabled) {
                            p1?.let {
                                Timber.d("Barcode: " + p1.displayValue)
                                cameraCallback?.onQRCode(p1.displayValue)
                            }
                        }
                    }
                }
            }).build()

            barcodeDetector!!.setProcessor(barCodeDetectorProcessor);
            multiDetectorBuilder.add(barcodeDetector)
            detectorAdded = true
        }

        if(detectorAdded) {
            multiDetector = multiDetectorBuilder.build()
            if(!multiDetector!!.isOperational) {
                cameraCallback!!.onDetectorError()
                return
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Throws(Exception::class)
    private fun initCamera(cameraId: Int, fsp: Float, widthPixels: Int = 640, heightPixels: Int = 480): CameraSource {
        Timber.d("initCamera cameraId $cameraId")
        Timber.d("initCamera widthPixels $widthPixels")
        Timber.d("initCamera heightPixels $heightPixels")
        return CameraSource.Builder(context, multiDetector)
                .setRequestedFps(fsp)
                .setRequestedPreviewSize(widthPixels, heightPixels)
                .setFacing(cameraId)
                .build()
    }

    interface OnCompleteListener {
        fun onComplete(byteArray: ByteArray?)
    }

    class ByteArrayTask(context: Context, private val renderScript: RenderScript?, private val onCompleteListener: OnCompleteListener) : AsyncTask<Any, Void, ByteArray>() {

        private val contextRef: WeakReference<Context> = WeakReference(context)

        override fun doInBackground(vararg params: kotlin.Any): ByteArray? {
            if (isCancelled) {
                return null
            }
            val byteArray = params[0] as ByteArray
            val width = params[1] as Int
            val height = params[2] as Int
            val orientation = params[3] as Int
            val rotation = params[4] as Float

            val windowService = contextRef.get()!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val currentRotation = windowService.defaultDisplay.rotation
            val nv21Bitmap = nv21ToBitmap(renderScript, byteArray, width, height)
            var rotate = orientation

            when (currentRotation) {
                Surface.ROTATION_90 -> {
                    rotate -= 90
                }
                Surface.ROTATION_180 -> {
                    rotate -= 180
                }
                Surface.ROTATION_270 -> {
                    rotate -= 270
                }
            }

            rotate %= 360
            rotate += rotation.toInt()

            val matrix = Matrix()
            matrix.postRotate(rotate.toFloat())
            return try {
                val bitmap = Bitmap.createBitmap(nv21Bitmap, 0, 0, width, height, matrix, true)
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                val byteArrayOut = stream.toByteArray()
                bitmap.recycle()
                byteArrayOut
            } catch (e: OutOfMemoryError) {
                Timber.e(e.message)
                null
            }
        }

        override fun onPostExecute(result: ByteArray?) {
            super.onPostExecute(result)
            if (isCancelled) {
                return
            }
            onCompleteListener.onComplete(result)
        }


        private fun nv21ToBitmap(rs: RenderScript?, yuvByteArray: ByteArray, width: Int, height: Int): Bitmap {

            val yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs))

            val yuvType = Type.Builder(rs, Element.U8(rs)).setX(yuvByteArray.size)
            val allocationIn = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT)

            val rgbaType = Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height)
            val allocationOut = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT)

            allocationIn.copyFrom(yuvByteArray)

            yuvToRgbIntrinsic.setInput(allocationIn)
            yuvToRgbIntrinsic.forEach(allocationOut)

            val bmpout = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            allocationOut.copyTo(bmpout)

            return bmpout
        }
    }

    companion object {

    }
}