package com.inbot.module.padbotSensor;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.inbot.module.padbotsdk.PadBotSdk;
import com.inbot.module.padbotSensor.listener.BluetoothListener;

@SuppressLint("NewApi")
public class BluetoothService extends Service {
	private static BluetoothService instance;
	
	private BluetoothService(){};
	
	public static BluetoothService getInstance()
	{
		if (instance == null)
		{
			instance = new BluetoothService();
		}
		return instance;
	}
	
	
	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;
	
	
	public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
	public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
	
	public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString("0000FFF1-0000-1000-8000-00805f9b34fb");
	
	private final static String SEARCH_TYPE_INFRA = "INFRA";
	private final static String SEARCH_TYPE_VOLTAGE = "VOLEAGE";
	private final static String SEARCH_TYPE_VERSION = "VERSION";
	
	private int mConnectionState = STATE_DISCONNECTED;
	private String bluetoothDeviceAddress;
	public BluetoothAdapter bluetoothAdapter;
	private static BluetoothGatt mBluetoothGatt;
	
	private BluetoothGattCharacteristic mNotifyCharacteristic;
	
	private CustomBluetoothGattCallback customBluetoothGattCallback;
	private class CustomBluetoothGattCallback extends BluetoothGattCallback {
		
		private BluetoothListener mBluetoothListener;
		public CustomBluetoothGattCallback () {
		}
		
		public void setBluetoothListener(
				BluetoothListener bluetoothListener) {
			this.mBluetoothListener = bluetoothListener;
		}
		
		
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,int newState) {
			String intentAction;
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				intentAction = ACTION_GATT_CONNECTED;
				mConnectionState = STATE_CONNECTED;
				
				//Set bluetoothGatt after conntected.
				PadBotSdk.setupBluetooth(mBluetoothGatt);
				mBluetoothGatt.discoverServices();
				
				broadcastUpdate(intentAction);
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				intentAction = ACTION_GATT_DISCONNECTED;
				mConnectionState = STATE_DISCONNECTED;
				setupNotify(false);
				//Remove bluetoothGatt after Bluetooth disconnected.
				PadBotSdk.clearBluetooth();
				broadcastUpdate(intentAction);
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				//setupNotify
				setupNotify(true);
				broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
			} else {
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
			}
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,BluetoothGattDescriptor descriptor, int status) {

		}


		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			if (characteristic.getValue() != null) {
				Log.i("backValue", characteristic.getValue().toString());
				String result = bytesToHexString(characteristic.getValue());
				mBluetoothListener.setupTextView(result);
			}
			broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
		}

		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			
		}


		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			if  (status == 0) {
				
			}

		};
	};
	
	private void broadcastUpdate(final String action) {
		final Intent intent = new Intent(action);
		sendBroadcast(intent);
	}
	
	private void broadcastUpdate(final String action,
			final BluetoothGattCharacteristic characteristic) {
		final Intent intent = new Intent(action);
		
		if (UUID.fromString(PadBotSdk.getNotifyCharactUuid()).equals(characteristic.getUuid())) {
			int flag = characteristic.getProperties();
			int format = -1;
			if ((flag & 0x01) != 0) {
				format = BluetoothGattCharacteristic.FORMAT_UINT16;
			} else {
				format = BluetoothGattCharacteristic.FORMAT_UINT8;
			}
			final int heartRate = characteristic.getIntValue(format, 1);
			intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
		} else {
			final byte[] data = characteristic.getValue();
			if (data != null && data.length > 0) {
				final StringBuilder stringBuilder = new StringBuilder(
						data.length);
				for (byte byteChar : data)
					stringBuilder.append(String.format("%02X ", byteChar));

				intent.putExtra(EXTRA_DATA, new String(data) + "\n"
						+ stringBuilder.toString());
			}
		}
		sendBroadcast(intent);
	}
	
	 /**
	 * connect to the robot
	 * @param address
	 * @return
	 */
	public boolean connect(final String address, BluetoothListener bluetoothListener) {
		//initialize PadBotSdk before connect
		PadBotSdk.init();
		
		if (bluetoothAdapter == null || address == null) {
			return false;
		}
		
		final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
		if (device == null) {
			return false;
		}
		
		if (null == customBluetoothGattCallback) {
			customBluetoothGattCallback = new CustomBluetoothGattCallback(); 
		}
		
		customBluetoothGattCallback.setBluetoothListener(bluetoothListener);
		
		// We want to directly connect to the device, so we are setting the
		// autoConnect
		// parameter to false.
		mBluetoothGatt = device.connectGatt(this, false, customBluetoothGattCallback);
		Log.i("mBluetoothGatt", "mBluetoothGatt1"+mBluetoothGatt.toString());
		
		return true;
	}
	
	/**
	 * disconnect bluetoothGatt
	 */
	public boolean disConnect() {
		if (mBluetoothGatt == null) {
			return false;
		}
		mBluetoothGatt.disconnect();
		return true;
	}
	
	/**
	 * get bluetooth device
	 * @return
	 */
	public BluetoothDevice getDevice() {
		BluetoothDevice bluetoothDevice = null;
		if (null != mBluetoothGatt) {
			bluetoothDevice = mBluetoothGatt.getDevice();
		}
		return bluetoothDevice;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,boolean enabled) {
        if (bluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        
    }
    
    private void setupNotify(boolean value) {
    	String serviceUUID = "0000FFF0-0000-1000-8000-00805f9b34fb";
		BluetoothGattService mBluetoothGattServer = mBluetoothGatt.getService(UUID.fromString(serviceUUID));
		if (mBluetoothGattServer == null) {
			return;
		}

		String notifyCharactUuid = PadBotSdk.getNotifyCharactUuid();
		mNotifyCharacteristic = mBluetoothGattServer.getCharacteristic(UUID.fromString(notifyCharactUuid));
		if (mNotifyCharacteristic == null) {
			return;
		}
		
		final int charaProp = mNotifyCharacteristic.getProperties();
		if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
			if (bluetoothAdapter == null || mBluetoothGatt == null) {
	            return;
	        }
	        mBluetoothGatt.setCharacteristicNotification(mNotifyCharacteristic, value);
	        
	        BluetoothGattDescriptor bluetoothGattDescriptor = mNotifyCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
			if (bluetoothGattDescriptor != null) {
				bluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				mBluetoothGatt.writeDescriptor(bluetoothGattDescriptor);
			}
		}
    }
    
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }
    
    private static String bytesToHexString(byte[] bytes) {
        String result = "";
		try {
			result = new String(bytes,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        return result;
    }
    
}
