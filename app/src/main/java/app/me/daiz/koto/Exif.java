package app.me.daiz.koto;

import android.media.ExifInterface;
import java.io.IOException;

public class Exif {
    // ExifのUserCommentスペースに、ウェブページのURLを書き込む
    public static boolean writePageURL(String jpegFilePath, String pageURL) {
        ExifInterface ex = null;
        try {
            ex = new ExifInterface(jpegFilePath);
            ex.setAttribute("UserComment", pageURL);
            ex.saveAttributes();
            return true;
        } catch (IOException e) {
            e.printStackTrace();

        }
        return false;
    }

    // ExifのUserCommentの内容を読み取る
    public static String readPageURL(String jpegFilePath) {
        try {
            ExifInterface ex = new ExifInterface(jpegFilePath);
            return ex.getAttribute("UserComment");
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }
        return "";
    }
}
