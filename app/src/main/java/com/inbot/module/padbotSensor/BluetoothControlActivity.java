package com.inbot.module.padbotSensor;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.inbot.module.padbotsdk.PadBotSdk;
import com.inbot.module.padbotSensor.listener.BluetoothListener;

public class BluetoothControlActivity extends Activity {
	
	private final static int ONE = 1;
	private final static int TWO = 2;
	private final static int TREE = 3;
	private final static int FOUR = 4;

	static final int MSG_SPEED = 0;


	private final static String SEARCH_TYPE_INFRA = "INFRA";
	private final static String SEARCH_TYPE_VOLTAGE = "VOLEAGE";
	private final static String SEARCH_TYPE_VERSION = "VERSION";
	private final static String SEARCH_TYPE_SPEED = "SPEED";
	
	private TextView nameTextView;
	private TextView addressTextView;
	
	private Button connectButton;
	private Button disconnectButton;
	
	private Button stopButton;
	private Button frowardButton;
	private Button backButton;
	private Button leftButton;
	private Button rightButton;
	
	private Button frowardLeftOneButton;
	private Button frowardLeftTwoButton;
	private Button frowardLeftThreeButton;
	private Button frowardLeftFourButton;
	
	private Button frowardRightOneButton;
	private Button frowardRightTwoButton;
	private Button frowardRightThreeButton;
	private Button frowardRightFourButton;
	
	private Button backLeftOneButton;
	private Button backLeftTwoButton;
	private Button backLeftThreeButton;
	private Button backLeftFourButton;
	
	private Button backRightOneButton;
	private Button backRightTwoButton;
	private Button backRightThreeButton;
	private Button backRightFourButton;
	
	private Button upHeaderButton;
	private Button bottomHeaderButton;
	
	private Button openInfraButton;
	private Button closeInfraButton;
	
	private Button speedOneButton;
	private Button speedTwoButton;
	private Button speedThreeButton;
	
	private Button searchInfraButton;
	private Button searchVoltageButton;
	private Button searchRobotVersionButton;
	private Button searchRobotSpeedButton;
	
	private TextView infraValueTextView;
	private TextView voltageValueTextView;
	private TextView versionValueTextView;
	private TextView speedValueTextView;
	
	private Button startAutochargeButton;
	private Button stopAutochargeButton;
	
	private Button back;
	
	private BluetoothListener bluetoothListener;
	private BluetoothDevice connectDevice;
	
	private Handler setupViewHandler;
	private String searchType;
	
	private String infraString;
	private String voltageString;
	private String versionString;
	private String speedString;
	private boolean isRun;

//	private Button test=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_bluetooth_control);
		
		if (null == setupViewHandler) {
			setupViewHandler = new Handler();
		}
		
		isRun = false;
		
		connectDevice = (BluetoothDevice) getIntent().getParcelableExtra("connectDevice");
		
		nameTextView = (TextView)findViewById(R.id.name_value_text_view);
		addressTextView = (TextView)findViewById(R.id.address_value_text_view);
		
		infraValueTextView = (TextView)findViewById(R.id.infra_value_text_view);
		voltageValueTextView = (TextView)findViewById(R.id.voltage_value_text_view);
		versionValueTextView = (TextView)findViewById(R.id.version_value_text_view);
		speedValueTextView = (TextView)findViewById(R.id.speed_value_text_view);
		
		if (null != connectDevice) {
			nameTextView.setText(connectDevice.getName());
			addressTextView.setText(connectDevice.getAddress());
		}
		
		connectButton = (Button)findViewById(R.id.connect_button);
		disconnectButton = (Button)findViewById(R.id.disconnect_button);
		
		stopButton = (Button)findViewById(R.id.stop_button);
		frowardButton = (Button)findViewById(R.id.forward_button);
		backButton = (Button)findViewById(R.id.back_button);
		leftButton = (Button)findViewById(R.id.left_button);
		rightButton = (Button)findViewById(R.id.right_button);
		
		frowardLeftOneButton = (Button)findViewById(R.id.froward_left_one_button);
		frowardLeftTwoButton = (Button)findViewById(R.id.froward_left_two_button);
		frowardLeftThreeButton = (Button)findViewById(R.id.froward_left_three_button);
		frowardLeftFourButton = (Button)findViewById(R.id.froward_left_four_button);
		
