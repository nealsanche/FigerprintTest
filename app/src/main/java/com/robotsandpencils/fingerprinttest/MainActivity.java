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

import com.afollestad.materialdialogs.MaterialDialog;
import com.mattprecious.swirl.SwirlView;
import com.mtramin.rxfingerprint.RxFingerprint;

public class MainActivity extends AppCompatActivity {

    public static final int DELAY_MILLIS = 1500;
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

        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.title)
                .customView(R.layout.fingerprint_view, false)
                .positiveText(R.string.use_password)
                .negativeText(R.string.cancel)
                .show();

        SwirlView swirlView = (SwirlView) dialog.findViewById(R.id.swirl_view);

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);

        swirlView.setState(SwirlView.State.ON, true);

        if (!prefs.contains("password")) {
            RxFingerprint.encrypt(this, "password")
                    .subscribe(fingerprintEncryptionResult -> {
                        switch (fingerprintEncryptionResult.getResult()) {
                            case FAILED:
                                swirlView.setState(SwirlView.State.ERROR, true);
                                swirlView.postDelayed(() -> {
                                    swirlView.setState(SwirlView.State.ON, true);
                                }, DELAY_MILLIS);
                                setStatusText("Fingerprint not recognized, try again!");
                                break;
                            case HELP:
                                swirlView.setState(SwirlView.State.ERROR, true);
                                swirlView.postDelayed(() -> {
                                    swirlView.setState(SwirlView.State.ON, true);
                                }, DELAY_MILLIS);
                                setStatusText(fingerprintEncryptionResult.getMessage());
                                break;
                            case AUTHENTICATED:
                                swirlView.post(() -> {
                                    swirlView.setState(SwirlView.State.OFF, true);
                                });
                                swirlView.postDelayed(() -> {
                                    dialog.dismiss();
                                }, DELAY_MILLIS);
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
                                        swirlView.setState(SwirlView.State.ERROR, true);
                                        swirlView.postDelayed(() -> {
                                            swirlView.setState(SwirlView.State.ON, true);
                                        }, DELAY_MILLIS);
                                        setStatusText("Fingerprint not recognized, try again!");
                                        break;
                                    case HELP:
                                        swirlView.post(() -> {
                                            swirlView.setState(SwirlView.State.ERROR, true);
                                            swirlView.postDelayed(() -> {
                                                swirlView.setState(SwirlView.State.ON, true);
                                            }, DELAY_MILLIS);
                                        });
                                        setStatusText(fingerprintDecryptionResult.getMessage());
                                        break;
                                    case AUTHENTICATED:
                                        swirlView.post(() -> {
                                            swirlView.setState(SwirlView.State.OFF, true);
                                        });
                                        swirlView.postDelayed(() -> {
                                            dialog.dismiss();
                                        }, DELAY_MILLIS);
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
