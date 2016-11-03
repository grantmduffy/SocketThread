package com.example.socketthread;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by opanga on 8/30/2016.
 */

public class SocketClient {

    private String mHost;
    private int mPort;
    private long mSendTimeout = 500; //ms

    private boolean mConnecting = false;
    private boolean mSending = false;

    private Socket mSocket;
    private Handler mMainHandler;
    private Handler mUIHandler;
    LooperThread mLooperThread;

    private Map<Character, String> mMessageQueue;

    private SocketEventListener mListener;

    public interface SocketEventListener {
        public void onConnected(boolean isConnected);
        public void onConnecting();
        public void onDisconnected();
        public void onSent(boolean didSend, String message);
        public void onSending();
    }

    class OnConnectedThread implements Runnable {
        @Override
        public void run(){
            println("On Connected");
            startSend();
            mListener.onConnected(isConnected());
        }
    }

    class OnConnectingThread implements Runnable {
        @Override
        public void run(){
            mListener.onConnecting();
        }
    }

    class OnSentThread implements Runnable {

        private boolean mDidSend;
        private String mMessage;

        public OnSentThread(boolean didSend, String message) {
            mDidSend = didSend;
            mMessage = message;
        }

        @Override
        public void run(){
            println("On Sent");
            mListener.onSent(mDidSend, mMessage);
            startSend();
        }
    }

    class OnSendingThread implements Runnable {
        @Override
        public void run(){
            mListener.onSending();
        }
    }

    public void setSocketEventListener(SocketEventListener listener){
        mListener = listener;
    }

    public SocketClient(){
        mLooperThread = new LooperThread();
        mLooperThread.start();
        mUIHandler = new Handler(Looper.getMainLooper());
        mMessageQueue = new HashMap<>();
    }

    public void connect(String host, int port){
        mHost = host;
        mPort = port;
        mMainHandler.post(new ConnectThread());
    }

    public void disconnect(){
        try {
            mSocket.close();
            mListener.onDisconnected();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void send(char cmd, String data){
        boolean run = mMessageQueue.isEmpty() && !mSending;
        mMessageQueue.put(cmd, data);
        if (run) startSend();
    }

    private void startSend(){
        println("Message Queue:");
        println(mMessageQueue.toString());
        if (mMessageQueue.isEmpty()) return;
        Set<Character> keys = mMessageQueue.keySet();
        Character command = keys.iterator().next();
        String data = "" + command + mMessageQueue.get(command);
        mMainHandler.post(new SendThread(data));
        mMessageQueue.remove(command);
    }

    private class ConnectThread implements Runnable{

        private long mStartTime;

        @Override
        public void run(){
            mConnecting = true;
            mStartTime = System.currentTimeMillis();
            mUIHandler.post(new OnConnectingThread());
            println("Connecting");
            while (System.currentTimeMillis() < mStartTime + mSendTimeout) {
                try {
                    InetAddress serverAddr = InetAddress.getByName(mHost);
                    mSocket = new Socket(serverAddr, mPort);
                    break;
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mConnecting = false;
            mUIHandler.post(new OnConnectedThread());
        }
    }

    private class SendThread implements Runnable{

        private String mMessage;
        private long mStartTime;

        public SendThread(String string){
            mMessage = string;
        }

        @Override
        public void run() {
            boolean didSend = false;
            mSending = true;
            mStartTime = System.currentTimeMillis();
            mUIHandler.post(new OnSendingThread());
            println("Sending " + mMessage);
            while (System.currentTimeMillis() < mStartTime + mSendTimeout) {
                try {
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                    out.println(mMessage);
                    out.flush();
                    String response = in.readLine();
                    println(response);
                    if (!response.equals(mMessage)) throw new Exception("'" + mMessage + "' wasn't recieved");
                    didSend  = true;
                    break;
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mSending = false;
            mUIHandler.post(new OnSentThread(didSend, mMessage));
        }
    }

    private class LooperThread extends Thread {
        @Override
        public void run(){
            Looper.prepare();
            mMainHandler = new Handler();
            Looper.loop();
        }
    }

    private void println(String str){
        print(str + "\n");
    }

    private void print(String str){
        Log.v("SocketThread", str);
    }

    public boolean isConnected(){
        if (mSocket == null) return false;
        return mSocket.isConnected();
    }

    public boolean isConnecting(){
        return mConnecting;
    }

    public boolean isSending(){
        return mSending;
    }

    public boolean isBusy(){
        return mConnecting || mSending || !mMessageQueue.isEmpty();
    }

    public void setTimeout(int timeout){
        mSendTimeout = timeout;
    }

}
