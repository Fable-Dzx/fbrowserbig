package com.fabledzx.fbrowserbig;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

    private WebView web;
    private EditText etUrl;
    private TextView btnBack, btnForward;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);

        // 绑定控件
        web = findViewById(R.id.webview);
        etUrl = findViewById(R.id.et_url);
        TextView btnGo = findViewById(R.id.btn_go);
        btnBack = findViewById(R.id.btn_back);
        btnForward = findViewById(R.id.btn_forward);
        TextView btnRefresh = findViewById(R.id.btn_refresh);
        TextView btnHome = findViewById(R.id.btn_home);

        // 隐藏默认的标题栏（因为我们在 XML 里自定义了地址栏）
        if (getActionBar() != null) {
            getActionBar().hide();
        }

        initWebView();
        initUI(btnGo, btnRefresh, btnHome);

        loadHomePage();
    }

    private void initWebView() {
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, String url) {
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    return false;
                }
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception ignored) {}
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                updateNavButtons();
            }

            @Override
            public void onPageFinished(WebView v, String url) {
                if (!etUrl.hasFocus() && url != null && !url.startsWith("about:")) {
                    etUrl.setText(url);
                }
                updateNavButtons();
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView v, String title) {
                // 如果你们想要把标题显示在任务栏，可以保留这行
                // setTitle(title == null || title.length() == 0 ? "FBrowser" : title);
            }
        });
    }

    private void initUI(TextView btnGo, TextView btnRefresh, TextView btnHome) {
        etUrl.setOnEditorActionListener((v, action, e) -> {
            go();
            return true;
        });
        etUrl.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) etUrl.selectAll();
        });
        btnGo.setOnClickListener(v -> go());
        
        btnBack.setOnClickListener(v -> {
            if (web.canGoBack()) web.goBack();
        });
        btnForward.setOnClickListener(v -> {
            if (web.canGoForward()) web.goForward();
        });
        btnRefresh.setOnClickListener(v -> web.reload());
        btnHome.setOnClickListener(v -> loadHomePage());
    }

    private void updateNavButtons() {
        // 0xFF 表示不透明，0xFF333333 是深灰，0xFFCCCCCC 是浅灰
        btnBack.setTextColor(web.canGoBack() ? 0xFF333333 : 0xFFCCCCCC);
        btnForward.setTextColor(web.canGoForward() ? 0xFF333333 : 0xFFCCCCCC);
    }

    private void loadHomePage() {
        String homeHtml = "<!doctype html><meta charset=utf-8><meta name=viewport content='width=device-width,initial-scale=1'>"
                + "<body style='margin:0;height:100vh;display:flex;align-items:center;justify-content:center;font:14px sans-serif;color:#888;background:#fff;text-align:center'>"
                + "<div><b style='font-size:22px;color:#111'>F</b><br>输入网址开始浏览</div></body>";
        web.loadDataWithBaseURL(null, homeHtml, "text/html", "utf-8", null);
        etUrl.setText("");
    }

    private void go() {
        String q = etUrl.getText().toString().trim();
        if (q.isEmpty()) return;

        String url;
        if (q.contains("://")) {
            url = q;
        } else {
            boolean looksLikeUrl = q.contains(".") || q.startsWith("localhost")
                    || q.matches("[0-9]+(\\.[0-9]+){3}(:[0-9]+)?");
            if (looksLikeUrl) {
                url = (q.contains(":") && !q.contains(".") ? "http://" : "https://") + q;
            } else {
                url = "https://www.bing.com/search?q=" + Uri.encode(q);
            }
        }
        web.loadUrl(url);
        
        etUrl.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etUrl.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && web != null && web.canGoBack()) {
            web.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (web != null) {
            web.stopLoading();
            web.setWebChromeClient(null);
            web.setWebViewClient(null);
            web.destroy();
            web = null;
        }
        super.onDestroy();
    }
}