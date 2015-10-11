package app.me.daiz.koto;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
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

            ContentResolver contentResolver = getContentResolver();
            Util.refreshPhotoGallery(contentResolver, filePath, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    // jpgファイルに対して、contentURIからpathを取得する
    public String getPath(Intent data) {
        ContentResolver cr = getContentResolver();
        return Util.getJpegFilePath(cr, data);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PIC_REQUEST_CODE && resultCode == RESULT_OK) {
            String path = getPath(data);
            // 読む
            if (path != null) {
                final String pageURL = Exif.readPageURL(path);
                AlertDialog.Builder willOpenPage = new AlertDialog.Builder(this);
                willOpenPage.setTitle("下記のウェブサイトにアクセスしますか？");
                willOpenPage.setMessage(pageURL);

                willOpenPage.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        webView.loadUrl(pageURL);
                    }
                });
                willOpenPage.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                willOpenPage.show();
            }
        }
    }
}
