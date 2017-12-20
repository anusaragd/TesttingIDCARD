package com.example.masters.testtingidcard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import static android.os.Build.ID;
import static com.example.masters.testtingidcard.user.username;

public class LoginIDCardActivity extends Activity {

    EditText Username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_idcard);

//        Username = (EditText)findViewById(R.id.username);
//        Username.getText().toString();

    }

    public void EnviarOnClick(View v){


        Thread nt = new Thread(){
            String res;
            EditText Username = (EditText)findViewById(R.id.username);
            EditText Password = (EditText)findViewById(R.id.password);


            @Override
            public void run(){

//                Intent i = new Intent(getApplicationContext(), Search.class);
//                startActivity(i);

//                String NAMESPACE ="http://tempuri.org/";
//                String URL ="http://10.0.0.36/webservice/WebService1.asmx?";
//                String METHOD_NAME ="Login";
//                String SOAP_ACTION = "http://tempuri.org/Login";
//
//
//                SoapObject request = new SoapObject(NAMESPACE,METHOD_NAME);
//                request.addProperty("USR",Username.getText().toString());
//                request.addProperty("PWD",Password.getText().toString());
//
//
//                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
//                envelope.dotNet = true;
//
//                envelope.setOutputSoapObject(request);
//
//                HttpTransportSE transportSE = new HttpTransportSE(URL);
//
//                try {
//                    transportSE.call(SOAP_ACTION,envelope);
//                    SoapPrimitive result = (SoapPrimitive) envelope.getResponse();
//                    res = result.toString();
//
//                } catch (IOException | XmlPullParserException e) {
//                    e.printStackTrace();
//                }
//
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        username = Username.toString(); ///// save Username
                        Toast.makeText(LoginIDCardActivity.this,"aaaaaa",Toast.LENGTH_LONG).show();// แสดง Pop - up
                        Intent intent = new Intent(LoginIDCardActivity.this,CreateActivity.class);
                        startActivity(intent);
                        Log.e("bbbbbbbb",username);

                    }

                });

            }
        };

        nt.start();
    }


}
