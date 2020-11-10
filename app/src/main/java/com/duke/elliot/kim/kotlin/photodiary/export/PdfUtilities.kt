package com.duke.elliot.kim.kotlin.photodiary.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.Toast
import com.duke.elliot.kim.kotlin.photodiary.utility.getOutputDirectory
import com.duke.elliot.kim.kotlin.photodiary.utility.showToast
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.pdf.PdfDocument
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
            PdfWriter.getInstance(document, FileOutputStream(getOutputDirectory(context).toString() + "/newPDF.pdf"))
            document.open()

            val image = Image.getInstance(file.toString())
            val scaler: Float = (document.pageSize.width - document.leftMargin()
                    - document.rightMargin() - 0) / image.width * 100
            image.scalePercent(scaler)
            image.alignment = Image.ALIGN_CENTER or Image.ALIGN_TOP
            document.add(image)
            Toast.makeText(context, context.getExternalFilesDir("null").toString() + "/newPDF.pdf", Toast.LENGTH_SHORT).show()
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*
    fun createPDFWithMultipleImage(){
        File file = getOutputFile()
        if (file != null){
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                PdfDocument pdfDocument = new PdfDocument();

                for (int i = 0; i < images.size(); i++){
                    Bitmap bitmap = BitmapFactory.decodeFile(images.get(i).getPath());
                    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), (i + 1)).create();
                    PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                    Canvas canvas = page.getCanvas();
                    Paint paint = new Paint();
                    paint.setColor(Color.BLUE);
                    canvas.drawPaint(paint);
                    canvas.drawBitmap(bitmap, 0f, 0f, null);
                    pdfDocument.finishPage(page);
                    bitmap.recycle();
                }
                pdfDocument.writeTo(fileOutputStream);
                pdfDocument.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

     */
}
/*
private void createPDFWithMultipleImage(){
    File file = getOutputFile();
    if (file != null){
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            PdfDocument pdfDocument = new PdfDocument();

            for (int i = 0; i < images.size(); i++){
                Bitmap bitmap = BitmapFactory.decodeFile(images.get(i).getPath());
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), (i + 1)).create();
                PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                Canvas canvas = page.getCanvas();
                Paint paint = new Paint();
                paint.setColor(Color.BLUE);
                canvas.drawPaint(paint);
                canvas.drawBitmap(bitmap, 0f, 0f, null);
                pdfDocument.finishPage(page);
                bitmap.recycle();
            }
            pdfDocument.writeTo(fileOutputStream);
            pdfDocument.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

private File getOutputFile(){
    File root = new File(this.getExternalFilesDir(null),"My PDF Folder");

    boolean isFolderCreated = true;

    if (!root.exists()){
        isFolderCreated = root.mkdir();
    }

    if (isFolderCreated) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "PDF_" + timeStamp;

        return new File(root, imageFileName + ".pdf");
    }
    else {
        Toast.makeText(this, "Folder is not created", Toast.LENGTH_SHORT).show();
        return null;
    }
}
 */