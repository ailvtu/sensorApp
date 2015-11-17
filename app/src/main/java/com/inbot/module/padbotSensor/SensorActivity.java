package com.inbot.module.padbotSensor;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.inbot.module.padbotSensor.R;
import com.inbot.module.padbotsdk.PadBotSdk;

public class SensorActivity extends Activity {

    private static String TAG = "PadbotAPP";
    TextView Geoma = null;
    TextView Compass = null;
    EditText Path =null;

    Button Start = null;
    Button Stop =null;

    String Paths = null;

    private SensorManager padbotSensorManager;
    private Sensor mSensor;
    private Sensor aSensor;
    private SensorEventListener padbotSensorListener;

    float[] accelerometerValues=new float[3];
    float[] magneticFieldValues=new float[3];
    float[] values=new float[3];
    float[] MR=new float[9];

    private static final String[] selectSpeed = {"low","middle","hight"};
    private static final String[] selectFrequency = {"200000us","60000us","20000us","0us"};
    private Spinner spinner,spinner2;
    private ArrayAdapter<String>adapter,adapter2;

    writeData Writer = new writeData();
    int frequency;
    int speedflag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        Geoma = (TextView)findViewById(R.id.geoma);
        Compass = (TextView)findViewById(R.id.compass);

        Path = (EditText)findViewById(R.id.paths);

        Start = (Button)findViewById(R.id.startbutton);
        Stop = (Button)findViewById(R.id.stopbutton);

        spinner = (Spinner)findViewById(R.id.spinner);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,selectSpeed);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner2 = (Spinner)findViewById(R.id.spinner2);
        adapter2 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,selectFrequency);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);


        spinner.setOnItemSelectedListener(new SpinnerSelectSpeedListener());
        spinner.setVisibility(View.VISIBLE);

        spinner2.setOnItemSelectedListener(new SpinnerSelectFrequencyListener());
        spinner2.setVisibility(View.VISIBLE);

        Writer.CreateFiles();

        padbotSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if(event.sensor.getType()== Sensor.TYPE_MAGNETIC_FIELD){
                    magneticFieldValues=event.values;
                    double Gvalue = Math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2]);
                    Geoma.setText("Geomagnetism:" + Gvalue);
                    String Gvaluetostr = Double.toString(Gvalue);
                    Writer.WriteData(Gvaluetostr,1);
                    Log.i(TAG, "onSensorChanged Gvaluetostr:" + Gvaluetostr);
                    Writer.WriteData(Double.toString(values[0]), 2);
                    Log.i(TAG, "onSensorChanged Cvaluetostr:"+values[0]);

                }
                if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                    accelerometerValues = event.values;


                }
                SensorManager.getRotationMatrix(MR,null,accelerometerValues,magneticFieldValues);
                SensorManager.getOrientation(MR, values);
                values[0] = (float)Math.toDegrees(values[0]);
                //经过SensorManager.getOrientation(R, values);得到的values值为弧度
                //转换为角度
                Compass.setText("Orientation:" + values[0]);

                //values[0]  ：azimuth 方向角，但用（磁场+加速度）得到的数据范围是（-180～180）,
                //0表示正北，90表示正东，180/-180表示正南，-90表示正西

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                padbotSensorManager.registerListener(padbotSensorListener,mSensor,frequency);
                padbotSensorManager.registerListener(padbotSensorListener, aSensor, frequency);
                PadBotSdk.goBackward();
                Log.i(TAG, "onStartClick register Sucess ");
                String Time = Writer.GetTime();
                Paths = Path.getText().toString();

                Writer.CreateFile(Paths + Time +" "+ selectSpeed[speedflag]+selectFrequency[Math.abs(frequency-3)]+".txt");

                Toast.makeText(SensorActivity.this, "Start Collecting", Toast.LENGTH_LONG).show();
            }
        });
        Stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PadBotSdk.stop();
                padbotSensorManager.unregisterListener(padbotSensorListener);
                Log.i(TAG, "onStopClick unregister sucess ");
                Toast.makeText(SensorActivity.this,"Stop Collecting",Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        padbotSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mSensor = padbotSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        aSensor = padbotSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        super.onResume();
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sensor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class SpinnerSelectSpeedListener implements android.widget.AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            switch (arg2){
                case 0:PadBotSdk.setSpeedLevelOne();
                       speedflag = 0;
                       Log.i(TAG, "onItemSelected :" + arg2);
                       break;
                case 1:PadBotSdk.setSpeedLevelTwo();
                       speedflag = 1;
                       Log.i(TAG, "onItemSelected :" + arg2);
                       break;
                case 2:PadBotSdk.setSpeedLevelThree();
                       speedflag = 2;
                       Log.i(TAG, "onItemSelected :" + arg2);
                       break;
                default:PadBotSdk.setSpeedLevelOne();

            }
            Toast.makeText(SensorActivity.this, "The Speed :" + selectSpeed[arg2], Toast.LENGTH_SHORT).show();

        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }

    }


    private class SpinnerSelectFrequencyListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            switch (arg2){
                case 0:frequency = padbotSensorManager.SENSOR_DELAY_NORMAL;
                    Log.i(TAG, "FrequencySelect :" + frequency);
                    break;
                case 1:frequency = padbotSensorManager.SENSOR_DELAY_UI;
                    Log.i(TAG, "FrequencySelect :" + frequency);
                    break;
                case 2:frequency = padbotSensorManager.SENSOR_DELAY_GAME;
                    Log.i(TAG, "FrequencySelect :" + frequency);
                    break;
                case 3:frequency = padbotSensorManager.SENSOR_DELAY_FASTEST;
                    Log.i(TAG, "FrequencySelect :" + frequency);
                    break;
                default:frequency = padbotSensorManager.SENSOR_DELAY_NORMAL;
            }
            Toast.makeText(SensorActivity.this, "The Frequency :" + frequency, Toast.LENGTH_SHORT).show();
        }

        public void onNothingSelected(AdapterView<?> arg0) {

        }
    }
}
