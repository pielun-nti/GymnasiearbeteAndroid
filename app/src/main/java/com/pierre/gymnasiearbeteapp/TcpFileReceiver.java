package com.pierre.gymnasiearbeteapp;

import android.os.Environment;

import java.io.*;
import java.net.Socket;
import java.util.Date;

import static com.pierre.gymnasiearbeteapp.MainActivity.activity;

public class TcpFileReceiver extends Thread {
    Socket socket;
    public TcpFileReceiver(Socket socket){
        this.socket = socket;
    }
    @Override
    public void run() {
        try {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = socket.getInputStream();
            } catch (IOException ex) {
                System.out.println("Can't get socket input stream. ");
            }

            try {
                out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/PsTech/tcpfile.wav");
            } catch (FileNotFoundException ex) {
                System.out.println("File not found. ");
            }

            byte[] bytes = new byte[Env.filesize];

            int count;
            while ((count = in.read(bytes)) > 0) {
                out.write(bytes, 0, count);
            }

            out.close();
            in.close();
            System.out.println("TCP Done received!" + new Date().toString());
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity.status.setText("Done receiving tcp " + new Date().toString());
                }
            });
            MainActivity.takeTimeAfterReceivedNow();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
