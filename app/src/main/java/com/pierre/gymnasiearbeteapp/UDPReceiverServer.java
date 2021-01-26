package com.pierre.gymnasiearbeteapp;

import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Date;

import static com.pierre.gymnasiearbeteapp.MainActivity.activity;

public class UDPReceiverServer extends Thread {

    DatagramSocket serverSocket;

    @Override
    public void run(){
        try {
            serverSocket = new DatagramSocket(Env.receiverport);
            System.out.println("Started UDP Server");
            FileOutputStream out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/PsTech/udpfile.wav");
            byte[] recData = new byte[Env.filesize];
            while (true) {
                DatagramPacket recPacket = new DatagramPacket(recData, recData.length);
                serverSocket.receive(recPacket);
                System.out.println("\n Packet length: " + recPacket.getLength());
                out.write(recPacket.getData(), 0, recPacket.getLength());
                System.out.println("\nPacket written to file");
                out.flush();
                //out.close();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.status.setText("Done receiving udp " + new Date().toString());
                    }
                });
                MainActivity.takeTimeAfterReceivedNow();
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
