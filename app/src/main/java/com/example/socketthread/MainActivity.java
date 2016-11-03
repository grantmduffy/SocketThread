package com.example.socketthread;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    EditText mHostView;
    EditText mPortView;
    EditText mMessageView;
    Button mConnectView;
    Button mSendView;
    TextView mStatusView;
    SeekBar mRedView;
    SeekBar mGreenView;
    SeekBar mBlueView;
    View mColorView;

    SocketClient mClient;
    int mRed = 255;
    int mGreen = 255;
    int mBlue = 255;

    SocketClient.SocketEventListener mSocketListener = new SocketClient.SocketEventListener() {
        @Override
        public void onConnected(boolean successful) {
            if (successful) {
                mConnectView.setText("Disconnect");
                mConnectView.setClickable(true);
                mStatusView.setText("Connected");
                mSendView.setClickable(true);
            } else {
                mConnectView.setClickable(true);
                mConnectView.setText("Connect");
                mStatusView.setText("Failed to Connect");
            }
        }

        @Override
        public void onConnecting() {
            mConnectView.setText("Connecting...");
            mConnectView.setClickable(false);
        }

        @Override
        public void onDisconnected() {
            mConnectView.setText("Connect");
            mConnectView.setClickable(true);
        }

        @Override
        public void onSent(boolean successful, String message) {
            mSendView.setClickable(true);
            mSendView.setText("Send");
            if (successful) {
                mStatusView.setText(message + " Sent");
            } else {
                mStatusView.setText(message + " Failed to send");
            }
        }

        @Override
        public void onSending() {
            mSendView.setClickable(false);
            mSendView.setText("Sending...");
        }
    };

    SeekBar.OnSeekBarChangeListener mRedListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mRed = progress;
            updateColor();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    SeekBar.OnSeekBarChangeListener mGreenListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mGreen = progress;
            updateColor();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    SeekBar.OnSeekBarChangeListener mBlueListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mBlue = progress;
            updateColor();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHostView = (EditText) findViewById(R.id.editTextHost);
        mPortView = (EditText) findViewById(R.id.editTextPort);
        mMessageView = (EditText) findViewById(R.id.editTextMessage);
        mConnectView = (Button) findViewById(R.id.buttonConnect);
        mSendView = (Button) findViewById(R.id.buttonSend);
        mStatusView = (TextView) findViewById(R.id.textViewStatus);
        mRedView = (SeekBar) findViewById(R.id.redSeekBar);
        mGreenView = (SeekBar) findViewById(R.id.greenSeekBar);
        mBlueView = (SeekBar) findViewById(R.id.blueSeekBar);
        mColorView = findViewById(R.id.colorView);

        mClient = new SocketClient();
        mClient.setSocketEventListener(mSocketListener);
        mRedView.setOnSeekBarChangeListener(mRedListener);
        mGreenView.setOnSeekBarChangeListener(mGreenListener);
        mBlueView.setOnSeekBarChangeListener(mBlueListener);
        mSendView.setClickable(false);
        mColorView.setBackgroundColor(Color.argb(255, mRed, mGreen, mBlue));
    }

    public void connectClicked(View view){
        if (mClient.isConnected()){
            mClient.disconnect();
        } else {
            String host = mHostView.getText().toString();
            int port = Integer.parseInt(mPortView.getText().toString());
            mClient.connect(host, port);
        }
    }

    public void onSend(View view){
        String message = mMessageView.getText().toString();
        mClient.send('m', message);
    }

    private void updateColor(){
        mColorView.setBackgroundColor(Color.argb(255, mRed, mGreen, mBlue));
        mClient.send('c', "" + (char) mRed + (char) mGreen + (char) mBlue);
    }
}
