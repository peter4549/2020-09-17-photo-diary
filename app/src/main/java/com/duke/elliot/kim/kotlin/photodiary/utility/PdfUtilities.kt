package com.duke.elliot.kim.kotlin.photodiary.utility

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.Toast
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object PdfUtilities {
    private fun getBitmapFromView(view: View): Bitmap? {
        val bitmap =
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val background: Drawable? = view.background

        if (background != null)
            background.draw(canvas)
        else
            canvas.drawColor(Color.WHITE)

        view.draw(canvas)

        return bitmap
    }

    fun viewToPdf(view: View, context: Context) {
        val bitmap = getBitmapFromView(view)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream) ?: run {
            showToast(context, "PDF 변환에 실패했습니다.")
            return
        }

        val file = File(context.externalCacheDir.toString() + File.separator.toString() + "pdf_temporary_image.jpg")

        try {
            file.createNewFile()

            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(byteArrayOutputStream.toByteArray())

            val document = Document()
            PdfWriter.getInstance(document, FileOutputStream(context.getExternalFilesDir("null").toString() + "/newPDF.pdf"))
            document.open()

            val image = Image.getInstance(file.toString())
            val scaler: Float = (document.pageSize.width - document.leftMargin()
                    - document.rightMargin() - 0) / image.width * 100
            image.scalePercent(scaler)
            image.alignment = Image.ALIGN_CENTER or Image.ALIGN_TOP
            document.add(image)
            document.close()
            Toast.makeText(context, context.getExternalFilesDir("null").toString() + "/newPDF.pdf", Toast.LENGTH_SHORT).show()
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}