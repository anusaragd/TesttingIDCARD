package com.example.masters.testtingidcard;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends Activity {

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private static final String[] powerActionStrings = { "Power Down",
            "Cold Reset", "Warm Reset" };

    private static final String[] stateStrings = { "Unknown", "Absent",
            "Present", "Swallowed", "Powered", "Negotiable", "Specific" };

    private static final String[] featureStrings = { "FEATURE_UNKNOWN",
            "FEATURE_VERIFY_PIN_START", "FEATURE_VERIFY_PIN_FINISH",
            "FEATURE_MODIFY_PIN_START", "FEATURE_MODIFY_PIN_FINISH",
            "FEATURE_GET_KEY_PRESSED", "FEATURE_VERIFY_PIN_DIRECT",
            "FEATURE_MODIFY_PIN_DIRECT", "FEATURE_MCT_READER_DIRECT",
            "FEATURE_MCT_UNIVERSAL", "FEATURE_IFD_PIN_PROPERTIES",
            "FEATURE_ABORT", "FEATURE_SET_SPE_MESSAGE",
            "FEATURE_VERIFY_PIN_DIRECT_APP_ID",
            "FEATURE_MODIFY_PIN_DIRECT_APP_ID", "FEATURE_WRITE_DISPLAY",
            "FEATURE_GET_KEY", "FEATURE_IFD_DISPLAY_PROPERTIES",
            "FEATURE_GET_TLV_PROPERTIES", "FEATURE_CCID_ESC_COMMAND" };

    private static final String[] propertyStrings = { "Unknown", "wLcdLayout",
            "bEntryValidationCondition", "bTimeOut2", "wLcdMaxCharacters",
            "wLcdMaxLines", "bMinPINSize", "bMaxPINSize", "sFirmwareID",
            "bPPDUSupport", "dwMaxAPDUDataSize", "wIdVendor", "wIdProduct" };

    private static final int DIALOG_VERIFY_PIN_ID = 0;
    private static final int DIALOG_MODIFY_PIN_ID = 1;
    private static final int DIALOG_READ_KEY_ID = 2;
    private static final int DIALOG_DISPLAY_LCD_MESSAGE_ID = 3;

    private UsbManager mManager;
    private Reader mReader;
    private PendingIntent mPermissionIntent;

    private static final int MAX_LINES = 25;
    private TextView mResponseTextView;
    private ArrayAdapter<String> mReaderAdapter;


    private Button mOpenButton;
    private Button mCloseButton;
    private Button mGetCardInfo;
    private TextView textViewReader;

    private Button mSave;

    private TextView txtIDCard;
    private TextView txtTitleTH;
    private TextView txtFirstNameTH;
    private TextView txtLastNameTH;
    private TextView txtTitleEN;
    private TextView txtFirstNameEN;
    private TextView txtLastNameEN;
    private TextView txtGender;
    private TextView txtDOB;
    private TextView txtIssue;
    private TextView txtExpire;
    private TextView txtAddress;

    private String str_CID = null;
    private String imgBase64String = "";
    private String str_DOB = null;
    private String strIssue=null;
    private String strExpire=null;
    private Bitmap bitmapCard=null;

    private String varTitleTH="";
    private String varFNTH="";
    private String varLNTH="";
    private String varTitleEN="";
    private String varFNEN="";
    private String varLNEN="";
    private String varGender="";
    private String varAddress="";


    private void logMsg(String msg){

        DateFormat dateFormat = new SimpleDateFormat("[dd-MM-yyyy HH:mm:ss]: ");
        Date date = new Date();
        String oldMsg = mResponseTextView.getText().toString();

        mResponseTextView
                .setText(oldMsg + "\n" + dateFormat.format(date) + msg);

        if (mResponseTextView.getLineCount() > MAX_LINES) {
            mResponseTextView.scrollTo(0,
                    (mResponseTextView.getLineCount() - MAX_LINES)
                            * mResponseTextView.getLineHeight());
        }
    }
    private void logBuffer(byte[] buffer, int bufferLength) {

        String bufferString = "";

        for (int i = 0; i < bufferLength; i++) {

            String hexChar = Integer.toHexString(buffer[i] & 0xFF);
            if (hexChar.length() == 1) {
                hexChar = "0" + hexChar;
            }

            if (i % 16 == 0) {

                if (bufferString != "") {

                    logMsg(bufferString);
                    bufferString = "";
                }
            }

            bufferString += hexChar.toUpperCase() + " ";
        }

        if (bufferString != "") {
            logMsg(bufferString);
        }
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (ACTION_USB_PERMISSION.equals(action)) {

                synchronized (this) {

                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                        if (device != null) {

                            // Open reader
                            logMsg("Opening reader: " + device.getDeviceName()
                                    + "...");
                            new OpenTask().execute(device);
                        }

                    } else {

                        logMsg("Permission denied for device "
                                + device.getDeviceName());

                        // Enable open button
                        mOpenButton.setEnabled(true);
                    }
                }

            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {

                synchronized (this) {

                    // Update reader list
                    mReaderAdapter.clear();
                    for (UsbDevice device : mManager.getDeviceList().values()) {
                        if (mReader.isSupported(device)) {
                            mReaderAdapter.add(device.getDeviceName());
                        }
                    }

                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (device != null && device.equals(mReader.getDevice())) {

                        // Disable buttons
                        mCloseButton.setEnabled(false);

                        // Close reader
                        logMsg("Closing reader...");
                        new CloseTask().execute();
                    }
                }
            }
        }
    };

    private class OpenTask extends AsyncTask<UsbDevice, Void, Exception> {
        @Override
        protected Exception doInBackground(UsbDevice... params) {
            Exception result = null;
            try {
                mReader.open(params[0]);
            } catch (Exception e) {
                result = e;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Exception result) {

            if (result != null) {

                logMsg(result.toString());

            } else {

                logMsg("Reader name: " + mReader.getReaderName());

                int numSlots = mReader.getNumSlots();
                logMsg("Number of slots: " + numSlots);
                // Enable buttons
                mCloseButton.setEnabled(true);
            }
        }
    }

    private class CloseTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            mReader.close();
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            mOpenButton.setEnabled(true);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Get USB manager
        mManager = (UsbManager) getSystemService(Context.USB_SERVICE);


        mReader = new Reader(mManager);
        mReader.setOnStateChangeListener(new Reader.OnStateChangeListener() {

            @Override
            public void onStateChange(int slotNum, int prevState, int currState) {

                if (prevState < Reader.CARD_UNKNOWN
                        || prevState > Reader.CARD_SPECIFIC) {
                    prevState = Reader.CARD_UNKNOWN;
                }

                if (currState < Reader.CARD_UNKNOWN
                        || currState > Reader.CARD_SPECIFIC) {
                    currState = Reader.CARD_UNKNOWN;
                }

                // Create output string
                final String outputString = "Slot " + slotNum + ": "
                        + stateStrings[prevState] + " -> "
                        + stateStrings[currState];

                // Show output
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        logMsg(outputString);
                    }
                });
            }
        });

        // Register receiver for USB permission
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
                ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mReceiver, filter);

        // Initialize textViewReader
        textViewReader = (TextView) findViewById(R.id.textViewReader);

        // Initialize reader spinner
        mReaderAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item);
        for (UsbDevice device : mManager.getDeviceList().values()) {
            if (mReader.isSupported(device)) {
                mReaderAdapter.add(device.getDeviceName());
                textViewReader.setText(device.getDeviceName());
            }
        }


        // Initialize response text view
        mResponseTextView = (TextView) findViewById(R.id.main_text_view_response);
        mResponseTextView.setMovementMethod(new ScrollingMovementMethod());
        mResponseTextView.setMaxLines(MAX_LINES);
        mResponseTextView.setText("");

        mOpenButton = (Button) findViewById(R.id.main_button_open);
        mOpenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean requested = false;

                // Disable open button
                mOpenButton.setEnabled(false);

                String deviceName = (String) textViewReader.getText();

                if (deviceName != null) {

                    // For each device
                    for (UsbDevice device : mManager.getDeviceList().values()) {

                        // If device name is found
                        if (deviceName.equals(device.getDeviceName())) {

                            // Request permission
                            mManager.requestPermission(device,
                                    mPermissionIntent);

                            requested = true;
                            break;
                        }
                    }
                }

                if (!requested) {

                    // Enable open button
                    mOpenButton.setEnabled(true);
                }
            }
        });

        mCloseButton = (Button) findViewById(R.id.main_button_close);
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Close reader
                logMsg("Closing reader...");
                new CloseTask().execute();
            }
        });

        mGetCardInfo = (Button) findViewById(R.id.main_button_read);
        mGetCardInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    initalizeCard();
                } catch (ReaderException e) {
                    e.printStackTrace();
                }
            }
        });

        //###### Initial TextView ######//
        txtIDCard = (TextView) findViewById(R.id.textViewID);
        txtTitleTH = (TextView) findViewById(R.id.textViewTitleTH);
        txtFirstNameTH = (TextView) findViewById(R.id.textViewFirstNameTH);
        txtLastNameTH = (TextView) findViewById(R.id.textViewLastNameTH);
        txtTitleEN = (TextView) findViewById(R.id.textViewTitleEN);
        txtFirstNameEN = (TextView) findViewById(R.id.textViewFirstNameEN);
        txtLastNameEN = (TextView) findViewById(R.id.textViewLastNameEN);
        txtGender = (TextView) findViewById(R.id.textViewGender);
        txtDOB = (TextView) findViewById(R.id.textViewDOB);
        txtIssue = (TextView) findViewById(R.id.textViewISSUE);
        txtExpire = (TextView) findViewById(R.id.textViewEXPIRE);
        txtAddress = (TextView) findViewById(R.id.textViewAddress);
        txtAddress.setMovementMethod(new ScrollingMovementMethod());
        txtAddress.setMaxLines(2);
        txtAddress.setText("");

        mSave = (Button) findViewById(R.id.mSave);
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    UploadPictureViaWebservice();
                }catch (Exception ex){
                    logMsg(ex.toString());
                }

            }
        });

    }



    private void initalizeCard() throws ReaderException {

        mReader.power(0, Reader.CARD_WARM_RESET);
        mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);

        // Get ATR
        logMsg("Slot " + "0" + ": Getting ATR...");
        byte[] atr = mReader.getAtr(0);
        // Show ATR
        if (atr != null) {
            logMsg("ATR:");
            logBuffer(atr, atr.length);

        } else {
            logMsg("ATR: None");
        }
        // -------------

