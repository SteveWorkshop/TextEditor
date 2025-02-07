package io.github.materialapps.texteditor.logic.converter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.itextpdf.html2pdf.HtmlConverter;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.IOException;
import java.io.OutputStream;

import io.github.materialapps.texteditor.BaseApplication;
import lombok.Getter;
import lombok.Setter;

public class PDFConverter {
    private static final String TAG = "PDFConverter";

    @Getter
    @Setter
    private Callback callback;

    public void convert(String text,String fileName){
        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,fileName);
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        ContentResolver contentResolver = BaseApplication.getApplication().getContentResolver();
        Uri uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
        if(uri!=null) {
            try {
                OutputStream outputStream = contentResolver.openOutputStream(uri);
                Parser parser = Parser.builder().build();
                Node document= parser.parse(text);
                HtmlRenderer renderer = HtmlRenderer.builder().build();
                String html = renderer.render(document);
                Log.d(TAG, "convert: "+html);
                HtmlConverter.convertToPdf(html, outputStream);
                outputStream.flush();
                outputStream.close();
                callback.onSuccess();
            } catch (IOException e) {
                Log.e(TAG, "convert: ", e);
                callback.onFailure();
            }
        }
        else{
            callback.onFailure();
        }
    }
    public interface Callback{
        void onSuccess();
        void onFailure();
    }
}
