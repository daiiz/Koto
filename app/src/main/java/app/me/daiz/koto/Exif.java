package app.me.daiz.koto;

import android.media.ExifInterface;
import android.util.Log;

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
        String res = "";
        try {
            ExifInterface ex = new ExifInterface(jpegFilePath);
            res = ex.getAttribute("UserComment");
            Log.i(">>> pageURL", res);  // これを取り除くとクラッシュする
            return res;
        } catch (IOException e) {
            Log.e(">> ", "IOException");
        } catch (RuntimeException e) {
            Log.e(">> ", "RuntimeException");
        }
        return "";
    }
}
