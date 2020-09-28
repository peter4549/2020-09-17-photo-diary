package com.duke.elliot.kim.kotlin.photodiary.diary.media

import com.cleveroad.audiovisualization.DbmHandler
import com.duke.elliot.kim.kotlin.photodiary.diary.media.audio.Complex
import com.duke.elliot.kim.kotlin.photodiary.diary.media.audio.FFT


class AudioRecordingDbmHandler : DbmHandler<ByteArray>(), AudioRecorder.RecordingCallback {
    private var dbs: DoubleArray? = null
    private var allAmps: DoubleArray? = null
    override fun onDataReceivedImpl(
        bytes: ByteArray,
        layersCount: Int,
        dBmArray: FloatArray,
        ampsArray: FloatArray
    ) {
        val bytesPerSample = 2 // As it is 16bit PCM
        val amplification = 320.0 // choose a number as you like
        var fft = Array(bytes.size / bytesPerSample) { Complex(0.0, 0.0) }
        var index = 0
        var floatIndex = 0
        while (index < bytes.size - bytesPerSample + 1) {
            var sample = 0.0
            for (b in 0 until bytesPerSample) {
                var v = bytes[index + b].toInt()
                if (b < bytesPerSample - 1) {
                    v = v and 0xFF
                }
                sample += v shl (b * 8).toDouble().toInt()
            }
            val sample32 = amplification * (sample / 32768.0)
            fft[floatIndex] = Complex(sample32, 0.0)
            index += bytesPerSample
            floatIndex++
        }
        fft = FFT.fft(fft)
        // calculate dBs and amplitudes
        val dataSize = fft.size / 2 - 1
        if (dbs == null || dbs!!.size != dataSize) {
            dbs = DoubleArray(dataSize)
        }
        if (allAmps == null || allAmps!!.size != dataSize) {
            allAmps = DoubleArray(dataSize)
        }
        for (i in 0 until dataSize) {
            dbs!![i] = fft[i].abs()
            var k = 1f
            if (i == 0 || i == dataSize - 1) {
                k = 2f
            }
            val re = fft[2 * i].re()
            val im = fft[2 * i + 1].im()
            val sqMag = re * re + im * im
            allAmps!![i] = (k * Math.sqrt(sqMag.toDouble()) / dataSize)
        }
        val size = dbs!!.size / layersCount
        for (i in 0 until layersCount) {
            val index = ((i + 0.5f) * size).toInt()
            val db = dbs!![index]
            val amp = allAmps!![index]
            dBmArray[i] = if (db > MAX_DB_VALUE) 1F else (db / MAX_DB_VALUE).toFloat()
            ampsArray[i] = amp.toFloat()
        }
    }

    fun stop() {
        calmDownAndStopRendering()
    }

    override fun onDataReady(data: ByteArray?) {
        onDataReceived(data)
    }

    companion object {
        private const val MAX_DB_VALUE = 170F
    }
}