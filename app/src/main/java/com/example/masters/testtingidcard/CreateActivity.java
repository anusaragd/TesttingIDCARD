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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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

//        nameroome = (EditText)findViewById(R.id.Room_name);
//        nameroome.getText();
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

//    private void logMsg(String msg){
//
//        DateFormat dateFormat = new SimpleDateFormat("[dd-MM-yyyy HH:mm:ss]: ");
//        Date date = new Date();
//        String oldMsg = mResponseTextView.getText().toString();
//
//        mResponseTextView
//                .setText(oldMsg + "\n" + dateFormat.format(date) + msg);
//
//        if (mResponseTextView.getLineCount() > MAX_LINES) {
//            mResponseTextView.scrollTo(0,
//                    (mResponseTextView.getLineCount() - MAX_LINES)
//                            * mResponseTextView.getLineHeight());
//        }
//    }

    public void createOnClick(){
        nameroome = (EditText)findViewById(R.id.Room_name);
        nameroome.getText().toString();
        UploadPictureViaWebservice();
//        try
//        {
//            UploadPictureViaWebservice();
//        }catch (Exception ex){
//            logMsg(ex.toString());
//        }

//        Intent intent = new Intent(CreateActivity.this, putcreate.class);
////        intent.putExtra("room",nameroome.toString());
//        startActivity(intent);
//        UploadViaWebservice(nameroome.toString() , user.username);
    }

