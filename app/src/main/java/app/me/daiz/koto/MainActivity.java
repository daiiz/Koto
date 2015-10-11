package app.me.daiz.koto;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity {
    private WebView webView;
    private Button captureButton;
    private Button openImgButton;
    private Button openUrlButton;

    int PIC_REQUEST_CODE = 1000;
    String DEFAULT_PAGE_URL = "http://www.google.co.jp";

    private String getBrowserUrl() {
        return webView.getUrl();
    };

    private void attachViews() {
        webView = (WebView) findViewById(R.id.browser);
        captureButton = (Button) findViewById(R.id.capturebutton);
        openImgButton = (Button) findViewById(R.id.openImgButton);
        openUrlButton = (Button) findViewById(R.id.openUrlButton);
    }

    private void setBrowser() {
        webView.setWebViewClient(new MyWebViewClient());
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setDrawingCacheEnabled(true);
    }

    public class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {}
    }

    // WebViewの見えている部分だけをキャプチャして、
    // JPEGのexifコメントにURLを焼き込んで保存する。
    private void captureWebView() {
        webView.destroyDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(webView.getDrawingCache(true));
        final Canvas c = new Canvas();
        c.drawBitmap(bitmap, 0, 0, null);
        webView.draw(c);

        final String SAVE_DIR = "/Koto/";
        File file = new File(Environment.getExternalStorageDirectory().getPath() + SAVE_DIR);
        try {
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        String fileName = Util.getJpegFileName();
        String filePath = file.getAbsolutePath() + "/" + fileName;

        try {
            FileOutputStream out = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            // 書き込む
            Exif.writePageURL(filePath, getBrowserUrl());

            // 読む
            String pageURL = Exif.readPageURL(filePath);
            Toast.makeText(this, pageURL, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ContentValues values = new ContentValues();
        ContentResolver contentResolver = getContentResolver();
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put("_data", filePath);
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private void bindEvents() {
        // キャプチャボタン
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder willCapturePage = new AlertDialog.Builder(MainActivity.this);
                willCapturePage.setTitle("このページをキャプチャしますか？");
                willCapturePage.setMessage("スクリーンショット画像は端末内に保存されます。");

                willCapturePage.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        captureWebView();
                    }
                });
                willCapturePage.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {}
                });
                willCapturePage.show();
            }
        });

        // 画像を開くボタン
        openImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // WebページのURLを記述してある画像を選択してもらう

                // TODO
                // ACTION_GET_CONTENT にすれば、Google Photoからも吸えるようになるけれど
                // URL取得の場合分けが増えるのでまた今度
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/jpeg");
                startActivityForResult(intent, PIC_REQUEST_CODE);
            }
        });

        // URLを入力してページを開くボタン
        openUrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // WebページのURLを入力してもらい、ページを開く
                final EditText editView = new EditText(MainActivity.this);
                final String urlPlaceholder = getBrowserUrl();
                editView.setText(urlPlaceholder);
                editView.selectAll();
                AlertDialog.Builder willOpenPage = new AlertDialog.Builder(MainActivity.this);
                willOpenPage.setTitle("URLを入力してください");
                willOpenPage.setView(editView);

                willOpenPage.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String url = editView.getText().toString();
                        if (url.equals("")) {
                            url = urlPlaceholder;
                        }
                        webView.loadUrl(url);
                    }
                });
                willOpenPage.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {}
                });

                willOpenPage.show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.hide();
        }

        attachViews();
        setBrowser();
        bindEvents();
        webView.loadUrl(DEFAULT_PAGE_URL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // uriからpathを取得するメソッド
    public String getPath(Intent data) {
        Uri uri = data.getData();
        this.grantUriPermission(this.getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        this.grantUriPermission(this.getPackageName(), uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        final int takeFlags = data.getFlags()
                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        getContentResolver().takePersistableUriPermission(uri, takeFlags);

        if (String.valueOf(uri).substring(0, 21).equals("content://com.android")) {
            String[] photo_split = String.valueOf(uri).split("%3A");
            String imageURI = "content://media/external/images/media/"+photo_split[1];

            ContentResolver cr = this.getContentResolver();
            String[] columns = {MediaStore.Images.Media.DATA};
            Cursor c = cr.query(Uri.parse(imageURI), columns, null, null, null);
            c.moveToFirst();

            File fileContents = new File(c.getString(0));
            return fileContents.getAbsolutePath();
        }
        return "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PIC_REQUEST_CODE && resultCode == RESULT_OK) {

            String path = getPath(data);
            // 読む
            if (path != null) {
                ExifInterface ex = null;
                try {
                    ex = new ExifInterface(path);
                    AlertDialog.Builder willOpenPage = new AlertDialog.Builder(this);
                    willOpenPage.setTitle("下記のウェブサイトにアクセスしますか？");
                    willOpenPage.setMessage(ex.getAttribute("UserComment"));

                    final ExifInterface finalEx = ex;
                    willOpenPage.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String url = finalEx.getAttribute("UserComment");
                            webView.loadUrl(url);
                        }
                    });
                    willOpenPage.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {}
                    });

                    willOpenPage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
