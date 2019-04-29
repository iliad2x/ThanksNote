package kr.co.misosalife.thanksnote2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.WebView;

import kr.co.misosalife.thanksnote2.R;
import kr.co.misosalife.thanksnote2.webview.ThanksNoteWebProgressBar;
import kr.co.misosalife.thanksnote2.webview.ThanksNoteWebviewClient;

public class WebviewActivity extends AppCompatActivity {

    WebView web;
    private BackPressCloseHandler backPressCloseHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // progress bat setting : setContentView 전에 꼭! 적어줘야 한다.
        getWindow().requestFeature(Window.FEATURE_PROGRESS);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview);

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");

        String cookie = intent.getStringExtra("login_cookie");
        if( !cookie.equals("") ) {
            CookieManager.getInstance().setCookie(getString(R.string.url_host), cookie);
        }
        startSite( url );

        backPressCloseHandler = new BackPressCloseHandler(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 웹뷰에서 back 버튼 막기
     * @param menu
     * @return
     */
    @Override
    public boolean onKeyDown( int keyCode, KeyEvent event ) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                WebView web = (WebView) findViewById(R.id.webView);
                web.goBack();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }

    /**
     * 웹뷰 시작
     * @param url
     */
    public void startSite( String url ) {
        Intent intent = getIntent();
        String userId = intent.getStringExtra("user_id");
        String passPwd = intent.getStringExtra("user_pwd");

        web = (WebView) findViewById(R.id.webView);

        ThanksNoteWebviewClient client = new ThanksNoteWebviewClient();
        web.setWebViewClient(client);

        ThanksNoteWebProgressBar progBar = new ThanksNoteWebProgressBar(this);
        web.setWebChromeClient(progBar);

        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setSupportZoom(true);
        web.getSettings().setBuiltInZoomControls(true);
        web.getSettings().setDisplayZoomControls(false);

        // Home page Load
        web.loadUrl( url );
    }
}