		frowardRightOneButton = (Button)findViewById(R.id.froward_right_one_button);
		frowardRightTwoButton = (Button)findViewById(R.id.froward_right_two_button);
		frowardRightThreeButton = (Button)findViewById(R.id.froward_right_three_button);
		frowardRightFourButton = (Button)findViewById(R.id.froward_right_four_button);
		
		backLeftOneButton = (Button)findViewById(R.id.back_left_one_button);
		backLeftTwoButton = (Button)findViewById(R.id.back_left_two_button);
		backLeftThreeButton = (Button)findViewById(R.id.back_left_three_button);
		backLeftFourButton = (Button)findViewById(R.id.back_left_four_button);
		
		backRightOneButton = (Button)findViewById(R.id.back_right_one_button);
		backRightTwoButton = (Button)findViewById(R.id.back_right_two_button);
		backRightThreeButton = (Button)findViewById(R.id.back_right_three_button);
		backRightFourButton = (Button)findViewById(R.id.back_right_four_button);
		
		upHeaderButton = (Button)findViewById(R.id.top_header_button);
		bottomHeaderButton = (Button)findViewById(R.id.bottom_header_button);
		
		openInfraButton = (Button)findViewById(R.id.open_infra_button);
		closeInfraButton = (Button)findViewById(R.id.close_infra_button);
		
		speedOneButton = (Button)findViewById(R.id.speed_one);
		speedTwoButton = (Button)findViewById(R.id.speed_two);
		speedThreeButton = (Button)findViewById(R.id.speed_three);
		
		searchInfraButton = (Button)findViewById(R.id.search_infra_button);
		searchVoltageButton = (Button)findViewById(R.id.search_voltage_button);
		searchRobotVersionButton = (Button)findViewById(R.id.search_robot_version_button); 
		searchRobotSpeedButton = (Button)findViewById(R.id.search_robot_speed_button);
		
		startAutochargeButton = (Button)findViewById(R.id.start_autocharge);
		stopAutochargeButton = (Button)findViewById(R.id.stop_autocharge);
		
		back = (Button)findViewById(R.id.back);
		
