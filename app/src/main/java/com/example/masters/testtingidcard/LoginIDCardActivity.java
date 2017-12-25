package com.example.masters.testtingidcard;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import static android.os.Build.ID;
import static com.example.masters.testtingidcard.user.username;

public class LoginIDCardActivity extends Activity {

    EditText Username, Password;
    Button Login;
    TextView show;


//    String URL = "http://203.151.213.80/webservice/WebService1.asmx?";
//    String NAMESPACE = "http://tempuri.org/";
//    String SOAP_ACTION = "http://tempuri.org/Login";
//    String METHOD_NAME = "Login ";
//    String PARAMETER_NAME = "USER_NAME";

    private final String NAMESPACE = "http://tempuri.org/";
    private final String URL = "http://203.151.213.80/webservice/WebService1.asmx?";
    private final String SOAP_ACTION = "http://tempuri.org/Login";
    private final String METHOD_NAME = "Login";
    /** Called when the activity is first created. */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_idcard);

        Username = (EditText) findViewById(R.id.username);
        Password = (EditText) findViewById(R.id.password);
        show = (TextView) findViewById(R.id.textView3);

        Login = (Button) findViewById(R.id.button);
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                new CallWebService().execute(Username.getText().toString());
                new LongOperation().execute();
            }
        });
//        Username.getText().toString();

    }

    class LongOperation extends AsyncTask<String,Void,String>{
        private ProgressDialog Dialog = new ProgressDialog(LoginIDCardActivity.this);

        @Override
        protected String doInBackground(String... strings) {
            loginAction();
            return null;
        }

        protected void onPreExecute(){
            Dialog.setMessage("Loading...");
            Dialog.show();
        }

        protected  void onPostExecute(String resultGot){
            Dialog.dismiss();
        }

    }
    private void loginAction(){
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

        EditText userName = (EditText) findViewById(R.id.username);
        String user_Name = userName.getText().toString();
        EditText userPassword = (EditText) findViewById(R.id.password);
        String user_Password = userPassword.getText().toString();

        //Pass value for userName variable of the web service
        PropertyInfo unameProp = new PropertyInfo();
        unameProp.setName("userName");
        unameProp.setValue(user_Name);
        unameProp.setType(String.class);
        request.addProperty(unameProp);

        //Pass value for Password variable of the web service
        PropertyInfo passwordProp = new PropertyInfo();
        passwordProp.setName("password");
        passwordProp.setValue(user_Password);
        passwordProp.setType(String.class);
        request.addProperty(passwordProp);

        final SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);
        final HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

//        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
//        envelope.setOutputSoapObject(request);
//        androidHttpTransport = new HttpTransportSE(URL);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    androidHttpTransport.call(SOAP_ACTION, envelope);
                    SoapPrimitive response = (SoapPrimitive)envelope.getResponse();
                    
                    TextView result = (TextView)findViewById(R.id.textView3);
                    result.setText(response.toString());
                }
                catch (Exception e){
                    
                }
            }
        });

    }


//    class CallWebService extends AsyncTask <String,Void,String>{
//        @Override
//        protected String doInBackground(String... strings) {
//
//            String result = "";
//            SoapObject soapObject = new SoapObject(NAMESPACE, METHOD_NAME);
//
//            PropertyInfo propertyInfo = new PropertyInfo();
//            propertyInfo.setName(PARAMETER_NAME);
//            propertyInfo.setValue(params[0]);
//            propertyInfo.setType(String.class);
//
//            soapObject.addProperty(propertyInfo);
//
//            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
//            envelope.setOutputSoapObject(soapObject);
//
//            HttpTransportSE httpTransportSE = new HttpTransportSE(URL);
//
//            try {
//                httpTransportSE.call(SOAP_ACTION, envelope);
//                SoapPrimitive soapPrimitive = (SoapPrimitive)envelope.getResponse();
//                result = soapPrimitive.toString();
//            }
//            catch (Exception e){
//                e.printStackTrace();
//            }
//
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(String s){
//            show.setText("aaaaa" + s);
//        }
//    }


//    public void EnviarOnClick(View v){
//
//
//        Thread nt = new Thread(){
//            String res;
//            EditText Username = (EditText)findViewById(R.id.username);
//            EditText Password = (EditText)findViewById(R.id.password);
//
//
//            @Override
//            public void run(){
//
////                Intent i = new Intent(getApplicationContext(), Search.class);
////                startActivity(i);
//
////                String NAMESPACE ="http://tempuri.org/";
////                String URL ="http://10.0.0.36/webservice/WebService1.asmx?";
////                String METHOD_NAME ="Login";
////                String SOAP_ACTION = "http://tempuri.org/Login";
////
////
////                SoapObject request = new SoapObject(NAMESPACE,METHOD_NAME);
////                request.addProperty("USR",Username.getText().toString());
////                request.addProperty("PWD",Password.getText().toString());
////
////
////                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
////                envelope.dotNet = true;
////
////                envelope.setOutputSoapObject(request);
////
////                HttpTransportSE transportSE = new HttpTransportSE(URL);
////
////                try {
////                    transportSE.call(SOAP_ACTION,envelope);
////                    SoapPrimitive result = (SoapPrimitive) envelope.getResponse();
////                    res = result.toString();
////
////                } catch (IOException | XmlPullParserException e) {
////                    e.printStackTrace();
////                }
////
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        username = Username.toString(); ///// save Username
//                        Toast.makeText(LoginIDCardActivity.this,"aaaaaa",Toast.LENGTH_LONG).show();// แสดง Pop - up
//                        Intent intent = new Intent(LoginIDCardActivity.this,CreateActivity.class);
//                        startActivity(intent);
//                        Log.e("bbbbbbbb",username);
//
//                    }
//
//                });
//
//            }
//        };
//
//        nt.start();
//    }


}
