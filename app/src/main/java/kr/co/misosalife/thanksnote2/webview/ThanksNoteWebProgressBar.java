package kr.co.misosalife.thanksnote2.webview;

import android.support.v7.app.AppCompatActivity;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * Created by iliad on 2015-08-23.
 */
public class ThanksNoteWebProgressBar extends WebChromeClient {

    private AppCompatActivity currentAct;

    public ThanksNoteWebProgressBar(AppCompatActivity act) {
        currentAct = act;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        currentAct.setProgress( newProgress * 100 );
    }
}
