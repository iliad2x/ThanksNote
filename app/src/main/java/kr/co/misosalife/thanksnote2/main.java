package kr.co.misosalife.thanksnote2;

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

public class main extends Activity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_main );

        try {
            int contactPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);

            if (contactPermission == PackageManager.PERMISSION_GRANTED) {

                startLogin();

            } else {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, PERMISSIONS_REQUEST_CODE);

                } else {

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, PERMISSIONS_REQUEST_CODE);

                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults) {

        switch( requestCode ){
            case PERMISSIONS_REQUEST_CODE:
            {
                if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    // permission was granted
                    startLogin();
                }else{
                    View loadingView = findViewById(R.id.loading);
                    final Context self = this;
                    Snackbar.make(loadingView, "이 앱을 실행하려면 연락처 읽기 권한이 필요합니다.", Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                            ActivityCompat.requestPermissions((main) self, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, PERMISSIONS_REQUEST_CODE);
                        }
                    }).show();
                }
                return;
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void startLogin(){
        Intent intent = new Intent( this, LoginActivity.class );
        startActivity( intent);
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
