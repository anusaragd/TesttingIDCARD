package com.example.masters.testtingidcard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ShowroomActivity extends Activity {

    TextView showname, id_customer;
    Button Create;

    ListView Listshow;
    ArrayAdapter<String> adapter;

    private final String NAMESPACE = "http://tempuri.org/";
    private final String URL = "http://10.0.0.36/webservice/WebService1.asmx/Register_Room"; // WSDL URL
    private final String SOAP_ACTION = "http://tempuri.org/Register_Room";
    private final String METHOD_NAME = "Register_Room"; // Method on web service

    ArrayList<HashMap<String, String>> MyArrList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showroom);

        // Permission StrictMode
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

//        ShowData();

        showname = (TextView) findViewById(R.id.username);
        showname.setText(user.username);

//        id_customer = (TextView)findViewById(R.id.idview);
//        id_customer.setText(ID_Customer);

//        Listshow = (ListView)findViewById(R.id.listview_room);
//        Listshow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view,
//                                    int position, long id) {
//                Toast.makeText(getApplicationContext(), Listshow.getItemAtPosition(position).toString(),
//                        Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(ShowroomActivity.this,DescriptionActivity.class);
//                startActivity(intent);
//            }
//        });

        Create = (Button) findViewById(R.id.create_button);
        Create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ShowroomActivity.this, LoginIDCardActivity.class);
                startActivity(intent);

            }
        });

        Listshow = (ListView) findViewById(R.id.listview_room);
//        Listshow.setAdapter(adapter);
        Listshow.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(getApplicationContext(), Listshow.getItemAtPosition(position).toString(),
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ShowroomActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }

//    public void ShowData()
//    {
//        // listView1
//        final ListView lisView = (ListView)findViewById(R.id.listview_room);
//
//        // keySearch
//        TextView strKeySearch = (TextView)findViewById(R.id.idview);
//
//        // Disbled Keyboard auto focus
//        InputMethodManager imm = (InputMethodManager)getSystemService(
//                Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(lisView.getWindowToken(), 0);
//
//
//        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
//        request.addProperty("strName", strKeySearch.getText().toString());
//
//        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
//                SoapEnvelope.VER11);
//
//        envelope.setOutputSoapObject(request);
//
//        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
//
//
//        try {
//
//            androidHttpTransport.call(SOAP_ACTION, envelope);
//            SoapObject result = (SoapObject) envelope.bodyIn;
//
//            if (result != null) {
//
//                /**
//                 * [{"MemberID":"1","Username":"weerachai","Password":"weerachai@1","Name":"Weerachai Nukitram","Tel":"0819876107","Email":"weerachai@thaicreate.com"},
//                 * {"MemberID":"2","Username":"adisorn","Password":"adisorn@2","Name":"Adisorn Bunsong","Tel":"021978032","Email":"adisorn@thaicreate.com"},
//                 * {"MemberID":"3","Username":"surachai","Password":"surachai@3","Name":"Surachai Sirisart","Tel":"0876543210","Email":"surachai@thaicreate.com"}]
//                 */
//
//                MyArrList = new ArrayList<HashMap<String, String>>();
//                HashMap<String, String> map;
//
//                JSONArray data = new JSONArray(result.getProperty(0).toString());
//
//                for(int i = 0; i < data.length(); i++){
//                    JSONObject c = data.getJSONObject(i);
//
//                    map = new HashMap<String, String>();
//                    map.put("Description", c.getString("Description"));
////                    map.put("Username", c.getString("Username"));
////                    map.put("Password", c.getString("Password"));
////                    map.put("Name", c.getString("Name"));
////                    map.put("Email", c.getString("Email"));
////                    map.put("Tel", c.getString("Tel"));
//                    MyArrList.add(map);
//                }
//
////                lisView1.setAdapter(new ImageAdapter(this));
//
//
//            } else {
//                Toast.makeText(getApplicationContext(),
//                        "Web Service not Response!", Toast.LENGTH_LONG)
//                        .show();
//            }
//
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (XmlPullParserException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (JSONException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

//    }
}
