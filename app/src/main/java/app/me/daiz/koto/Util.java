package app.me.daiz.koto;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
    public static String getJpegFileName() {
        String today = new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date());
        int random = (int) Math.floor(Math.random() * 10000);
        String extension = ".koto.jpg";

        // e.g. i2015-09-11-10-08-3312.koto.jpg
        String fileName = "i" + today + "-" + random + extension;

        return fileName;
    }

    public static void refreshPhotoGallery(ContentResolver contentResolver, String JpegFilePath, String JpegFileName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.TITLE, JpegFileName);
        values.put("_data", JpegFilePath);
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    // content://com.android.providers.media.documents/document/image:53362 形式のパスを
    // content://media/external/images/media/53362 形式に変換して
    // 端末内の絶対パス /storage/emulated/0/Koto/xxxxxxx.jpg を返す
    public static String getJpegFilePath(ContentResolver contentResolver, Intent data) {
        Uri uri = data.getData();

        final int takeFlags = data.getFlags()
                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        contentResolver.takePersistableUriPermission(uri, takeFlags);

        if (String.valueOf(uri).substring(0, 21).equals("content://com.android")) {
            String[] photo_split = String.valueOf(uri).split("%3A");
            String imageURI = "content://media/external/images/media/" + photo_split[1];

            ContentResolver cr = contentResolver;
            String[] columns = {MediaStore.Images.Media.DATA};
            Cursor c = cr.query(Uri.parse(imageURI), columns, null, null, null);
            c.moveToFirst();

            File fileContents = new File(c.getString(0));
            return fileContents.getAbsolutePath();
        }
        return "";
    }

}
