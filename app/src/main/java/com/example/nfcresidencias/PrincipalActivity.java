package com.example.nfcresidencias;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PrincipalActivity extends AppCompatActivity {

    Button buttonLogout, ActivateButton;
    String stringtoreceive;
    TextView cajaBienvenido, textView;
    private EditText inputView;
    private PendingIntent pendingIntent;
    private NdefMessage messageToWrite;
    private IntentFilter[] writeFilters;
    private String[][] writeTechList;
    private String shaSum;
    private LottieAnimationView listeningAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        textView = (TextView) findViewById(R.id.text);
        //inputView = (EditText) findViewById(R.id.input);

        Intent intent = new Intent(this,getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_MUTABLE);

        writeFilters = new IntentFilter[]{};
        writeTechList = new String[][] {
                {Ndef.class.getName()},
                {NdefFormatable.class.getName()}
        };

        listeningAnimation = findViewById(R.id.listeningAnimation);

        processNFC(getIntent());

        buttonLogout=findViewById(R.id.buttonLogout);
        ActivateButton=findViewById(R.id.ActivateButton);
        cajaBienvenido=(TextView) findViewById(R.id.textViewBienvenida);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        } else {
            cajaBienvenido.setText("Bienvenido(a), profe(a).");
        }
        stringtoreceive=extras.getString("STRING_I_NEED");
        cajaBienvenido.setText("Bienvenido(a), profe(a). "+stringtoreceive+".");

        ActivateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    byte[] language = Locale.getDefault().getLanguage().getBytes("UTF-8");
                    shaSum = generateRandomString();
                    encryptSHA256(); //shaSUM se hashea
                    byte[] text = shaSum.getBytes();
                    byte[] payload = new byte[text.length + language.length + 1];

                    payload[0] = 0x02; //UTF-8
                    System.arraycopy(language,0,payload,1, language.length);
                    System.arraycopy(text,0,payload,1 + language.length, text.length);

                    NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
                    messageToWrite = new NdefMessage(new NdefRecord[]{record});
                    textView.setText("Acerca la etiqueta NFC al dispositivo");
                    enableWrite();
                    listeningAnimation.playAnimation(); // Iniciar animación
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    String errorMessage = e.getMessage();
                    String activityName = "PrincipalActivity";
                    insertErrorLog(errorMessage, activityName);
                }
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences=getSharedPreferences("preferenciasLogin", Context.MODE_PRIVATE);
                preferences.edit().clear().commit();

                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    private void enableRead(){
        NfcAdapter.getDefaultAdapter(this).enableForegroundDispatch(this,pendingIntent,null,null);
    }
    private void enableWrite(){
        NfcAdapter.getDefaultAdapter(this).enableForegroundDispatch(this,pendingIntent,writeFilters,writeTechList);
    }
    private void disableRead(){
        NfcAdapter.getDefaultAdapter(this).disableForegroundDispatch(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableRead();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableRead();
    }

    @Override
    protected void onNewIntent (Intent intent){
        super.onNewIntent(intent);
        processNFC(intent);
    }
    private void processNFC(Intent intent){
        if(messageToWrite != null){
            writeTag(intent);
        }else{
            readTag(intent);
        }
    }

    private void writeTag(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if(tag != null){
            try {
                Ndef ndef = Ndef.get(tag);
                if (ndef == null) {
                    NdefFormatable ndefFormatable = NdefFormatable.get(tag);
                    if (ndefFormatable != null) {
                        ndefFormatable.connect();
                        ndefFormatable.format(messageToWrite);
                        ndefFormatable.close();
                        Toast.makeText(this, "La TAG se ha formateado y se le ha añadido un token", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Tag no se puede formatear", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    ndef.connect();
                    ndef.writeNdefMessage(messageToWrite);
                    ndef.close();
                    Toast.makeText(this, "Se ha añadido el token a la NFC TAG", Toast.LENGTH_SHORT).show();
                    insertHash();
                }
            }catch (FormatException | IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "No se pudo escribir en la etiqueta NFC", Toast.LENGTH_LONG).show();

                // Llamada al método insertErrorLog para registrar el error en la tabla logs
                String errorMessage = e.getMessage();
                String activityName = "PrincipalActivity";
                insertErrorLog(errorMessage, activityName);
            }finally {
                messageToWrite = null;
            }
        }
        pauseListeningAnimation();
        textView.setText("Token OTP escrito correctamente");
    }
    private void pauseListeningAnimation() {
        LottieAnimationView listeningAnimation = findViewById(R.id.listeningAnimation);
        listeningAnimation.pauseAnimation();
    }
    private void readTag(Intent intent) {
        Parcelable[] messages = intent.getParcelableArrayExtra((NfcAdapter.EXTRA_NDEF_MESSAGES));
        textView.setText("");
        if(messages != null){
            for(Parcelable message : messages) {
                NdefMessage ndefMessage = (NdefMessage) message;
                for(NdefRecord record : ndefMessage.getRecords()){
                    switch (record.getTnf()){
                        case NdefRecord.TNF_WELL_KNOWN:
                            textView.append("Token en el TAG: ");
                            if(Arrays.equals(record.getType(), NdefRecord.RTD_TEXT)){
                                textView.append(new String(record.getPayload()));
                                textView.setText(textView.getText().toString().substring(0,17)+textView.getText().toString().substring(20));
                            }
                    }
                }
            }
        }
    }
    public static String generateRandomString() {
        String CHAR_LIST = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder randomString = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            int randomIndex = (int)(Math.random() * CHAR_LIST.length());
            randomString.append(CHAR_LIST.charAt(randomIndex));
        }
        return randomString.toString();
    }

    private void encryptSHA256(){

        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        digest.reset();
        try {
            digest.update(shaSum.getBytes("utf8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        shaSum = String.format("%064x", new BigInteger(1, digest.digest()));
    }

    private void insertHash(){
        StringRequest stringRequest=new StringRequest(Request.Method.POST, "http://192.168.1.148/appNFC/almacenar_otp.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.isEmpty()){
                    System.out.println("Si jala");
                }

            }

        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(PrincipalActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros=new HashMap<String,String>();
                parametros.put("shaSum",shaSum);
                return parametros;
            }

        };
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    // Método para insertar un registro de error en la tabla logs
    private void insertErrorLog(String errorMessage, String activityName) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://192.168.1.148/appNFC/insert_error_log.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Si la respuesta del servidor no está vacía, se insertó correctamente el registro del error
                        if (!response.isEmpty()) {
                            System.out.println("Error registrado correctamente");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PrincipalActivity.this, "Error al insertar el registro de error: " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("error_message", errorMessage);
                parametros.put("activity_name", activityName);
                return parametros;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


}