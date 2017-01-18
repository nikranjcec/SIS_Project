package com.kranjcecni.fingerprintdemo;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_NAME = "example_key";
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    public Cipher cipher;
    public Cipher cipherDecrypt;
    public FingerprintManager.CryptoObject cryptoObject;


    private Button btnCrypt;
    private Button btnDecypt;
    private EditText txtPlainText;
    private TextView txtCryptText;
    private TextView txtDecyptText;
    private SecretKey secretKey;
    private  byte[] encrypted;


    private static final String SECRET_KEY_1 = "ssdkF$HUy2A#D%kd";
    private IvParameterSpec ivParameterSpec;

    //Instanciranje i inicijaliziranje instance Cipher
    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Instanca Cipher nije kreirana", e);
        }


        try {
            keyStore.load(null);
             secretKey = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Instanca Cipher nije inicijalizirana", e);
        }

    }

    public boolean cipherInitDecrypt() {
        try {
            cipherDecrypt = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }
        try {
            keyStore.load(null);
            secretKey = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            try {
                cipherDecrypt.init(Cipher.DECRYPT_MODE , secretKey, cipher.getParameters());
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            }
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }

    }


    protected void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore"); //kreiranje instance KeySotre
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"); //instanca KeyGenerator
        } catch (NoSuchAlgorithmException |
                NoSuchProviderException e) {
            throw new RuntimeException(
                    "KeyGenerator nije instanciran", e);
        }

        try {
            keyStore.load(null); // inicijaliziranje ključa
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException |
                InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        initWidgets();
        setUpListeners();

        /* Provjera dali je na uređaju uključeno
        otkljuavanje pinom ili uzorkom*/

        keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

        if (!keyguardManager.isKeyguardSecure()) {

            Toast.makeText(this,
                    "Nije uključeno otključavanje uređaja pinom ili uzorkom",
                    Toast.LENGTH_LONG).show();
            return;
        }

        /*Provjera dli je registriran
        barem jedan otisak prsta*
         */
        fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

        if (!fingerprintManager.hasEnrolledFingerprints()) {

            // This happens when no fingerprints are registered.
            Toast.makeText(this,
                    "Potrebno je u postavkama ragistrirati barem jedan otisak",
                    Toast.LENGTH_LONG).show();
            return;
        }


        /*Provjera dali je uključena dozvola
        za autentifikaciju otiskom prsta*/

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.USE_FINGERPRINT) !=
                PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,
                    "Autentifikacija otiskom prsta nije dozvoljena",
                    Toast.LENGTH_LONG).show();

            return;
        }



        generateKey();
    }

    private void setUpListeners() {
        btnCrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cipherInit()) {

                    //kreiranje CryptoObject instance
                    cryptoObject = new FingerprintManager.CryptoObject(cipher);

                    LogInFragment fragment = new LogInFragment();
                    fragment.setCryptoObject(cryptoObject, 1);
                    fragment.setFingerprintManager(fingerprintManager);
                    fragment.show(getFragmentManager(), "LIF");

                }

            }
        });

        btnDecypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cipherInitDecrypt()) {
                    cryptoObject = new FingerprintManager.CryptoObject(cipherDecrypt);
                    LogInFragment fragment = new LogInFragment();
                    fragment.setCryptoObject(cryptoObject, 2);
                    fragment.setFingerprintManager(fingerprintManager);
                    fragment.show(getFragmentManager(), "LIF");

                }

            }
        });
    }

    private void initWidgets() {

        btnCrypt = (Button) findViewById(R.id.btnCrypt);
        btnDecypt = (Button) findViewById(R.id.btnDecrypt);
        txtPlainText = (EditText) findViewById(R.id.txtPlainText);
        txtCryptText = (TextView) findViewById(R.id.txtCryptText);
        txtDecyptText = (TextView) findViewById(R.id.txtDecrypttText);
    }
    //kriptiranje unesenog teksta
    public void crypt (){

        String plainText = txtPlainText.getText().toString();
        try {
            encrypted = cipher.doFinal(plainText.getBytes());
            txtCryptText.setText(Base64.encodeToString(encrypted, 0));
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
    }

    String desifrirano;
    //dekriptiranje kriptiranog teksta
    public void decrypt(){
        String cryptText = txtCryptText.getText().toString();
        try {
            byte[] decrypted = cipherDecrypt.doFinal(encrypted);
            try {
                 desifrirano = new String (decrypted, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            txtDecyptText.setText(desifrirano);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
    }


}
