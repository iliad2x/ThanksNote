package kr.co.misosalife.thanksnote2.webview;

import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by iliad on 2015-08-23.
 */
public class ThanksNoteWebviewClient extends WebViewClient {

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url ){
        view.loadUrl( url );
        return true;
    }

}
