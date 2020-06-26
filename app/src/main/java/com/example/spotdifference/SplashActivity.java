package com.example.spotdifference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class SplashActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final int RC_SIGN_IN = 100;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth mAuth;

    private Handler mHandler = new Handler();

    private static final int SDCARD_PERMISSION = 1;

    SignInButton Google_Login;
    ImageView splash;
    Button startb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        Google_Login = findViewById(R.id.googleLogin);

        mHandler.postDelayed(mMyTask, 1000);

        start();

        // Google 로그인을 앱에 통합
        // GoogleSignInOptions 개체를 구성할 때 requestIdToken을 호출
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("159951731205-ho18o9af0js557rcc4vfs9i6vrd7sodi.apps.googleusercontent.com")
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        Google_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkStoragePermission();
                Intent intent = googleSignInClient.getSignInIntent();
                startActivityForResult(intent, RC_SIGN_IN);
            }
        });


    }

    public void start() { //로고 올라가고, 구글로그인 띄우기
        splash = findViewById(R.id.splash_logo);
        Google_Login = findViewById(R.id.googleLogin);

        TranslateAnimation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.3f,
                Animation.RELATIVE_TO_SELF, -0.1f);
        animation.setFillAfter(true); // 이동 이후 자리에 고정
        animation.setDuration(2000); // 3초 간 이동

        splash.startAnimation(animation);

        Animation buttonani = new AlphaAnimation(0, 1);
        buttonani.setStartOffset(2000);
        buttonani.setDuration(2000);
        Google_Login.startAnimation(buttonani);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Toast.makeText(SplashActivity.this, "Failed! Please Login Again..", Toast.LENGTH_SHORT).show();

            }
        }
    }

    // 사용자가 정상적으로 로그인한 후에 GoogleSignInAccount 개체에서 ID 토큰을 가져와서
// Firebase 사용자 인증 정보로 교환하고 Firebase 사용자 인증 정보를 사용해 Firebase에 인증합니다.
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(SplashActivity.this, "Failed! Please Login Again..", Toast.LENGTH_SHORT).show();
                        } else {
                            finish();
                            Intent myIntent = new Intent(SplashActivity.this, MainActivity.class);
                            startActivity(myIntent);
                            Toast.makeText(SplashActivity.this, "Welcome to Spot Difference!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        ;
    }

    private Runnable mMyTask = new Runnable() {
        @Override
        public void run() {
            if (mAuth.getCurrentUser() != null) {
                //이미 로그인 되었다면 이 액티비티를 종료함
                Toast.makeText(SplashActivity.this, "Already Login.", Toast.LENGTH_SHORT).show();
                finish();
                //그리고 profile 액티비티를 연다.
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            } else {
                Google_Login.setVisibility(View.VISIBLE);

            }
        }
    };

    /*permission 보내는 코드 */
    void checkStoragePermission() { //스플레쉬 화면으로 옮기는게 좋을듯
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                        SDCARD_PERMISSION);
            }
        }
    }

}

