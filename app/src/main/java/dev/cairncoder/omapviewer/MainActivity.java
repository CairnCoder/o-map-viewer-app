package dev.cairncoder.omapviewer;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.webkit.WebChromeClient;
import android.webkit.ValueCallback;
import android.net.Uri;

import android.content.ActivityNotFoundException;
import android.content.Intent;

import android.webkit.WebSettings;

import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;





public class MainActivity extends AppCompatActivity {

    WebView urWeb;

    private ValueCallback<Uri[]> filePathCallback;

    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        urWeb = findViewById(R.id.urWeb);
        WebSettings s = urWeb.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMediaPlaybackRequiresUserGesture(true);
        urWeb.setWebViewClient(new WebViewClient());


        urWeb.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(
                    WebView webView,
                    ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams) {

                if (MainActivity.this.filePathCallback != null) {
                    MainActivity.this.filePathCallback.onReceiveValue(null);
                }
                MainActivity.this.filePathCallback = filePathCallback;

                try {
                    startActivityForResult(fileChooserParams.createIntent(), 1001);
                } catch (ActivityNotFoundException e) {
                    MainActivity.this.filePathCallback = null;
                    return false;
                }
                return true;
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                customView = view;
                customViewCallback = callback;

                // Put the fullscreen view over your activity
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                );

                addContentView(customView, new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT
                ));
                urWeb.setVisibility(View.GONE);
            }

            @Override
            public void onHideCustomView() {
                if (customView == null) return;

                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

                ((ViewGroup) customView.getParent()).removeView(customView);
                customView = null;

                if (customViewCallback != null) {
                    customViewCallback.onCustomViewHidden();
                    customViewCallback = null;
                }

                urWeb.setVisibility(View.VISIBLE);
            }
        });

        //urWeb.loadUrl("https://cairncoder.github.io/o-map-viewer/");
        urWeb.loadUrl("file:///android_asset/o-map-viewer/src/index.html");


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != 1001) return;

        if (filePathCallback == null) return;

        Uri[] results = null;

        if (resultCode == RESULT_OK) {
            if (data != null) {
                // Multiple selection
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    results = new Uri[count];
                    for (int i = 0; i < count; i++) {
                        results[i] = data.getClipData().getItemAt(i).getUri();
                    }
                } else if (data.getData() != null) {
                    // Single selection
                    results = new Uri[]{ data.getData() };
                }
            }
        }

        filePathCallback.onReceiveValue(results);
        filePathCallback = null;
    }


}