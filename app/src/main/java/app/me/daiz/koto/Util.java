package app.me.daiz.koto;

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
}