		//stop
		stopButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				PadBotSdk.stop();
			}
		});
		
		//go forward
		frowardButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				PadBotSdk.goForward();
			}
		});
		
		//go backward
		backButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				PadBotSdk.goBackward();
			}
		});
		
		//turn left
		leftButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				PadBotSdk.turnLeft();
			}
		});
		
		//turn right
		rightButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				PadBotSdk.turnRight();
			}
		});
		
		//go forward left
		frowardLeftOneButton.setOnClickListener(new OnClickListener() {
					
			@Override
			public void onClick(View view) {
				PadBotSdk.goForwardLeft(ONE);
			}
		});
		

		frowardLeftTwoButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				PadBotSdk.goForwardLeft(TWO);
			}
		});
		

		frowardLeftThreeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				PadBotSdk.goForwardLeft(TREE);
			}
		});
		

		frowardLeftFourButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				PadBotSdk.goForwardLeft(FOUR);
			}
		});
		

		//go forward right
		frowardRightOneButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				PadBotSdk.goForwardRight(ONE);
			}
		});
		
		
		frowardRightTwoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				PadBotSdk.goForwardRight(TWO);
			}
		});
		

		frowardRightThreeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				PadBotSdk.goForwardRight(TREE);
			}
		});
		
		
		frowardRightFourButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				PadBotSdk.goForwardRight(FOUR);
			}
		});
		
		//go backward left
		backLeftOneButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				PadBotSdk.goBackwardLeft(ONE);
			}
		});
		

		backLeftTwoButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				PadBotSdk.goBackwardLeft(TWO);
			}
		});
		

		backLeftThreeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				PadBotSdk.goBackwardLeft(TREE);
			}
		});
		

		backLeftFourButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				PadBotSdk.goBackwardLeft(FOUR);
			}
		});
		
		//go backward right
		backRightOneButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				PadBotSdk.goBackwardRight(ONE);
			}
		});
		

		backRightTwoButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				PadBotSdk.goBackwardRight(TWO);
			}
		});
		

		backRightThreeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				PadBotSdk.goBackwardRight(TREE);
			}
		});
		

		backRightFourButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				PadBotSdk.goBackwardRight(FOUR);
			}
		});
		
		//head rise
		upHeaderButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				PadBotSdk.headRise();
			}
		});
		
		//headdown
		bottomHeaderButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				PadBotSdk.headDown();
			}
		});
		
		//turn infrared on
		openInfraButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				PadBotSdk.turnInfraredOn();
			}
		});
		
		//turn infrared off
		closeInfraButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				PadBotSdk.turnInfraredOff();
			}
		});
		
		//set movement speed
		speedOneButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				PadBotSdk.setSpeedLevelOne();
			}
		});
		
		//set movement speed
		speedTwoButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				PadBotSdk.setSpeedLevelTwo();
			}
		});
		
		//set movement speed
		speedThreeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				PadBotSdk.setSpeedLevelThree();
			}
		});
		
		
		//get infrared distance
		searchInfraButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				isRun = false;
				searchType = SEARCH_TYPE_INFRA;
				PadBotSdk.getRobotInfrared();
			}
		});
		
		//get robot voltage
		searchVoltageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				isRun = false;
				searchType = SEARCH_TYPE_VOLTAGE;
				PadBotSdk.getRobotVoltage();
			}
		});
		
		//get robot hardware version
		searchRobotVersionButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				isRun = false;
				searchType = SEARCH_TYPE_VERSION;
				PadBotSdk.getRobotVersion();
			}
		});
		
		//get robot hardware speed
		searchRobotSpeedButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				isRun = false;
				searchType = SEARCH_TYPE_SPEED;
				PadBotSdk.getSpeed();

			}
		});
		
		//start auto charge
		startAutochargeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				PadBotSdk.startAutocharge();
			}
		});
		
		//stop auto charge
		stopAutochargeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				PadBotSdk.stopAutocharge();
			}
		});
		
		//disconnect and back to the home page.
		back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				BluetoothService.getInstance().disConnect();
				finish();
			}
		});
		
		bluetoothListener = new BluetoothListener() {
			
			@Override
			public void setupTextView(String value) {
				if (SEARCH_TYPE_INFRA.equals(searchType)) {
					infraString = value;
				}
				else if (SEARCH_TYPE_VOLTAGE.equals(searchType)) {
					voltageString = value;
				}
				else if (SEARCH_TYPE_VERSION.equals(searchType)) {
					versionString = value;


				}
				else if (SEARCH_TYPE_SPEED.equals(searchType)) {
					speedString = value;
				}
				
				if (isRun == false) {
					setupViewHandler.post(runnableUi);
				}
			}
		};
		
		connectButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				BluetoothService.getInstance().connect(connectDevice.getAddress(), bluetoothListener);
				//SendDevice();
				//OpenOtherActivity();
			}
		});
		
		disconnectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BluetoothService.getInstance().disConnect();
			}
		});

	}

	/**
	 * //Jumpping to other Activity directly
	 */


    Runnable   runnableUi=new  Runnable(){
        @Override  
        public void run() {
        	isRun = true;
        	if (SEARCH_TYPE_INFRA.equals(searchType)) {
        		if (voltageValueTextView != null) {
        			infraValueTextView.setText(getString(R.string.infra_value) + infraString);
        		}
			}
			else if (SEARCH_TYPE_VOLTAGE.equals(searchType)) {
				if (voltageValueTextView != null) {
					voltageValueTextView.setText(getString(R.string.voltage_value) + voltageString);
        		}
			}
			else if (SEARCH_TYPE_VERSION.equals(searchType)) {
				if (versionValueTextView != null) {
					versionValueTextView.setText(getString(R.string.version_value)+versionString);
        		}
			}
			else if (SEARCH_TYPE_SPEED.equals(searchType)) {
				if (speedValueTextView != null) {
					speedValueTextView.setText(getString(R.string.speed_value) + speedString);
//					System.out.println(speedString);

				}
			}
        	setupViewHandler.removeCallbacks(runnableUi);
        }
    };

}
