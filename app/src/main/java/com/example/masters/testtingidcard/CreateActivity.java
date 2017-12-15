package com.example.masters.testtingidcard;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
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

import static com.example.masters.testtingidcard.user.username;

public class CreateActivity extends AppCompatActivity {


    EditText nameroome;
    //    MultiAutoCompleteTextView description;
    ArrayAdapter<String> adapter;
    String username, description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
//        nameroome = (EditText) findViewById(R.id.Room_name);
//        nameroome.getText();
//        description = (MultiAutoCompleteTextView) findViewById(R.id.multiAutoCompleteTextView2);

//        Button save = (Button) findViewById(R.id.create_button);
//        save.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            }
//
//
//        });
//

    }

}