//    public String UploadViaWebservice(String nameroom,String User) {
//
////        if (nameroom == "") {
////            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
////            nameroom = device_id + "_" + timeStamp + ".jpg";
////            fileNameR = nameroom;
////        }
//
//        String strResponse="";
////        String strname="";
////        String encodedImage = Base64.encodeToString(User, Base64.DEFAULT);
//
////		String URL =  "http://203.151.213.80/ServiceAforge/Wacservice.asmx";
////		String NAMESPACE = "http://tempuri.org/";
////		String METHOD_NAME = "_MainProcessStringImage";
////		String SOAP_ACTION = "http://tempuri.org/_MainProcessStringImage/";
//        String URL =  "http://10.0.0.36/webservice/WebService1.asmx?";
//        String NAMESPACE = "http://tempuri.org/";
//        String METHOD_NAME = "Register_Room";
//        String SOAP_ACTION = "http://tempuri.org/Register_Room";
//        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
//
//
////        strname = "1";
////        nameroom = "2";
////		/**** with parameter *****/
//        PropertyInfo pi;
//        pi=new PropertyInfo();
//        pi.setName("Customer_ID");
//        pi.setValue(User);
//        pi.setType(String.class);
//        request.addProperty(pi);
//
//        pi=new PropertyInfo();
//        pi.setName("Description");
//        pi.setValue(nameroom);
//        pi.setType(String.class);
//        request.addProperty(pi);
////		/*************************/
//
//        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
//        envelope.dotNet = true;
//        envelope.setOutputSoapObject(request);
//        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
//        androidHttpTransport.debug = true;
//        try
//        {
//            androidHttpTransport.call(SOAP_ACTION, envelope);
//            SoapObject response;
//            response= (SoapObject) envelope.bodyIn;
//            strResponse = response.getProperty(0).toString();
//
//
//        }
//        catch (Exception e)
//        {
//            //e.printStackTrace();
//            strResponse = e.toString();
//        }
//
//        return strResponse;
//
//    }


    public String UploadPictureViaWebservice() {

        int cnt = 0;
        String strResponse="";

        String URL =  "http://10.0.0.43/webservice/WebService1.asmx";
        String NAMESPACE = "http://tempuri.org/";
        String METHOD_NAME = "RegisterEnroll";
        String SOAP_ACTION = "http://tempuri.org/RegisterEnroll/";
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

        /**** with parameter *****/
        PropertyInfo pi;


        //CID
        String strCardID = str_CID;
        pi=new PropertyInfo();
        pi.setName("ID_Card");
        pi.setValue(strCardID);
        pi.setType(String.class);
        request.addProperty(pi);
        //ShowMsg(strCardID);

        //ID_Room
        String ID_Room = "123456";
        pi=new PropertyInfo();
        pi.setName("ID_Room");
        pi.setValue(ID_Room);
        pi.setType(String.class);
        request.addProperty(pi);
        //ShowMsg(ID_Room);

        //Name_Title_TH
        String Name_Title_TH = varTitleTH;
        pi=new PropertyInfo();
        pi.setName("Name_Title_TH");
        pi.setValue(Name_Title_TH);
        pi.setType(String.class);
        request.addProperty(pi);
        //ShowMsg(Name_Title_TH);

        //Name_Title_EN
        String Name_Title_EN = varTitleEN;
        pi=new PropertyInfo();
        pi.setName("Name_Title_EN");
        pi.setValue(Name_Title_EN);
        pi.setType(String.class);
        request.addProperty(pi);
        //ShowMsg(Name_Title_EN);

        //Name_TH
        String Name_TH = varFNTH;
        pi=new PropertyInfo();
        pi.setName("Name_TH");
        pi.setValue(Name_TH);
        pi.setType(String.class);
        request.addProperty(pi);
        //ShowMsg(Name_TH);

        //LastName_TH  ****
        String LastName_TH = varLNTH.replace(" ","").replace("\n","");
        String LastName_TH_tmp = ConvStringToHex(LastName_TH);
        LastName_TH_tmp = LastName_TH_tmp.substring(0, LastName_TH_tmp.indexOf("900"));
        cnt = LastName_TH_tmp.length()/3;
        LastName_TH = LastName_TH.substring(0,cnt);
        pi=new PropertyInfo();
        pi.setName("LastName_TH");
        pi.setValue(LastName_TH);
        pi.setType(String.class);
        request.addProperty(pi);
        //ShowMsg(LastName_TH);

        //Name_EN
        String Name_EN = varFNEN;
        pi=new PropertyInfo();
        pi.setName("Name_EN");
        pi.setValue(Name_EN);
        pi.setType(String.class);
        request.addProperty(pi);
        //ShowMsg(Name_EN);

        //LastName_EN ****
        String LastName_EN = varLNEN.replace(" ","").replace("\n","");
        String LastName_EN_tmp = ConvStringToHex(LastName_EN);
        LastName_EN_tmp = LastName_EN_tmp.substring(0, LastName_EN_tmp.indexOf("900"));
        cnt = LastName_EN_tmp.length() / 2;
        LastName_EN = LastName_EN.substring(0,cnt);
        pi=new PropertyInfo();
        pi.setName("LastName_EN");
        pi.setValue(LastName_EN);
        pi.setType(String.class);
        request.addProperty(pi);
        //ShowMsg(LastName_EN);


        if (varAddress==""){
            //
        }else{
            varAddress = varAddress.replace("#"," ");
            varAddress = varAddress.substring(0, varAddress.indexOf("@"));
            varAddress = varAddress.replaceAll("\\s+", " ");
            varAddress = varAddress.replace("\\0","").replace("\0","");
        }
        pi=new PropertyInfo();
        pi.setName("Address");
        pi.setValue(varAddress);
        pi.setType(String.class);
        request.addProperty(pi);
        //ShowMsg(varAddress);


        //Gender
        if (varGender=="ชาย"){
            varGender = "0";
        }else {
            varGender = "1";
        }
        pi=new PropertyInfo();
        pi.setName("Gender");
        pi.setValue(varGender);
        pi.setType(String.class);
        request.addProperty(pi);
        //ShowMsg(varGender);


        //Date_Of_Birth
        pi=new PropertyInfo();
        pi.setName("Date_Of_Birth");
        pi.setValue(str_DOB);
        pi.setType(String.class);
        request.addProperty(pi);
        //ShowMsg(str_DOB);

        //Date_Of_Issue
        pi=new PropertyInfo();
        pi.setName("Date_Of_Issue");
        pi.setValue(strIssue);
        pi.setType(String.class);
        request.addProperty(pi);
        //ShowMsg(strIssue);

        //Date_Of_Expiry
        pi=new PropertyInfo();
        pi.setName("Date_Of_Expiry");
        pi.setValue(strExpire);
        pi.setType(String.class);
        request.addProperty(pi);
        //ShowMsg(strExpire);


        //Imagebase64
        pi=new PropertyInfo();
        pi.setName("Imagebase64");
        pi.setValue(imgBase64String);
        pi.setType(String.class);
        request.addProperty(pi);
        //ShowMsg(imgBase64String);

        /*************************/

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        androidHttpTransport.debug = true;
        try
        {
            androidHttpTransport.call(SOAP_ACTION, envelope);
            SoapObject response;
            response= (SoapObject) envelope.bodyIn;
            strResponse = response.getProperty(0).toString();
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            strResponse = e.toString();
        }

        logMsg(strResponse);
        Toast.makeText(MainActivity.this,strResponse, Toast.LENGTH_SHORT).show();

        return strResponse;

    }
}