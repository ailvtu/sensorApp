package com.inbot.module.padbotSensor;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import com.inbot.module.padbotsdk.PadBotSdk;
import com.inbot.module.padbotSensor.listener.BluetoothListener;

import java.util.Timer;
import java.util.TimerTask;

public class MessengerService extends Service {
    /** Command to the service to display a message */
    static final String tag = "PaBotAPP";
    static final int MSG_CONTROL = 1;
    static final int MSG_SPEED = 0;
    private final static String SEARCH_TYPE_SPEED = "SPEED";
    private String searchType = null;
    static String speed = "Speed";
    Messenger clientMessenger=null;
    private BluetoothListener bluetoothListener;
    private BluetoothDevice connectDevice;

    boolean isRunTimer = false;
    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

                if(msg.what==MSG_CONTROL) {
                    if (isRunTimer == false){
                        clientMessenger = msg.replyTo;       //remember the msg.replyTo
                        timer.schedule(task, 1000, 1000); //start Timer get Speed every 1s
                        isRunTimer = true;
                    }

                    if (msg.arg1 == 1)
                        PadBotSdk.goForward();
                    else if (msg.arg1 == 2)
                        PadBotSdk.turnLeft();
                    else if (msg.arg1 == 3)
                        PadBotSdk.turnRight();
                    else if (msg.arg1 == 4)
                        PadBotSdk.goBackward();
                    else if (msg.arg1 == 5)
                        PadBotSdk.stop();
                    else if(msg.arg1==6)
                        PadBotSdk.goBackwardLeft(msg.arg2);
                    else if(msg.arg1==7)
                        PadBotSdk.goBackwardRight(msg.arg2);
                    else if (msg.arg1 ==8 )
                        PadBotSdk.goForwardLeft(msg.arg2);
                    else if(msg.arg1 == 9)
                        PadBotSdk.goForwardRight(msg.arg2);
                }
                if(msg.what==MSG_SPEED) {

                    connectDevice =  (BluetoothDevice)msg.obj;
                    connect();
                }
                    super.handleMessage(msg);
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        Log.i(tag,"binding");
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothListener = new BluetoothListener() {  //return the speed
            @Override
            public void setupTextView(String value) {
//                    if(value.length())
                Log.i(tag,value);
                Log.i(tag,"speed length="+value.length());
              /*
                String[] strarray1=value.split(":");
                String[] strarray2=strarray1[1].split(",");
                float leftV = Float.parseFloat(strarray2[0]);
                float rightR = Float.parseFloat(strarray2[1]);
                */
                speed = value;

            }
        };

    }
    private  void sendSpeed(String speedValue) {

        if(clientMessenger!=null) {
            Message msg2 = Message.obtain(null, MSG_CONTROL, 0, 0);
            long Time = System.currentTimeMillis();
            String T = String.valueOf(Time);
            Bundle bundle = new Bundle();
            bundle.putString("speed", speedValue+","+T);
            msg2.setData(bundle);   //send msg by bundle,General type data must be passed by Bundle
            try {
                clientMessenger.send(msg2);
                Log.i(tag,"send success "+speedValue+","+T);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }

    private  void connect() {
        BluetoothService.getInstance().connect(connectDevice.getAddress(), bluetoothListener);
        Toast.makeText(getApplicationContext(), "Connect success", Toast.LENGTH_SHORT).show();
    }
   /*
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
        @Override
        public void run() {   //Timer start
            PadBotSdk.getSpeed();
//            if(speed.substring(3).equals("Vel"))
             sendMessage(speed);
             Log.i("PaBotAPP",speed);
        }
    };*/

    Handler handlerTimer = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0x88) {
                searchType = SEARCH_TYPE_SPEED;
                PadBotSdk.getSpeed();
                sendSpeed(speed);
                Log.i("PaBotAPP",speed);
            }
            super.handleMessage(msg);
        };
    };
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            Message messageTimer = new Message();
            messageTimer.what = 0x88;
            handlerTimer.sendMessage(messageTimer);
        }
    };
}
