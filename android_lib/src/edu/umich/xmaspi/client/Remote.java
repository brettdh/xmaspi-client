package edu.umich.xmaspi.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import android.util.Log;

public class Remote {
    private static final String DEFAULT_NAME = "threepio";
    private static final String DEFAULT_HOST = "141.212.110.237";
    private static final short DEFAULT_PORT = 4908;
    private static final String START_COMMAND = "let's go\n";
    private static final String START_RESPONSE = "Go Time!";
    private static final String TAG = "xmaspi.client.Remote";
    
    private Socket sock;
    private PrintWriter socketWriter;
    private BufferedReader socketReader;
    
    public Remote() throws IOException {
        this(DEFAULT_NAME);
    }
    
    public Remote(String name) throws IOException {
        this(name, DEFAULT_HOST, DEFAULT_PORT);
    }
    
    public Remote(String name, String host) throws IOException {
        this(name, host, DEFAULT_PORT);
    }
    
    public Remote(String name, String host, short port) throws IOException {
        sock = new Socket(host, port);
        
        socketReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        socketWriter = new PrintWriter(sock.getOutputStream());
        socketWriter.print(name + "\n");
        
        waitToStart();
    }
    
    private void waitToStart() throws IOException {
        socketWriter.print(START_COMMAND);
        socketWriter.flush();
        String line = "";
        while (line != null && !line.startsWith(START_RESPONSE)) {
            line = socketReader.readLine();
            if (line != null) {
                Log.d(TAG, line);
            } else {
                Log.d(TAG, "Error: expected a line, but socket was closed");
            }
        }
    }
    
    public void writeLed(int led_id, int brightness, int red, int green, int blue) throws IOException {
        OutputStream out = sock.getOutputStream();
        byte[] cmd = new byte[] { 
            (byte) led_id, (byte) brightness, 
            (byte) red, (byte) green, (byte) blue
        };
        out.write(cmd);
        out.flush();
        //Log.d("Remote", String.format("wrote led %d bright %d rgb %d %d %d", 
        //                              led_id, brightness, red, green, blue));
    }
    
    public void busyWait() throws IOException {
        busyWait(1000);
    }
    
    public void busyWait(double durationSeconds) throws IOException {
        int durationMillis = (int) Math.floor(durationSeconds * 1000);
        while (durationMillis > 0) {
            writeLed(100, 0, 0, 0, 0); // NOP
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                return;
            }
            durationMillis -= 500;
        }
    }
    
    public void done() throws IOException {
        sock.close();
    }
}
