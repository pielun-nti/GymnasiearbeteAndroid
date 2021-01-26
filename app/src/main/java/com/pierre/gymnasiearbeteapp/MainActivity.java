package com.pierre.gymnasiearbeteapp;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Date;

import static com.pierre.gymnasiearbeteapp.Env.host;
import static com.pierre.gymnasiearbeteapp.Env.port;
import static com.pierre.gymnasiearbeteapp.Env.receiverport;

public class MainActivity extends AppCompatActivity {
    Context context;
    Button tcp, udp;
    public static TextView status;
    public static Activity activity;
    static long timeStarted;
    static double timeEnded;
    String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GetAllPermissions();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build(); //This will allow Network operations on main thread (app might be slow for users bcs of this)
        StrictMode.setThreadPolicy(policy);
        context = getApplicationContext();
        activity = this;
        tcp = (Button) findViewById(R.id.btnUploadTCP);
        udp = (Button) findViewById(R.id.btnUploadUDP);
        status = (TextView) findViewById(R.id.txtStatus);
        tcp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startReceivingTCPServer();
                uploadTCP();
            }
        });
        udp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startReceivingUDPServer();
                uploadUDP();
            }
        });
    }

    void startReceivingUDPServer(){
        UDPReceiverServer udpReceiverServer = new UDPReceiverServer();
        udpReceiverServer.start();
    }

    void startReceivingTCPServer(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean stop = false;
                    while (!stop) {
                        Socket socket = new Socket(host, receiverport);
                        TcpFileReceiver tcpFileReceiver = new TcpFileReceiver(socket);
                        tcpFileReceiver.start();
                        if (socket.isConnected()){
                            stop = true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    void sendResponse(final String message,final Socket s) {
        new Thread(new Runnable() {
            @Override
            public void run() {

        try {
            PrintWriter writer = null;
            writer = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), "UTF-8"), true);
            writer.println(message);
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
            }
        }).start();
    }

    void takeTimeBeforeSendNow(){
        timeStarted = System.currentTimeMillis();
    }

    public static void takeTimeAfterReceivedNow(){
        timeEnded = (double) System.currentTimeMillis()-timeStarted;
        timeEnded /= 2;
        if (timeEnded < 0){
            timeEnded *= -1;
        }
        Log.e("MainA", "Total time taken: " + timeEnded + " ms");
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, "Total time taken: " + timeEnded + " ms", Toast.LENGTH_LONG).show();
                status.setText("Total time taken: " + timeEnded + " ms");
            }
        });
    }

    private void uploadTCP(){
        new Thread(new Runnable() {
            @Override
            public void run() {
    try {
    //take time now
        takeTimeBeforeSendNow();
    Socket socket = null;
    File file = new File(Environment.getExternalStorageDirectory() + "/PsTech/mic/gymnasiearbete.wav");
    long length = file.length();
    System.out.println("file send length: " + length);
    socket = new Socket(host, port);


    byte[] bytes = new byte[(int) length];
    InputStream in = new FileInputStream(file);
    OutputStream out = socket.getOutputStream();

    int count;
    while ((count = in.read(bytes)) > 0) {
        out.write(bytes, 0, count);
    }

    out.close();
    in.close();
    socket.close();
    //long time = System.nanoTime();
    System.out.println("Done sending tcp " + new Date().toString());
    activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
            status.setText("Done sending tcp " + new Date().toString());
        }
    });
    } catch (Exception ex) {
    ex.printStackTrace();
    final String ex2 = ex.toString();
    activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
            status.setText("TCP Error: " + ex2);
        }
    });

    }
            }
        }).start();
    }


    private void uploadUDP(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    takeTimeBeforeSendNow();
                    File myFile = new File(Environment.getExternalStorageDirectory() + "/PsTech/mic/gymnasiearbete.wav");
                    long length = myFile.length();
                    System.out.println("file send  length: " + length);
                    DatagramSocket ds = null;
                    BufferedInputStream bis = null;
                        ds = new DatagramSocket();
                        DatagramPacket dp;
                        int packetsize = 1024;
                        double nosofpackets;
                        nosofpackets = Math.ceil(length / packetsize);

                        bis = new BufferedInputStream(new FileInputStream(myFile));
                        for (double i = 0; i < nosofpackets + 1; i++) {
                            byte[] mybytearray = new byte[packetsize];
                            bis.read(mybytearray, 0, mybytearray.length);
                            //System.out.println("Packet Sent:" + (i + 1));
                            dp = new DatagramPacket(mybytearray, mybytearray.length, InetAddress.getByName(host), port);
                            ds.send(dp);
                        }
                    System.out.println("Done sending udp " + new Date().toString());
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            status.setText("Done sending udp " + new Date().toString());
                        }
                    });
                }
                    catch (Exception ex) {

                        ex.printStackTrace();
                        final String ex2 = ex.toString();
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                status.setText("UDP Error: " + ex2);
                            }
                        });                    }
            }
        }).start();
    }

    private void GetAllPermissions() //Request all required permissions
    {
        if (Build.VERSION.SDK_INT < 23) return; //check if api level is right

        if (!PermissionCheck()) //If no access to location then request all permissions
        {
            int getPermission = 1;
            this.requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, //GPS Location (accurate)
                    Manifest.permission.ACCESS_COARSE_LOCATION, //Network, CellData, Wifi location (inaccurate)
                    Manifest.permission.READ_CONTACTS, //Get contacts and contacts data
                    Manifest.permission.WRITE_CONTACTS, //Add contacts to the list
                    Manifest.permission.READ_CALL_LOG, //Read the call log
                    Manifest.permission.READ_SMS, //Read SMS Messages
                    Manifest.permission.SEND_SMS, //Send SMS Messages
                    Manifest.permission.READ_CALENDAR, //List calendar events
                    Manifest.permission.WRITE_CALENDAR, //Add, modify, delete calendar events
                    Manifest.permission.CAMERA, //Camera photo, record, tap
                    Manifest.permission.RECORD_AUDIO, //Camera record, Mic record, tap
                    Manifest.permission.READ_EXTERNAL_STORAGE, //To check files
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, //To save recordings
                    Manifest.permission.RECEIVE_BOOT_COMPLETED, //To restart NetworkHandler service automatically on reboot
                    Manifest.permission.READ_PHONE_STATE, //To get IMEI and SIM Serial
                    Manifest.permission.RECEIVE_SMS, //To receive SMS
                    Manifest.permission.ANSWER_PHONE_CALLS, //answer phone calls
                    Manifest.permission.PROCESS_OUTGOING_CALLS, // receive, process outgoing phone calls such as recording them
                    Manifest.permission.BROADCAST_SMS, //not sure
                    Manifest.permission.BROADCAST_STICKY, //
                    Manifest.permission.VIBRATE, // Vibrate phone
                    Manifest.permission.WRITE_CALL_LOG, //write call log
                    Manifest.permission.BLUETOOTH, //
                    Manifest.permission.BLUETOOTH_ADMIN, //
                    Manifest.permission.CALL_PHONE //To call phone
            }, getPermission);
        }
    }

    private boolean PermissionCheck() //Check if required permissions are granted
    {
        if (Build.VERSION.SDK_INT < 23) return false;
        boolean gpsPermission =
                this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        boolean contactsPermission =
                this.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
                        this.checkSelfPermission(Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
                        this.checkSelfPermission(Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED &&
                        this.checkSelfPermission(Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_GRANTED &&
                        this.checkSelfPermission(Manifest.permission.BROADCAST_STICKY) == PackageManager.PERMISSION_GRANTED &&
                        this.checkSelfPermission(Manifest.permission.BROADCAST_SMS) == PackageManager.PERMISSION_GRANTED &&
                        this.checkSelfPermission(Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED &&
                        this.checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                        this.checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED;

        boolean callLogPermission =
                this.checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED &&
                        this.checkSelfPermission(Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED && this.checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;

        boolean smsPermission =
                this.checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
                        this.checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
                        this.checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;

        boolean calendarPermission =
                this.checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
                        this.checkSelfPermission(Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED;

        boolean mediaPermission =
                this.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                        this.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        boolean devicePermission =
                this.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                        this.checkSelfPermission(Manifest.permission.RECEIVE_BOOT_COMPLETED) == PackageManager.PERMISSION_GRANTED;

        boolean fileSystemPermission =
                this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        this.checkSelfPermission(Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED &&
                        this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        return contactsPermission && gpsPermission && callLogPermission && smsPermission &&
                calendarPermission && mediaPermission && fileSystemPermission && devicePermission;
    }
}