////        SELECT Command
////         See GlobalPlatform Card Specification (e.g. 2.2, section 11.9)
////         CLA: 00
////         INS: A4
////         P1: 04 i.e. b3 is set to 1, means select by name
////         P2: 00 i.e. first or only occurence
////         Lc: 08 i.e. length of AID see below
////         Data: A0 00 00 00 54 4B 00 01
////         AID of the card manager
        byte[] recvBuffer;

        // SELECT Command
        byte[] sendBuffer={
                (byte) 0x00, (byte) 0xA4, (byte) 0x04,
                (byte) 0x00, (byte) 0x08, (byte) 0xA0,
                (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x54, (byte) 0x48, (byte) 0x00,
                (byte) 0x01
        };
        recvBuffer=new byte[300];
        mReader.transmit(0, sendBuffer, sendBuffer.length, recvBuffer, recvBuffer.length);
        //logMsg(ByteToString_ETC(recvBuffer));
        // ---


        String[] arrayInfo = null;
        byte[] COMMAND;
//      //# CID
        COMMAND = new byte[] {
                (byte) 0x80, (byte) 0xb0, (byte) 0x00,
                (byte) 0x04, (byte) 0x02, (byte) 0x00,
                (byte) 0x0d
        };
        recvBuffer=new byte[300];
        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
        COMMAND = new byte[] {
                (byte) 0x00, (byte) 0xc0, (byte) 0x00, (byte) 0x00, (byte) 0x0d
        };
        recvBuffer=new byte[300];
        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
        try {
            str_CID = new String(recvBuffer, "TIS620");
            //logMsg("ID " + str_CID);
            str_CID = str_CID.substring(0,13);
            txtIDCard.setText("ID : " + str_CID);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        //# FULL NAME TH
        COMMAND = new byte[] {
                (byte) 0x80, (byte) 0xB0, (byte) 0x00, (byte) 0x11, (byte) 0x02, (byte) 0x00, (byte) 0x64
        };
        recvBuffer=new byte[300];
        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
        //logMsg("+" + bytesToHex(recvBuffer));
        COMMAND = new byte[] {
                (byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0x64
        };
        recvBuffer=new byte[300];  //300
        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
        //logMsg("++" + bytesToHex(recvBuffer));
        String fn_th = null;
        try {
            fn_th = new String(recvBuffer, "TIS620");
            fn_th = fn_th.replace("\n","");
            //logMsg("TH FN " + fn_th);
            arrayInfo = fn_th.split("\\#", -1);
            txtTitleTH.setText(arrayInfo[0]);
            txtFirstNameTH.setText(arrayInfo[1]);
            txtLastNameTH.setText(arrayInfo[2]+""+arrayInfo[3].replace("\n",""));
            varTitleTH = arrayInfo[0];
            varFNTH=arrayInfo[1];
            varLNTH=arrayInfo[2]+""+arrayInfo[3];
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        //# FULL NAME EN
        COMMAND = new byte[] {
                (byte) 0x80, (byte) 0xB0, (byte) 0x00, (byte) 0x75, (byte) 0x02, (byte) 0x00, (byte) 0x64
        };
        recvBuffer=new byte[300];
        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
        COMMAND = new byte[] {
                (byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0x64
        };
        recvBuffer=new byte[300];
        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
        String fn_en = null;
        try {
            fn_en = new String(recvBuffer, "TIS620");
            fn_en = fn_en.replace("\n","");
            //logMsg("TH EN " + fn_en);
            arrayInfo = fn_en.split("\\#", -1);
            txtTitleEN.setText(arrayInfo[0]);
            txtFirstNameEN.setText(arrayInfo[1]);
            txtLastNameEN.setText(arrayInfo[2]+ "" +arrayInfo[3].replace("\n",""));
            varTitleEN = arrayInfo[0];
            varFNEN=arrayInfo[1];
            varLNEN=arrayInfo[2]+ "" +arrayInfo[3];
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //# DOB
        COMMAND = new byte[] {
                (byte) 0x80, (byte) 0xB0, (byte) 0x00, (byte) 0xD9, (byte) 0x02, (byte) 0x00, (byte) 0x08
        };
        recvBuffer=new byte[300];
        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
        COMMAND = new byte[] {
                (byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0x08
        };
        recvBuffer=new byte[300];
        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
        try {
            str_DOB = new String(recvBuffer, "TIS620");
            //logMsg("DOB " + str_DOB);
            str_DOB = str_DOB.substring(0,8);
            txtDOB.setText("เกิดวันที่ : " + str_DOB.substring(6,8) + "/" + str_DOB.substring(4,6) + "/" + str_DOB.substring(0,4));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        //Gender
        COMMAND = new byte[] {
                (byte) 0x80, (byte) 0xB0, (byte) 0x00, (byte) 0xE1, (byte) 0x02, (byte) 0x00, (byte) 0x01
        };
        recvBuffer=new byte[300];
        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
        COMMAND = new byte[] {
                (byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0x01
        };
        recvBuffer=new byte[300];
        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
        String str_Gender = null;
        try {
            str_Gender = new String(recvBuffer, "TIS620");
            boolean  b = str_Gender.startsWith("1");
            if (b==true){
                str_Gender = "ชาย";
            }else {
                str_Gender = "หญิง";
            }
            varGender = str_Gender;
            //logMsg("Gender " + str_Gender);
            txtGender.setText("เพศ : " + str_Gender);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //# Address
        COMMAND = new byte[]{
                (byte) 0x80, (byte) 0xB0, (byte) 0x15,
                (byte) 0x79, (byte) 0x02, (byte) 0x00,
                (byte) 0x64
        };
        recvBuffer=new byte[300];
        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
        COMMAND = new byte[] {
                (byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0x64
        };
        recvBuffer=new byte[300];
        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
        String strAddress = null;
        try {
            strAddress = new String(recvBuffer, "TIS620");
            strAddress = strAddress.replace("#"," ");
            txtAddress.setText("ที่อยู่ : " + strAddress);
            varAddress = new String(recvBuffer, "TIS620");   // no replace #
            varAddress = varAddress + "@";
        } catch (UnsupportedEncodingException e) {
            varAddress="";
            e.printStackTrace();
        }


        //# ISSUE/ EXPIRE  25580703 25661103 01
        COMMAND = new byte[]{
                (byte) 0x80, (byte) 0xB0, (byte) 0x01,
                (byte) 0x67, (byte) 0x02, (byte) 0x00,
                (byte) 0x12
        };
        recvBuffer=new byte[300];
        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
        COMMAND = new byte[] {
                (byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0x12
        };
        recvBuffer=new byte[300];
        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
        String str_ISSUEEXPIRE = null;
        try {
            str_ISSUEEXPIRE = new String(recvBuffer, "TIS620");
            //logMsg("ISSUE/ EXPIRE " + str_ISSUEEXPIRE);
            strIssue = str_ISSUEEXPIRE.substring(0,8);
            txtIssue.setText("วันออกบัตร : " + strIssue.substring(6,8) + "/" + strIssue.substring(4,6) + "/" + strIssue.substring(0,4));
            strExpire = str_ISSUEEXPIRE.substring(8,16);
            txtExpire.setText("วันบัตรหมดอายุ : " + strExpire.substring(6,8) + "/" + strExpire.substring(4,6) + "/" + strExpire.substring(0,4));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        //# ISSUE USER
        COMMAND = new byte[]{
                (byte) 0x80, (byte) 0xB0, (byte) 0x00,
                (byte) 0xF6, (byte) 0x02, (byte) 0x00,
                (byte) 0x64
        };
        recvBuffer=new byte[300];
        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
        COMMAND = new byte[] {
                (byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0x64
        };
        recvBuffer=new byte[300];
        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
        String strISSUER = null;
        try {
            strISSUER = new String(recvBuffer, "TIS620");
            //logMsg("ISSUER " + strISSUER);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }



        //# ImageNumber
        COMMAND = new byte[]{
                (byte) 0x80, (byte) 0xB0, (byte) 0x16,
                (byte) 0x19, (byte) 0x02, (byte) 0x00,
                (byte) 0x0E
        };
        recvBuffer=new byte[300];
        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
        COMMAND = new byte[] {
                (byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0x0E
        };
        recvBuffer=new byte[300];
        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
        String str_ImageNumber = null;
        try {
            str_ImageNumber = new String(recvBuffer, "TIS620");
            //logMsg("ImageNumber " + str_ImageNumber);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }



        //# Photo
        try
        {
            String hexstring = "";
            String tmp = "";
            for (int r=0;r<21;r++) {
                COMMAND = new byte[]
                        {(byte) 0x80, (byte) 0xB0, (byte) ((byte) 0x01+r), (byte) ((byte) 0x7B-r), (byte) 0x02, (byte) 0x00, (byte) 0xFF};
                recvBuffer=new byte[2]; //254
                mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
                //logMsg(bytesToHex(recvBuffer)); // response 61FF; 61 = success, FF = GET RESPONSE size
                COMMAND = new byte[] {
                        (byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0xFF
                };
                recvBuffer=new byte[260];  //260
                mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);

                tmp = bytesToHex(recvBuffer);
                tmp = tmp.substring(0,tmp.length()-10);
                hexstring = hexstring + tmp;
            }

            byte[] byteRawHex = hexStringToByteArray(hexstring);
            imgBase64String = Base64.encodeToString(byteRawHex,Base64.NO_WRAP);
            bitmapCard = BitmapFactory.decodeByteArray(byteRawHex, 0, byteRawHex.length);

            ImageView imgPhoto = (ImageView)findViewById(R.id.idcard);
            imgPhoto.setImageBitmap(bitmapCard);

            if (bitmapCard == null) {
                imgBase64String = "";
                try {
                    SavePhoto(hexstring,str_CID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            logMsg("DONE.");
        }catch (Exception ex){
            logMsg(ex.getMessage());
        }


    }

    private void ShowMsg(String msg){
        Toast.makeText(MainActivity.this,msg,Toast.LENGTH_LONG).show();
    }
    private void SavePhoto(String hexStr, String idcard) throws IOException {
        idcard = idcard.substring(0,13);
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/IDCard");
        myDir.mkdirs();
        String filename = idcard + ".jpg";
        File file = new File (myDir, filename);

        FileOutputStream outimg = new FileOutputStream(file);
        for (int i = 0; i < hexStr.length(); i += 2)
        {
            int byteimg = Character.digit(hexStr.charAt(i), 16) * 16 + Character.digit(hexStr.charAt(i + 1), 16);
            outimg.write(byteimg);
        }
        outimg.close();
    }
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    private void writeToFile(String data, String idcard) {
        idcard = idcard.substring(0,13);
        // Get the directory for the user's public pictures directory.
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/IDCard");
        String filename = idcard + ".txt.jpg";
        File file = new File (myDir, filename);
        try
        {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);
            myOutWriter.close();
            fOut.flush();
            fOut.close();
        }
        catch (IOException e)
        {
            //Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    private static String ConvStringToHex(String base)
    {
        StringBuffer buffer = new StringBuffer();
        int intValue;
        for(int x = 0; x < base.length(); x++)
        {
            int cursor = 0;
            intValue = base.charAt(x);
            String binaryChar = new String(Integer.toBinaryString(base.charAt(x)));
            for(int i = 0; i < binaryChar.length(); i++)
            {
                if(binaryChar.charAt(i) == '1')
                {
                    cursor += 1;
                }
            }
            if((cursor % 2) > 0)
            {
                intValue += 128;
            }
            buffer.append(Integer.toHexString(intValue) + "");
        }
        return buffer.toString();
    }

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


//public class MainActivity extends Activity {
//    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
//
//    private static final String[] powerActionStrings = { "Power Down",
//            "Cold Reset", "Warm Reset" };
//
//    private static final String[] stateStrings = { "Unknown", "Absent",
//            "Present", "Swallowed", "Powered", "Negotiable", "Specific" };
//
//    private static final String[] featureStrings = { "FEATURE_UNKNOWN",
//            "FEATURE_VERIFY_PIN_START", "FEATURE_VERIFY_PIN_FINISH",
//            "FEATURE_MODIFY_PIN_START", "FEATURE_MODIFY_PIN_FINISH",
//            "FEATURE_GET_KEY_PRESSED", "FEATURE_VERIFY_PIN_DIRECT",
//            "FEATURE_MODIFY_PIN_DIRECT", "FEATURE_MCT_READER_DIRECT",
//            "FEATURE_MCT_UNIVERSAL", "FEATURE_IFD_PIN_PROPERTIES",
//            "FEATURE_ABORT", "FEATURE_SET_SPE_MESSAGE",
//            "FEATURE_VERIFY_PIN_DIRECT_APP_ID",
//            "FEATURE_MODIFY_PIN_DIRECT_APP_ID", "FEATURE_WRITE_DISPLAY",
//            "FEATURE_GET_KEY", "FEATURE_IFD_DISPLAY_PROPERTIES",
//            "FEATURE_GET_TLV_PROPERTIES", "FEATURE_CCID_ESC_COMMAND" };
//
//    private static final String[] propertyStrings = { "Unknown", "wLcdLayout",
//            "bEntryValidationCondition", "bTimeOut2", "wLcdMaxCharacters",
//            "wLcdMaxLines", "bMinPINSize", "bMaxPINSize", "sFirmwareID",
//            "bPPDUSupport", "dwMaxAPDUDataSize", "wIdVendor", "wIdProduct" };
//
//    private static final int DIALOG_VERIFY_PIN_ID = 0;
//    private static final int DIALOG_MODIFY_PIN_ID = 1;
//    private static final int DIALOG_READ_KEY_ID = 2;
//    private static final int DIALOG_DISPLAY_LCD_MESSAGE_ID = 3;
//
//    private UsbManager mManager;
//    private Reader mReader;
//    private PendingIntent mPermissionIntent;
//
//    private static final int MAX_LINES = 25;
//    private TextView mResponseTextView;
//    private ArrayAdapter<String> mReaderAdapter;
//
//
//    private Button mOpenButton;
//    private Button mCloseButton;
//    private Button mGetCardInfo;
//    private TextView textViewReader;
//
//    private TextView txtIDCard;
//    private TextView txtTitleTH;
//    private TextView txtFirstNameTH;
//    private TextView txtLastNameTH;
//    private TextView txtTitleEN;
//    private TextView txtFirstNameEN;
//    private TextView txtLastNameEN;
//    private TextView txtGender;
//    private TextView txtDOB;
//    private TextView txtIssue;
//    private TextView txtExpire;
//    private TextView txtAddress;
//
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
//    private void logBuffer(byte[] buffer, int bufferLength) {
//
//        String bufferString = "";
//
//        for (int i = 0; i < bufferLength; i++) {
//
//            String hexChar = Integer.toHexString(buffer[i] & 0xFF);
//            if (hexChar.length() == 1) {
//                hexChar = "0" + hexChar;
//            }
//
//            if (i % 16 == 0) {
//
//                if (bufferString != "") {
//
//                    logMsg(bufferString);
//                    bufferString = "";
//                }
//            }
//
//            bufferString += hexChar.toUpperCase() + " ";
//        }
//
//        if (bufferString != "") {
//            logMsg(bufferString);
//        }
//    }
//
//
//    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
//
//        public void onReceive(Context context, Intent intent) {
//
//            String action = intent.getAction();
//
//            if (ACTION_USB_PERMISSION.equals(action)) {
//
//                synchronized (this) {
//
//                    UsbDevice device = (UsbDevice) intent
//                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);
//
//                    if (intent.getBooleanExtra(
//                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
//
//                        if (device != null) {
//
//                            // Open reader
//                            logMsg("Opening reader: " + device.getDeviceName()
//                                    + "...");
//                            new MainActivity.OpenTask().execute(device);
//                        }
//
//                    } else {
//
//                        logMsg("Permission denied for device "
//                                + device.getDeviceName());
//
//                        // Enable open button
//                        mOpenButton.setEnabled(true);
//                    }
//                }
//
//            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
//
//                synchronized (this) {
//
//                    // Update reader list
//                    mReaderAdapter.clear();
//                    for (UsbDevice device : mManager.getDeviceList().values()) {
//                        if (mReader.isSupported(device)) {
//                            mReaderAdapter.add(device.getDeviceName());
//                        }
//                    }
//
//                    UsbDevice device = (UsbDevice) intent
//                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);
//
//                    if (device != null && device.equals(mReader.getDevice())) {
//
//                        // Disable buttons
//                        mCloseButton.setEnabled(false);
//
//                        // Close reader
//                        logMsg("Closing reader...");
//                        new MainActivity.CloseTask().execute();
//                    }
//                }
//            }
//        }
//    };
//
//    private class OpenTask extends AsyncTask<UsbDevice, Void, Exception> {
//        @Override
//        protected Exception doInBackground(UsbDevice... params) {
//            Exception result = null;
//            try {
//                mReader.open(params[0]);
//            } catch (Exception e) {
//                result = e;
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(Exception result) {
//
//            if (result != null) {
//
//                logMsg(result.toString());
//
//            } else {
//
//                logMsg("Reader name: " + mReader.getReaderName());
//
//                int numSlots = mReader.getNumSlots();
//                logMsg("Number of slots: " + numSlots);
//                // Enable buttons
//                mCloseButton.setEnabled(true);
//            }
//        }
//    }
//
//    private class CloseTask extends AsyncTask<Void, Void, Void> {
//        @Override
//        protected Void doInBackground(Void... params) {
//            mReader.close();
//            return null;
//        }
//        @Override
//        protected void onPostExecute(Void result) {
//            mOpenButton.setEnabled(true);
//        }
//    }
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        // Get USB manager
//        mManager = (UsbManager) getSystemService(Context.USB_SERVICE);
//
//
//        mReader = new Reader(mManager);
//        mReader.setOnStateChangeListener(new Reader.OnStateChangeListener() {
//
//            @Override
//            public void onStateChange(int slotNum, int prevState, int currState) {
//
//                if (prevState < Reader.CARD_UNKNOWN
//                        || prevState > Reader.CARD_SPECIFIC) {
//                    prevState = Reader.CARD_UNKNOWN;
//                }
//
//                if (currState < Reader.CARD_UNKNOWN
//                        || currState > Reader.CARD_SPECIFIC) {
//                    currState = Reader.CARD_UNKNOWN;
//                }
//
//                // Create output string
//                final String outputString = "Slot " + slotNum + ": "
//                        + stateStrings[prevState] + " -> "
//                        + stateStrings[currState];
//
//                // Show output
//                runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        logMsg(outputString);
//                    }
//                });
//            }
//        });
//
//        // Register receiver for USB permission
//        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
//                ACTION_USB_PERMISSION), 0);
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(ACTION_USB_PERMISSION);
//        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
//        registerReceiver(mReceiver, filter);
//
//        // Initialize textViewReader
//        textViewReader = (TextView) findViewById(R.id.textViewReader);
//
//        // Initialize reader spinner
//        mReaderAdapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_spinner_item);
//        for (UsbDevice device : mManager.getDeviceList().values()) {
//            if (mReader.isSupported(device)) {
//                mReaderAdapter.add(device.getDeviceName());
//                textViewReader.setText(device.getDeviceName());
//            }
//        }
//
//
//        // Initialize response text view
//        mResponseTextView = (TextView) findViewById(R.id.main_text_view_response);
//        mResponseTextView.setMovementMethod(new ScrollingMovementMethod());
//        mResponseTextView.setMaxLines(MAX_LINES);
//        mResponseTextView.setText("");
//
//        mOpenButton = (Button) findViewById(R.id.main_button_open);
//        mOpenButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                boolean requested = false;
//
//                // Disable open button
//                mOpenButton.setEnabled(false);
//
//                String deviceName = (String) textViewReader.getText();
//
//                if (deviceName != null) {
//
//                    // For each device
//                    for (UsbDevice device : mManager.getDeviceList().values()) {
//
//                        // If device name is found
//                        if (deviceName.equals(device.getDeviceName())) {
//
//                            // Request permission
//                            mManager.requestPermission(device,
//                                    mPermissionIntent);
//
//                            requested = true;
//                            break;
//                        }
//                    }
//                }
//
//                if (!requested) {
//
//                    // Enable open button
//                    mOpenButton.setEnabled(true);
//                }
//            }
//        });
//
//        mCloseButton = (Button) findViewById(R.id.main_button_close);
//        mCloseButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                // Close reader
//                logMsg("Closing reader...");
//                new MainActivity.CloseTask().execute();
//            }
//        });
//
//        mGetCardInfo = (Button) findViewById(R.id.main_button_read);
//        mGetCardInfo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    initalizeCard();
//                } catch (ReaderException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        //###### Initial TextView ######//
//        txtIDCard = (TextView) findViewById(R.id.textViewID);
//        txtTitleTH = (TextView) findViewById(R.id.textViewTitleTH);
//        txtFirstNameTH = (TextView) findViewById(R.id.textViewFirstNameTH);
//        txtLastNameTH = (TextView) findViewById(R.id.textViewLastNameTH);
//        txtTitleEN = (TextView) findViewById(R.id.textViewTitleEN);
//        txtFirstNameEN = (TextView) findViewById(R.id.textViewFirstNameEN);
//        txtLastNameEN = (TextView) findViewById(R.id.textViewLastNameEN);
//        txtGender = (TextView) findViewById(R.id.textViewGender);
//        txtDOB = (TextView) findViewById(R.id.textViewDOB);
//        txtIssue = (TextView) findViewById(R.id.textViewISSUE);
//        txtExpire = (TextView) findViewById(R.id.textViewEXPIRE);
//        txtAddress = (TextView) findViewById(R.id.textViewAddress);
//        txtAddress.setMovementMethod(new ScrollingMovementMethod());
//        txtAddress.setMaxLines(2);
//        txtAddress.setText("");
//
//    }
//
//
//
//    private void initalizeCard() throws ReaderException {
//
//        mReader.power(0, Reader.CARD_WARM_RESET);
//        mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
//
//        // Get ATR
//        logMsg("Slot " + "0" + ": Getting ATR...");
//        byte[] atr = mReader.getAtr(0);
//        // Show ATR
//        if (atr != null) {
//            logMsg("ATR:");
//            logBuffer(atr, atr.length);
//
//        } else {
//            logMsg("ATR: None");
//        }
//        // -------------
//
//////        SELECT Command
//////         See GlobalPlatform Card Specification (e.g. 2.2, section 11.9)
//////         CLA: 00
//////         INS: A4
//////         P1: 04 i.e. b3 is set to 1, means select by name
//////         P2: 00 i.e. first or only occurence
//////         Lc: 08 i.e. length of AID see below
//////         Data: A0 00 00 00 54 4B 00 01
//////         AID of the card manager
//        byte[] recvBuffer;
//
//        // SELECT Command
//        byte[] sendBuffer={
//                (byte) 0x00, (byte) 0xA4, (byte) 0x04,
//                (byte) 0x00, (byte) 0x08, (byte) 0xA0,
//                (byte) 0x00, (byte) 0x00, (byte) 0x00,
//                (byte) 0x54, (byte) 0x48, (byte) 0x00,
//                (byte) 0x01
//        };
//        recvBuffer=new byte[300];
//        mReader.transmit(0, sendBuffer, sendBuffer.length, recvBuffer, recvBuffer.length);
//        //logMsg(ByteToString_ETC(recvBuffer));
//        // ---
//
//
//        String[] arrayInfo = null;
//        byte[] COMMAND;
////      //# CID
//        COMMAND = new byte[] {
//                (byte) 0x80, (byte) 0xb0, (byte) 0x00,
//                (byte) 0x04, (byte) 0x02, (byte) 0x00,
//                (byte) 0x0d
//        };
//        recvBuffer=new byte[300];
//        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
//        COMMAND = new byte[] {
//                (byte) 0x00, (byte) 0xc0, (byte) 0x00, (byte) 0x00, (byte) 0x0d
//        };
//        recvBuffer=new byte[300];
//        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
//        String str_CID = null;
//        try {
//            str_CID = new String(recvBuffer, "TIS620");
//            //logMsg("ID " + str_CID);
//            txtIDCard.setText(str_CID.substring(0,13));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//
//        //# FULL NAME TH
//        COMMAND = new byte[] {
//                (byte) 0x80, (byte) 0xB0, (byte) 0x00, (byte) 0x11, (byte) 0x02, (byte) 0x00, (byte) 0x64
//        };
//        recvBuffer=new byte[300];
//        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
//        COMMAND = new byte[] {
//                (byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0x64
//        };
//        recvBuffer=new byte[300];
//        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
//        String fn_th = null;
//        try {
//            fn_th = new String(recvBuffer, "TIS620");
//            fn_th = fn_th.replace("##","#");
//            //logMsg("TH FN " + fn_th);
//            arrayInfo = fn_th.split("\\#", -1);
//            txtTitleTH.setText(arrayInfo[0]);
//            txtFirstNameTH.setText(arrayInfo[1]);
//            txtLastNameTH.setText(arrayInfo[2]);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//
//        //# FULL NAME EN
//        COMMAND = new byte[] {
//                (byte) 0x80, (byte) 0xB0, (byte) 0x00, (byte) 0x75, (byte) 0x02, (byte) 0x00, (byte) 0x64
//        };
//        recvBuffer=new byte[300];
//        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
//        COMMAND = new byte[] {
//                (byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0x64
//        };
//        recvBuffer=new byte[300];
//        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
//        String fn_en = null;
//        try {
//            fn_en = new String(recvBuffer, "TIS620");
//            fn_en = fn_en.replace("##","#");
//            //logMsg("TH EN " + fn_en);
//            arrayInfo = fn_en.split("\\#", -1);
//            txtTitleEN.setText(arrayInfo[0]);
//            txtFirstNameEN.setText(arrayInfo[1]);
//            txtLastNameEN.setText(arrayInfo[2]);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//        //# DOB
//        COMMAND = new byte[] {
//                (byte) 0x80, (byte) 0xB0, (byte) 0x00, (byte) 0xD9, (byte) 0x02, (byte) 0x00, (byte) 0x08
//        };
//        recvBuffer=new byte[300];
//        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
//        COMMAND = new byte[] {
//                (byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0x08
//        };
//        recvBuffer=new byte[300];
//        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
//        String str_DOB = null;
//        try {
//            str_DOB = new String(recvBuffer, "TIS620");
//            //logMsg("DOB " + str_DOB);
//            str_DOB = str_DOB.substring(0,8);
//            txtDOB.setText(str_DOB.substring(6,8) + "/" + str_DOB.substring(4,6) + "/" + str_DOB.substring(0,4));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//
//        //Gender
//        COMMAND = new byte[] {
//                (byte) 0x80, (byte) 0xB0, (byte) 0x00, (byte) 0xE1, (byte) 0x02, (byte) 0x00, (byte) 0x01
//        };
//        recvBuffer=new byte[300];
//        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
//        COMMAND = new byte[] {
//                (byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0x01
//        };
//        recvBuffer=new byte[300];
//        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
//        String str_Gender = null;
//        try {
//            str_Gender = new String(recvBuffer, "TIS620");
//
//            boolean  b = str_Gender.startsWith("1");
//            if (b==true){
//                str_Gender = "ชาย";
//            }else {
//                str_Gender = "หญิง";
//            }
//            //logMsg("Gender " + str_Gender);
//            txtGender.setText(str_Gender);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//        //# Address
//        COMMAND = new byte[]{
//                (byte) 0x80, (byte) 0xB0, (byte) 0x15,
//                (byte) 0x79, (byte) 0x02, (byte) 0x00,
//                (byte) 0x64
//        };
//        recvBuffer=new byte[300];
//        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
//        COMMAND = new byte[] {
//                (byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0x64
//        };
//        recvBuffer=new byte[300];
//        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
//        String strAddress = null;
//        try {
//            strAddress = new String(recvBuffer, "TIS620");
//            strAddress = strAddress.replace("#"," ");
//            //logMsg("Address " + strAddress);
//            txtAddress.setText(strAddress);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//
//        //# ISSUE/ EXPIRE  25580703 25661103 01
//        COMMAND = new byte[]{
//                (byte) 0x80, (byte) 0xB0, (byte) 0x01,
//                (byte) 0x67, (byte) 0x02, (byte) 0x00,
//                (byte) 0x12
//        };
//        recvBuffer=new byte[300];
//        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
//        COMMAND = new byte[] {
//                (byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0x12
//        };
//        recvBuffer=new byte[300];
//        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
//        String str_ISSUEEXPIRE = null;
//        try {
//            str_ISSUEEXPIRE = new String(recvBuffer, "TIS620");
//            //logMsg("ISSUE/ EXPIRE " + str_ISSUEEXPIRE);
//            String strIssue = str_ISSUEEXPIRE.substring(0,8);
//            txtIssue.setText(strIssue.substring(6,8) + "/" + strIssue.substring(4,6) + "/" + strIssue.substring(0,4));
//            String strExpire = str_ISSUEEXPIRE.substring(8,16);
//            txtExpire.setText(strExpire.substring(6,8) + "/" + strExpire.substring(4,6) + "/" + strExpire.substring(0,4));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//
//        //# ISSUE USER
//        COMMAND = new byte[]{
//                (byte) 0x80, (byte) 0xB0, (byte) 0x00,
//                (byte) 0xF6, (byte) 0x02, (byte) 0x00,
//                (byte) 0x64
//        };
//        recvBuffer=new byte[300];
//        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
//        COMMAND = new byte[] {
//                (byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0x64
//        };
//        recvBuffer=new byte[300];
//        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
//        String strISSUER = null;
//        try {
//            strISSUER = new String(recvBuffer, "TIS620");
//            //logMsg("ISSUER " + strISSUER);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//
//
//        //# ImageNumber
//        COMMAND = new byte[]{
//                (byte) 0x80, (byte) 0xB0, (byte) 0x16,
//                (byte) 0x19, (byte) 0x02, (byte) 0x00,
//                (byte) 0x0E
//        };
//        recvBuffer=new byte[300];
//        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
//        COMMAND = new byte[] {
//                (byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0x0E
//        };
//        recvBuffer=new byte[300];
//        mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
//        String str_ImageNumber = null;
//        try {
//            str_ImageNumber = new String(recvBuffer, "TIS620");
//            //logMsg("ImageNumber " + str_ImageNumber);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//
//
////      //# Photo
//        String hexstring = "";
//        String tmp = "";
//        for (int r=0;r<20;r++) {
//            COMMAND = new byte[]
//                    {(byte) 0x80, (byte) 0xB0, (byte) ((byte) 0x01+r), (byte) ((byte) 0x7B-r), (byte) 0x02, (byte) 0x00, (byte) 0xFF};
//            recvBuffer=new byte[2]; //254
//            mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
//            //logMsg(bytesToHex(recvBuffer)); // response 61FF; 61 = success, FF = GET RESPONSE size
//            COMMAND = new byte[] {
//                    (byte) 0x00, (byte) 0xC0, (byte) 0x00, (byte) 0x00, (byte) 0xFF
//            };
//            recvBuffer=new byte[260];  //260
//            mReader.transmit(0, COMMAND, COMMAND.length, recvBuffer, recvBuffer.length);
//
//            tmp = bytesToHex(recvBuffer);
//            tmp = tmp.substring(0,tmp.length()-10);
//            hexstring = hexstring + tmp;
//        }
//
//        byte[] byteRawHex = hexStringToByteArray(hexstring);
//        String imgBase64String = Base64.encodeToString(byteRawHex,Base64.NO_WRAP);
//        Bitmap bitmap = BitmapFactory.decodeByteArray(byteRawHex, 0, byteRawHex.length);
//
//        ImageView imgPhoto = (ImageView)findViewById(R.id.idcard);
//        imgPhoto.setImageBitmap(bitmap);
//
//        if (bitmap == null) {
//            try {
//                SavePhoto(hexstring,str_CID);
//                writeToFile(imgBase64String,str_CID);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        logMsg("DONE.");
//    }
//
//    private void SavePhoto(String hexStr, String idcard) throws IOException {
//        idcard = idcard.substring(0,13);
//        String root = Environment.getExternalStorageDirectory().toString();
//        File myDir = new File(root + "/IDCard");
//        myDir.mkdirs();
//        String filename = idcard + ".jpg";
//        File file = new File (myDir, filename);
//
//        FileOutputStream outimg = new FileOutputStream(file);
//        for (int i = 0; i < hexStr.length(); i += 2)
//        {
//            int byteimg = Character.digit(hexStr.charAt(i), 16) * 16 + Character.digit(hexStr.charAt(i + 1), 16);
//            outimg.write(byteimg);
//        }
//        outimg.close();
//    }
//    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
//    public static String bytesToHex(byte[] bytes) {
//        char[] hexChars = new char[bytes.length * 2];
//        for ( int j = 0; j < bytes.length; j++ ) {
//            int v = bytes[j] & 0xFF;
//            hexChars[j * 2] = hexArray[v >>> 4];
//            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
//        }
//        return new String(hexChars);
//    }
//    public static byte[] hexStringToByteArray(String s) {
//        int len = s.length();
//        byte[] data = new byte[len / 2];
//        for (int i = 0; i < len; i += 2) {
//            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
//                    + Character.digit(s.charAt(i+1), 16));
//        }
//        return data;
//    }
//    private void writeToFile(String data, String idcard) {
//        idcard = idcard.substring(0,13);
//        // Get the directory for the user's public pictures directory.
//        String root = Environment.getExternalStorageDirectory().toString();
//        File myDir = new File(root + "/IDCard");
//        String filename = idcard + ".txt.jpg";
//        File file = new File (myDir, filename);
//        try
//        {
//            file.createNewFile();
//            FileOutputStream fOut = new FileOutputStream(file);
//            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
//            myOutWriter.append(data);
//            myOutWriter.close();
//            fOut.flush();
//            fOut.close();
//        }
//        catch (IOException e)
//        {
//            //Log.e("Exception", "File write failed: " + e.toString());
//        }
//    }
//
//
//}