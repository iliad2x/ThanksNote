package kr.co.misosalife.thanksnote2;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import kr.co.misosalife.thanksnote2.R;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity implements LoaderCallbacks<Cursor> {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private BackPressCloseHandler backPressCloseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        final Context context = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        backPressCloseHandler = new BackPressCloseHandler(this);


        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.user_id);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });


        TextView btnFindid = (TextView) findViewById(R.id.btn_find_info);
        btnFindid.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startFind();
            }
        });

        TextView btnRegist = (TextView) findViewById(R.id.btn_regist);
        btnRegist.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startRegist();
            }
        });


        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        getAutoLogin();

    }

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * 자동 로그인 처리
     */
    private void setAutoLogin( String id, String pw ) {

        SharedPreferences mPreference = getSharedPreferences(getString(R.string.shared_login), MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreference.edit();

        Switch auto = (Switch) findViewById(R.id.auto_login);
        boolean autoLogin = auto.isChecked();

        editor.putBoolean(getString(R.string.shared_autologin), autoLogin);

        if( autoLogin ) {
            editor.putString( getString(R.string.shared_id), id );
            editor.putString( getString(R.string.shared_pwd), pw );
        } else {
            editor.putString( getString(R.string.shared_id), "" );
            editor.putString( getString(R.string.shared_pwd), "" );
        }

        editor.commit();
    }


    /**
     * 자동 로그인 확인
     */
    private void getAutoLogin() {

        SharedPreferences mPreference = getSharedPreferences(getString(R.string.shared_login), MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreference.edit();

        boolean autoLogin = mPreference.getBoolean(getString(R.string.shared_autologin), false);
        String id = mPreference.getString(getString(R.string.shared_id), "");
        String pw = mPreference.getString(getString(R.string.shared_pwd), "");

        if( autoLogin && !id.equals("") && !pw.equals("") ) {
            mEmailView.setText(id);
            mPasswordView.setText(pw);
            attemptLogin();
        }

    }



    /**
     * 로그인 성공 후 홈페이지 열기
     */
    private void startSite( String id, String pwd, String cookie) {
        final Context context = this;
        Intent intent = new Intent( context, WebviewActivity.class);
        intent.putExtra( "url", getString(R.string.url_subject));
        intent.putExtra("user_id", id);
        intent.putExtra("user_pwd", pwd);
        intent.putExtra("login_cookie", cookie);
        startActivity(intent);
    }


    /**
     * 회원가입 페이지 열기
     */
    private void startRegist() {
        final Context context = this;
        Intent intent = new Intent( context, WebviewActivity.class);
        intent.putExtra( "url", getString(R.string.url_regist));
        intent.putExtra("user_id", "");
        intent.putExtra("user_pwd", "");
        intent.putExtra("login_cookie", "");
        startActivity(intent);
    }

    /**
     * 아이디/비밀번호 찾기 페이지 열기
     */
    private void startFind() {
        final Context context = this;
        Intent intent = new Intent( context, WebviewActivity.class);
        intent.putExtra( "url", getString(R.string.url_find));
        intent.putExtra("user_id", "");
        intent.putExtra("user_pwd", "");
        intent.putExtra("login_cookie", "");
        startActivity( intent );
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
//        View focusView = null;

        // Check for a valid password, if the user entered one.
        /*if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }*/

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
//            focusView = mEmailView;
            cancel = true;
        } /*else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }*/

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
//            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }


    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private String errorMessage;
        private String loginCookie;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            String data = "";

            try {
                data += URLEncoder.encode("mode", "UTF-8") + "=" + URLEncoder.encode("", "UTF-8") + "&";
                data += URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(mEmail, "UTF-8") + "&";
                data += URLEncoder.encode("pw", "UTF-8") + "=" + URLEncoder.encode(mPassword, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return false;
            }

            try {
                // Simulate network access.
                Thread.sleep(2000);

                try {
                    URL url = new URL(getString(R.string.url_login));
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.write(data);
                    wr.flush();

                    XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = parserCreator.newPullParser();
                    parser.setInput(conn.getInputStream(), null);
                    int parserEvent = parser.getEventType();
                    String tag = "", result = "", message = "";
                    boolean code = false, msg = false;
                    while( parserEvent != XmlPullParser.END_DOCUMENT ) {
                        switch( parserEvent  ) {

                            case XmlPullParser.TEXT:
                                tag = parser.getName();
                                if( code ) {
                                    result = parser.getText();
                                } else if( msg ) {
                                    message = parser.getText();
                                }

                                break;

                            case XmlPullParser.START_TAG:
                                tag = parser.getName();
                                if( tag.compareTo("code") == 0 ) {
                                    code = true;
                                } else if( tag.compareTo("msg") == 0) {
                                    msg = true;
                                }

                                break;

                            case XmlPullParser.END_TAG:
                                tag = parser.getName();
                                if( tag.compareTo("code") == 0 ) {
                                    code = false;
                                } else if( tag.compareTo("msg") == 0 ) {
                                    msg = false;
                                }

                                break;
                        }

                        parserEvent = parser.next();
                    }

                    if( result.equalsIgnoreCase("success")) {

                        loginCookie = conn.getHeaderField("Set-Cookie");
                        setAutoLogin(mEmail,mPassword);

                        return true;
                    } else {
                        errorMessage = message;
                        return false;
                    }

                }catch(Exception e) {
                    return false;
                }

            } catch (InterruptedException e) {
                return false;
            }

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                setAutoLogin( mEmail, mPassword);
                startSite( mEmail, mPassword, loginCookie );
            } else {
//                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.setError(errorMessage);
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

