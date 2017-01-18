package com.kranjcecni.fingerprintdemo;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.KeyguardManager;
import android.content.DialogInterface;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.app.Fragment;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import static android.content.Context.FINGERPRINT_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 */
public class LogInFragment extends DialogFragment implements FingerprintHandler.Callback {

    private FingerprintManager.CryptoObject mCryptoObject;
    private MainActivity mActivity;
    private FingerprintHandler mFingerprintHandler;
    private TextView txtFingerprintError;
    private FingerprintManager mFingerprintManager;
    private int type;

    public void setCryptoObject(FingerprintManager.CryptoObject cryptoObject, int type) {
        mCryptoObject = cryptoObject;
        this.type = type;
    }

    public void setFingerprintManager(FingerprintManager fingerprintManager) {
         mFingerprintManager = fingerprintManager;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mActivity = (MainActivity) getActivity();

        //Pozivanje metode za autentifikaciju
        mFingerprintHandler = new FingerprintHandler(getActivity(), this);
        mFingerprintHandler.startAuth(mFingerprintManager,mCryptoObject);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.login);
        builder.setMessage(R.string.stavitePrst);

        LayoutInflater li = getActivity().getLayoutInflater();
        View v = li.inflate(R.layout.fragment_log_in, null);
        txtFingerprintError = (TextView) v.findViewById(R.id.txtFingerprintError);
        txtFingerprintError.setVisibility(View.GONE);
                builder.setView(v);

        builder.setNegativeButton(R.string.odustani, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });

        AlertDialog dialog = builder.create();


        return dialog;
    }

    @Override
    public void onAuthenticated() {

        if (this.type == 2){
            mActivity.decrypt();
        }else{
            mActivity.crypt();
        }
        dismiss();
    }

    @Override
    public void onError(String poruka) {
        txtFingerprintError.setVisibility(View.VISIBLE);
        txtFingerprintError.setText(poruka);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

}
