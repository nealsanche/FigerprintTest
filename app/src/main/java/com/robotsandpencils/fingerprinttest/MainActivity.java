package com.robotsandpencils.fingerprinttest;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.mtramin.rxfingerprint.RxFingerprint;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        /*
        Subscription subscription = RxFingerprint.authenticate(this)
                .subscribe(fingerprintAuthenticationResult -> {
                    switch (fingerprintAuthenticationResult.getResult()) {
                        case FAILED:
                            setStatusText("Fingerprint not recognized, try again!");
                            break;
                        case HELP:
                            setStatusText(fingerprintAuthenticationResult.getMessage());
                            break;
                        case AUTHENTICATED:
                            setStatusText("Successfully authenticated!");
                            break;
                    }
                }, throwable -> {
                    Log.e("ERROR", "authenticate", throwable);
                });
                */

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);

        if (!prefs.contains("password")) {
            RxFingerprint.encrypt(this, "password")
                    .subscribe(fingerprintEncryptionResult -> {
                        switch (fingerprintEncryptionResult.getResult()) {
                            case FAILED:
                                setStatusText("Fingerprint not recognized, try again!");
                                break;
                            case HELP:
                                setStatusText(fingerprintEncryptionResult.getMessage());
                                break;
                            case AUTHENTICATED:
                                setStatusText("Successfully authenticated!");
                                Log.e("ENCRYPTED", fingerprintEncryptionResult.getEncrypted());

                                prefs.edit().putString("password", fingerprintEncryptionResult.getEncrypted()).apply();
                                break;
                        }
                    }, throwable -> {
                        Log.e("ERROR", "encrypt", throwable);
                    });
        } else {
            RxFingerprint.decrypt(this, prefs.getString("password", ""))
                    .subscribe(fingerprintDecryptionResult -> {
                                switch (fingerprintDecryptionResult.getResult()) {
                                    case FAILED:
                                        setStatusText("Fingerprint not recognized, try again!");
                                        break;
                                    case HELP:
                                        setStatusText(fingerprintDecryptionResult.getMessage());
                                        break;
                                    case AUTHENTICATED:
                                        setStatusText("Successfully authenticated!");
                                        Log.e("DECRYPTED", fingerprintDecryptionResult.getDecrypted());
                                        break;
                                }
                            }
                            , throwable -> {
                                Log.e("ERROR", "decrypt", throwable);
                            });


        }
    }

    private void setStatusText(String s) {
        Snackbar.make(mFab, s, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

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
}
