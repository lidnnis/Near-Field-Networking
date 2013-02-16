package com.example.nearfieldnetworking;

import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

//, CreateBeamUrisCallback

public class NFCActivity extends FragmentActivity implements
		CreateNdefMessageCallback, OnNdefPushCompleteCallback {
	private NfcAdapter mNfcAdapter;
	private BluetoothAdapter mBluetoothAdapter;
	private NFCService mNFCService = null;
	private BluetoothDevice bt;

	private Uri[] filesToSend;
	private DialogFragment newFragment;
	TextView mInfoText;

	public static final int REQUEST_ENABLE_BT = 1;
	public static final int REQUEST_FILE = 2;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int MESSAGE_DONE = 6;

	public static final int MESSAGE_SENT = 7;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	public static final String PREFS_NAME = "NFCPrefsFile";

	// private String filePath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nfc);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		mInfoText = (TextView) findViewById(R.id.textView);
		// Check for available NFC Adapter
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (mNfcAdapter == null) {
			mInfoText = (TextView) findViewById(R.id.textView);
			mInfoText.setText("NFC is not available on this device.");
		} else {
			// Register callback to set NDEF message
			mNfcAdapter.setNdefPushMessageCallback(this, this);
			// Register callback to listen for message-sent success
			mNfcAdapter.setOnNdefPushCompleteCallback(this, this);

			// mNfcAdapter.setBeamPushUris(new Uri[] {uri1, uri2}, this);
			// mNfcAdapter.setBeamPushUrisCallback(this, this);
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		// if(D) Log.e(TAG, "++ ON START ++");
		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mNFCService == null)
				mNFCService = new NFCService(this, mFileHandler);
		}
	}

	/**
	 * Implementation for the CreateNdefMessageCallback interface
	 */
	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		String macAddr = mBluetoothAdapter.getAddress();
		NdefMessage msg = new NdefMessage(NdefRecord.createMime(
				"application/com.example.nearfieldnetworking",
				macAddr.getBytes())
		/**
		 * The Android Application Record (AAR) is commented out. When a device
		 * receives a push with an AAR in it, the application specified in the
		 * AAR is guaranteed to run. The AAR overrides the tag dispatch system.
		 * You can add it back in to guarantee that this activity starts when
		 * receiving a beamed message. For now, this code uses the tag dispatch
		 * system.
		 */
		// ,NdefRecord.createApplicationRecord("com.example.android.beam")
		);
		return msg;
	}

	/**
	 * Implementation for the OnNdefPushCompleteCallback interface
	 */
	@Override
	public void onNdefPushComplete(NfcEvent arg0) {
		// A handler is needed to send messages to the activity when this
		// callback occurs, because it happens from a binder thread
		mHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
	}

	/** This handler receives a message from onNdefPushComplete */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case MESSAGE_SENT:
				// Toast.makeText(getApplicationContext(), "Pairing Initiated",
				// Toast.LENGTH_LONG).show();
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("*/*");
				startActivityForResult(intent, REQUEST_FILE);
				break;
			}
		}
	};

	private final Handler mFileHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case NFCService.STATE_CONNECTED:
					// setStatus(getString(R.string.title_connected_to,
					// mConnectedDeviceName));
					break;
				case NFCService.STATE_CONNECTING:
					// setStatus(R.string.title_connecting);
					break;
				case NFCService.STATE_LISTEN:
				case NFCService.STATE_NONE:
					// setStatus(R.string.title_not_connected);
					break;
				}
				break;

			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				Toast.makeText(getApplicationContext(), readMessage,
						Toast.LENGTH_LONG).show();

				if (newFragment.getDialog().isShowing()) {
					newFragment.getDialog().cancel();
				}
				mNFCService.stop();
				finish();
				break;
			case MESSAGE_DONE:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				mNFCService.stop();
				finish();
				break;
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();

		if (mNFCService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mNFCService.getState() == NFCService.STATE_NONE) {
				// Start the Bluetooth chat services
				mNFCService.start();
			}
		}

		// Check to see that the Activity started due to an Android Beam
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			processIntent(getIntent());
		}
	}

	@Override
	public void onNewIntent(Intent intent) {
		// onResume gets called after this to handle the intent
		setIntent(intent);
	}

	/**
	 * Parses the NDEF Message from the intent and prints to the TextView
	 */
	void processIntent(Intent intent) {
		Parcelable[] rawMsgs = intent
				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		// only one message sent during the beam
		NdefMessage msg = (NdefMessage) rawMsgs[0];
		// record 0 contains the MIME type, record 1 is the AAR, if present
		String macAddr = new String(msg.getRecords()[0].getPayload());
		// mInfoText.setText(macAddr);

		// Pair BT Devices

		if (!mBluetoothAdapter.getBondedDevices().isEmpty())
			bt = mBluetoothAdapter.getRemoteDevice(macAddr);
		// mInfoText.setText(new String("Transfer Complete"));
		// AcceptThread at = new AcceptThread();
		// at.run();
		mNFCService.connect(bt);

		newFragment = new ClientDialog();

		newFragment.show(getSupportFragmentManager(), "ClientDialog");
		getSupportFragmentManager().executePendingTransactions();

		newFragment.getDialog().setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub

				// mNFCService.stop();
				// Toast.makeText(getApplicationContext(),
				// "Connection Terminated", Toast.LENGTH_LONG).show();
				// finish();

			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_nfc, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.phone_nfc_settings:
			Intent intent = new Intent(Settings.ACTION_NFCSHARING_SETTINGS);
			startActivity(intent);
			return true;
		case R.id.phone_bt_settings:
			intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, REQUEST_ENABLE_BT);
			return true;
		case R.id.menu_nfc_settings:
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("*/*");
			startActivityForResult(intent, REQUEST_FILE);
			return true;
		case android.R.id.home:
			// app icon in action bar clicked; go home
			intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (requestCode == REQUEST_FILE) {

			if (resultCode == RESULT_OK) {
				filesToSend = new Uri[] { intent.getData() };
				Toast.makeText(getApplicationContext(),
						filesToSend[0].getPath(), Toast.LENGTH_LONG).show();

				mNFCService.writeToFile(filesToSend[0].getPath().getBytes());
			} else {
				// mNFCService.stop();
				// Toast.makeText(getApplicationContext(),
				// "Connection Terminated", Toast.LENGTH_LONG).show();
				// finish();
			}

		}
	}

}
