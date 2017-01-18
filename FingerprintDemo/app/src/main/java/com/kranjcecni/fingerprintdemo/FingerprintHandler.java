package com.kranjcecni.fingerprintdemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.telecom.Call;
import android.widget.Toast;

/**
 * Created by nikra on 29.12.2016..
 */

public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private CancellationSignal cancellationSignal;
    private Context appContext;
    private final Callback mCallback;

    public FingerprintHandler(Context context, Callback callback) {
        appContext = context;
        mCallback = callback;
    }

    public void startAuth(FingerprintManager manager,
                          FingerprintManager.CryptoObject cryptoObject) {

        cancellationSignal = new CancellationSignal();

        if (ActivityCompat.checkSelfPermission(appContext,
                Manifest.permission.USE_FINGERPRINT) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    @Override
    public void onAuthenticationError(int errMsgId,
                                      CharSequence errString) {
       mCallback.onError("Neuspijela autentifikacija\n" + errString);
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId,
                                     CharSequence helpString) {
        mCallback.onError( "Neuspijela autentifikacija\n" + helpString);
    }

    @Override
    public void onAuthenticationFailed() {

        mCallback.onError("Neuspijela autentifikacija");
    }

    @Override
    public void onAuthenticationSucceeded(
            FingerprintManager.AuthenticationResult result) {

        mCallback.onAuthenticated();

    }

    public interface Callback{

        void onAuthenticated();
        void onError(String poruka);
    }
}
