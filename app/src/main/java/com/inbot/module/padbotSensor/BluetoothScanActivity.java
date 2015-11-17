package com.inbot.module.padbotSensor;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.inbot.module.padbotSensor.listener.BluetoothListener;

@SuppressLint("NewApi")
public class BluetoothScanActivity extends Activity {
	
	private ListView bluetoothListView;
	private Button scanButton;
	
	private List<BluetoothDevice> deviceList;
	private Handler robotSearchHandler;
	private boolean mScanning;
	private BluetoothDeviceAdapter bluetoothDeviceAdapter;
	private BluetoothListener bluetoothListener;
	private Handler connectedHandler;
	private Handler disconnectedHandler;
	
	private boolean isConnecting;

	private BluetoothDevice connectDevice;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_bluetooth_scan);
		

		if (null == deviceList) {
			deviceList = new ArrayList<BluetoothDevice>();
		}
		
		if (null == robotSearchHandler) {
			robotSearchHandler = new Handler();
		}
		
		if (null == bluetoothDeviceAdapter) {
			bluetoothDeviceAdapter = new BluetoothDeviceAdapter(this, null);
		}
		
		if (null == connectedHandler) {
			connectedHandler = new Handler();
		}
		
		if (null == disconnectedHandler) {
			disconnectedHandler = new Handler();
		}
		
		isConnecting = false;
		
		//init bluetoothManager
	   final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (BluetoothService.getInstance().bluetoothAdapter == null) {
        	BluetoothService.getInstance().bluetoothAdapter = bluetoothManager.getAdapter();
        }


        if (BluetoothService.getInstance().bluetoothAdapter == null) {
        	Toast toast = Toast.makeText(getApplicationContext(), R.string.init_failed, Toast.LENGTH_SHORT);
		    toast.setGravity(Gravity.CENTER, 0, 0);
		    toast.show();
			return;
        }
        
//        BluetoothService.getInstance().bluetoothScanActivity = BluetoothScanActivity.this;
		

		bluetoothListView = (ListView)findViewById(R.id.bluetooth_list_view);

		scanButton = (Button)findViewById(R.id.bluetoot_scan_button);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
        	Toast toast = Toast.makeText(getApplicationContext(), R.string.main_myrobot_need_android_43_to_support, Toast.LENGTH_SHORT);
		    toast.setGravity(Gravity.CENTER, 0, 0);
		    toast.show();
		    finish();
			return;
        }
        
        
        

        scanButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				if (false == mScanning) {
					//搜索蓝牙
					scanBluetoothDevice(true);
				}
			}
		});
        

        bluetoothListView.setOnItemClickListener(new OnItemClickListener() {
        	 @Override  
             public void onItemClick(AdapterView<?> arg0,View arg1, int arg2, long arg3) {   

                 connectDevice = deviceList.get(arg2);
				 BluetoothService.getInstance().connect(connectDevice.getAddress(), bluetoothListener);
                 Toast.makeText(BluetoothScanActivity.this,"Connect to Padbot",Toast.LENGTH_LONG).show();
				 Intent intent = new Intent();
             	 intent.setClass(BluetoothScanActivity.this, SensorActivity.class);
             	 Bundle mBundle = new Bundle();
	   		     mBundle.putParcelable("connectDevice", connectDevice);
	   		     intent.putExtras(mBundle);
             	 startActivityForResult(intent, 0);
             }
		});
	}
	
	/**
	 * scan bluetooth device
	 * @param enable
	 */
	 private void scanBluetoothDevice(final boolean enable) {
        if (enable) {
            
        	robotSearchHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                	if (mScanning == true) {
                		mScanning = false;
                        //stop scan
                		BluetoothService.getInstance().bluetoothAdapter.stopLeScan(mLeScanCallback);
                      //reload ListView
                        reloadListView();
                	}
                    
                    invalidateOptionsMenu();
                }
            }, 5000);

            mScanning = true;
            
            BluetoothService.getInstance().bluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
    
            BluetoothService.getInstance().bluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }
 
 	// Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	if (null != device && null != device.getName() && "" != device.getName()) {
                		boolean isHas = false;
                		
                		for (BluetoothDevice bluetoothDevice : deviceList) {
                			if (device.getName().equals(bluetoothDevice.getName())) {
                				isHas = true;
                				break;
                			}
                		}
                		
                		if (false == isHas) {
                			deviceList.add(device);
                		}
                	}
                }
            });
        }
    };
    

    private class BluetoothDeviceAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<BluetoothDevice> bluetoothDeviceArrayList;
        private TextView nameTextView;
        private TextView addressTextView;
        private TextView connectStateTextView;
        private List<View> holder;

        public BluetoothDeviceAdapter(Context context, List<BluetoothDevice> bluetoothDeviceVoList){
   		 	this.mInflater = LayoutInflater.from(context);
   		 	if (null == deviceList) {
   		 		this.bluetoothDeviceArrayList = new ArrayList<BluetoothDevice>();
   		 	}
   		 	else {
   		 		this.bluetoothDeviceArrayList = deviceList;
   		 	}
   		 	
   	 	}

        @Override
        public int getCount() {
            return bluetoothDeviceArrayList.size();
        }

        @Override
        public Object getItem(int i) {
            return bluetoothDeviceArrayList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            // General ListView optimization code.
            if (view == null) {
                view = mInflater.inflate(R.layout.bluetooth_device_item, null);
                nameTextView = (TextView)view.findViewById(R.id.device_name_value_text_view);
                addressTextView = (TextView)view.findViewById(R.id.device_address_value_text_view);
                connectStateTextView = (TextView)view.findViewById(R.id.device_connect_state_value_text_view);
                holder = new ArrayList<View>();
	   			holder.add(nameTextView);
	   			holder.add(addressTextView);
	   			holder.add(connectStateTextView);
	   			view.setTag(holder);
            } else {
            	holder = (ArrayList)view.getTag();
            }

            final BluetoothDevice device = bluetoothDeviceArrayList.get(i);
            
            ((TextView)holder.get(0)).setText((String)device.getName());
            ((TextView)holder.get(1)).setText((String)device.getAddress());
            if (device.getBondState() == 0) {
            	((TextView)holder.get(2)).setText(getString(R.string.robot_state_connected));
            }
            else {
            	((TextView)holder.get(2)).setText(getString(R.string.robot_state_disconnected));
            }
            
	   		return view;
        }
    }
    
    /**
     * reload listView
     */
    private void reloadListView() {
    	bluetoothDeviceAdapter = new BluetoothDeviceAdapter(this,deviceList);
        bluetoothListView.setAdapter(bluetoothDeviceAdapter);
        bluetoothDeviceAdapter.notifyDataSetChanged();
    }

	/** Messenger for communicating with the service. */
	Messenger mService = null;

	/** Flag indicating whether we have called bind on the service. */
	boolean mBound;

	/**
	 * Class for interacting with the main interface of the service.
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the object we can use to
			// interact with the service.  We are communicating with the
			// service using a Messenger, so here we get a client-side
			// representation of that from the raw IBinder object.
			mService = new Messenger(service);
			mBound = true;
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mService = null;
			mBound = false;
		}
	};

	@Override
	protected void onStart() {
		super.onStart();

	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

}
