package com.example.masters.testtingidcard;

import android.app.Activity;
import android.os.Bundle;
import android.util.Base64;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by MASTERS on 19/12/2560.
 */

public class putcreate extends Activity {

    String deviceName = "";
    String device_id= "";

    String fileNameR="";
    byte[] fileContentsR = null;
    String nameroom;
    String User = user.username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_result);

        UploadViaWebservice(nameroom, User);
    }



    public String UploadViaWebservice(String nameroom,String User) {

//        if (nameroom == "") {
//            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//            nameroom = device_id + "_" + timeStamp + ".jpg";
//            fileNameR = nameroom;
//        }

        String strResponse="";
//        String strname="";
//        String encodedImage = Base64.encodeToString(User, Base64.DEFAULT);

//		String URL =  "http://203.151.213.80/ServiceAforge/Wacservice.asmx";
//		String NAMESPACE = "http://tempuri.org/";
//		String METHOD_NAME = "_MainProcessStringImage";
//		String SOAP_ACTION = "http://tempuri.org/_MainProcessStringImage/";
        String URL =  "http://10.0.0.36/webservice/WebService1.asmx?";
        String NAMESPACE = "http://tempuri.org/";
        String METHOD_NAME = "Register_Room";
        String SOAP_ACTION = "http://tempuri.org/Register_Room";
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);


//        strname = "1";
//        nameroom = "2";
//		/**** with parameter *****/
        PropertyInfo pi;
        pi=new PropertyInfo();
        pi.setName("Customer_ID");
        pi.setValue(User);
        pi.setType(String.class);
        request.addProperty(pi);

        pi=new PropertyInfo();
        pi.setName("Description");
        pi.setValue(nameroom);
        pi.setType(String.class);
        request.addProperty(pi);
//		/*************************/

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

        return strResponse;

    }
}
