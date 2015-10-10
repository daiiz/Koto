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

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
        }
    }

    private void bindEvents(final Activity self) {
        // キャプチャボタン
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // キャプチャ画像を保存する
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

                String fileName = "koto-" + Math.random() * 1000 + ".jpg";
                String AttachName = file.getAbsolutePath() + "/" + fileName;

                try {
                    FileOutputStream out = new FileOutputStream(AttachName);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();

                    // 書く
                    ExifInterface ex = new ExifInterface(AttachName);
                    ex.setAttribute("UserComment", getBrowserUrl());
                    ex.saveAttributes();

                    // 読む
                    ex = new ExifInterface(AttachName);
                    Toast.makeText(self, ex.getAttribute("UserComment"), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ContentValues values = new ContentValues();
                ContentResolver contentResolver = getContentResolver();
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.TITLE, fileName);
                values.put("_data", AttachName);
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            }
        });

        // 画像を開くボタン
        openImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // WebページのURLを読み取って、ページを開くどうか問う
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("image/jpeg");
                i.addCategory(Intent.CATEGORY_OPENABLE);
                Intent chooserIntent = Intent.createChooser(i, "Pick Image");
                startActivityForResult(chooserIntent, 1);
            }
        });

        // URLを入力してページを開くボタン
        openUrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // WebページのURLを入力してもらい、ページを開く
                final EditText editView = new EditText(MainActivity.this);
                editView.setText("http://daiz713.github.io/0/");
                AlertDialog.Builder willOpenPage = new AlertDialog.Builder(MainActivity.this);
                willOpenPage.setTitle("URLを入力してください");
                willOpenPage.setView(editView);

                willOpenPage.setPositiveButton(
                        "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // OK クリック処理
                                String url = editView.getText().toString();
                                webView.loadUrl(url);
                            }
                    });
                willOpenPage.setNegativeButton(
                    "キャンセル",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
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
        actionBar.hide();
        
        attachViews();
        setBrowser();
        bindEvents(this);
        webView.loadUrl("http://www.google.co.jp");
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
    public String getPath(Uri uri) {
        ContentResolver cr = getContentResolver();
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = cr.query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        String result = cursor.getString(column_index);
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {

            String path = getPath(data.getData());
            // 読む
            if (path != null) {
                ExifInterface ex = null;
                try {
                    ex = new ExifInterface(path);
                    AlertDialog.Builder willOpenPage = new AlertDialog.Builder(this);
                    willOpenPage.setTitle("下記のウェブサイトにアクセスしますか？");
                    willOpenPage.setMessage(ex.getAttribute("UserComment"));

                    final ExifInterface finalEx = ex;
                    willOpenPage.setPositiveButton(
                            "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // OK クリック処理
                                    String url = finalEx.getAttribute("UserComment");
                                    webView.loadUrl(url);
                                }
                            });
                    willOpenPage.setNegativeButton(
                            "キャンセル",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });

                    willOpenPage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
