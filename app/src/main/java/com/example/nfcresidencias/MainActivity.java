package com.example.nfcresidencias;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    EditText edtUser,edtPassword;
    Button btnLogin, btnAlert;
    String user,password,nombres;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edtUser=findViewById(R.id.editTextUser);
        edtPassword=findViewById(R.id.editTextPassword);
        btnLogin=findViewById(R.id.button);
        btnAlert=findViewById(R.id.buttonAlert);

        loadPreferences();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user=edtUser.getText().toString();
                password=edtPassword.getText().toString();
                encryptPassword();
                validateUser();
            }
        });
        btnAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alerta=new AlertDialog.Builder(MainActivity.this);
                alerta.setMessage("Por favor, acuda al centro de cómputo.");
                alerta.setCancelable(false);
                alerta.setPositiveButton("Salir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                alerta.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                AlertDialog titulo=alerta.create();
                titulo.setTitle("Alerta");
                titulo.show();
            }
        });
    }

    private void validateUser(String URL){
        StringRequest stringRequest=new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.isEmpty()){
                    //Obtener nombre de la Query y guardarlo en var local nombres
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        if (status.equals("success")) {
                            nombres = jsonObject.getString("nombres");
                            savePreferences();
                            Intent intent=new Intent(getApplicationContext(),PrincipalActivity.class);
                            intent.putExtra("STRING_I_NEED",nombres);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, "Credenciales incorrectas, verifique e intente de nuevo", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        // Manejar el error
                    }



                }else{
                    //no hay error que manejar pq no se abre nueva actividad
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros=new HashMap<String,String>();
                parametros.put("usuario",user);
                parametros.put("password",password);
                return parametros;
            }
        };

        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    //----------------------------------------Métodos a invocar----------------------------------------

    private void savePreferences(){
        SharedPreferences preferences=getSharedPreferences("preferenciasLogin", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString("user",user);
        editor.putBoolean("sesion",true);
        editor.commit();
    }
    private void loadPreferences(){
        SharedPreferences preferences=getSharedPreferences("preferenciasLogin", Context.MODE_PRIVATE);
        edtUser.setText(preferences.getString("user",null));
    }
    private void encryptPassword(){
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        digest.reset();
        try {
            digest.update(password.getBytes("utf8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        password = String.format("%064x", new BigInteger(1, digest.digest()));
    }
    private void validateUser(){
        if(!user.isEmpty() && !password.isEmpty()){
            validateUser("http://192.168.0.137/appNFC/validar_usuario.php");
        }else{
            Toast.makeText(MainActivity.this, "No se permiten campos vacíos", Toast.LENGTH_SHORT).show();
        }
    }
}