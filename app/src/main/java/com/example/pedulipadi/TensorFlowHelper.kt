package com.example.pedulipadi

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import com.example.pedulipadi.ml.Model

object TensorFlowHelper {

    const val imageSize = 180

    @Composable
    fun ClassifyImage(image: Bitmap, callback: (@Composable (leaf: String, confidence: Float) -> Unit)) {
        val model: Model = Model.newInstance(LocalContext.current)

        // Creates inputs for reference.
        val inputFeature0 =
            TensorBuffer.createFixedSize(intArrayOf(1, 180, 180, 3), DataType.FLOAT32)
        val byteBuffer: ByteBuffer = ByteBuffer.allocateDirect(1 * imageSize * imageSize * 3 * 4)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(imageSize * imageSize)
        image.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)
        var pixel = 0

        //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
        for (i in 0 until imageSize) {
            for (j in 0 until imageSize) {
                val `val` = intValues[pixel++]
                byteBuffer.putFloat((`val` shr 16 and 0xFF) / 255.0f)
                byteBuffer.putFloat((`val` shr 8 and 0xFF) / 255.0f)
                byteBuffer.putFloat((`val` and 0xFF) / 255.0f)

                /*byteBuffer.putFloat(((`val` shr 16 and 0xFF) - 127.5f) / 127.5f)
                byteBuffer.putFloat(((`val` shr 8 and 0xFF) - 127.5f) / 127.5f)
                byteBuffer.putFloat(((`val` and 0xFF) - 127.5f) / 127.5f)*/
            }
        }
        inputFeature0.loadBuffer(byteBuffer)

        // Runs model inference and gets result.
        val outputs: Model.Outputs = model.process(inputFeature0)
        val outputFeature0: TensorBuffer = outputs.outputFeature0AsTensorBuffer
        val confidences = outputFeature0.floatArray

        // find the index of the class with the biggest confidence.
        var maxPos = 0
        var maxConfidence = 0f
        for (i in confidences.indices) {
            if (confidences[i] > maxConfidence) {
                maxConfidence = confidences[i]
                maxPos = i
            }
        }

        val classes = arrayOf("Bacterial leaf blight","Brown spot","Leaf smut")
        callback.invoke(classes[maxPos], maxConfidence)

        model.close()

    }

}