package com.example.nearfieldnetworking;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ProgressDialog;
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
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
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
	// private DialogFragment newFragment;
	TextView mInfoText;

	public static final int REQUEST_ENABLE_BT = 1;
	public static final int REQUEST_FILE = 2;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	// public static final int MESSAGE_DONE = 6;
	public static final int MESSAGE_SENT = 6;
	public static final int MESSAGE_UPDATE = 7;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String PROGRESS = "progress";
	public static final String TOAST = "toast";

	// public static final String PREFS_NAME = "NFCPrefsFile";

	private String recievedFilepath;
	private String recievedFilename;
	private FileOutputStream fos = null;
	private int totalSize;
	// private int prevSize = 0;
	private boolean flag = false;
	private ProgressDialog progressBar;
	private ProgressDialog progressSBar;
	private ProgressDialog waitBar;

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

		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		// newFragment = new ClientDialog();

		if (mNfcAdapter == null) {
			mInfoText = (TextView) findViewById(R.id.textView);
			mInfoText.setText("NFC is not available on this device.");
		} else {
			// Register callback to set NDEF message
			mNfcAdapter.setNdefPushMessageCallback(this, this);
			// Register callback to listen for message-sent success
			mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
		}

		File directory = new File(Environment.getExternalStorageDirectory()
				+ File.separator + "NFN");
		directory.mkdirs();

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

	@Override
	public synchronized void onPause() {
		super.onPause();
		Log.e("debug", "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.e("debug", "-- ON STOP --");
	}

	@Override
	public void onResume() {
		super.onResume();

		Log.e("debug", "+ ON RESUME +");

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
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mNFCService != null)
			mNFCService.stop();
		Log.e("debug", "--- ON DESTROY ---");
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

		// Pair BT Devices
		// if (!mBluetoothAdapter.getBondedDevices().isEmpty()) {
		bt = mBluetoothAdapter.getRemoteDevice(macAddr);
		// } else {
		// Toast.makeText(getApplicationContext(), "Something went wrong",
		// Toast.LENGTH_LONG).show();
		// finish();
		// }

		mNFCService.connect(bt);

		// try {
		// synchronized (this) {
		// wait(5000);
		// }
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// newFragment.show(getSupportFragmentManager(), "ClientDialog");
		//
		//
		// getSupportFragmentManager().executePendingTransactions();
		//
		// newFragment.getDialog().setOnCancelListener(new OnCancelListener() {
		// @Override
		// public void onCancel(DialogInterface dialog) {
		// mNFCService.stop();
		// Toast.makeText(getApplicationContext(),
		// "Connection Terminated", Toast.LENGTH_LONG).show();
		// finish();
		//
		// }
		// });

		waitBar = new ProgressDialog(NFCActivity.this);
		waitBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		waitBar.setTitle("Initiating Transfer");
		waitBar.setMessage("Waiting For Data");
		waitBar.show();

		waitBar.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				mNFCService.stop();
				Toast.makeText(getApplicationContext(),
						"Connection Terminated", Toast.LENGTH_LONG).show();
				finish();

			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_nfc, menu);
		return true;
	}

	// @Override
	// protected void onSaveInstanceState(Bundle outState) {
	// //No call for super(). Bug on API Level > 11.
	// }

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
			// case R.id.menu_nfc_settings:
			// intent = new Intent(Intent.ACTION_GET_CONTENT);
			// intent.setType("*/*");
			// startActivityForResult(intent, REQUEST_FILE);
			// return true;
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

				// Uri selectedImage = intent.getData();
				// String[] filePathColumn = {MediaStore.Images.Media.DATA};
				//
				// Cursor cursor = getContentResolver().query(
				// filesToSend[0], filePathColumn, null, null, null);
				// cursor.moveToFirst();
				//
				// int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				// String filePath = cursor.getString(columnIndex);
				// cursor.close();

				// ArrayList<Parcelable> list =
				// intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
				// for (Parcelable p : list) {
				// Uri uri = (Uri) p;
				// Log.d("debug",uri.toString());
				// /// do something with it.
				// }

				try {

					progressSBar = new ProgressDialog(NFCActivity.this);
					progressSBar
							.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					progressSBar.setTitle("Sending");

					progressSBar.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							mNFCService.stop();
							Toast.makeText(getApplicationContext(),
									"Connection Terminated", Toast.LENGTH_LONG)
									.show();
							finish();

						}
					});
					
					//filesToSend[0].getPath();

					filesToSend = new Uri[] {Uri.fromFile(new File(Environment.getExternalStorageDirectory()+"/DCIM/Camera/IMG_20121225_130021.jpg")),Uri.fromFile(new File(Environment.getExternalStorageDirectory()+"/DCIM/Camera/IMG_20121226_175015.jpg"))};
					
					for (int i = 0; i != filesToSend.length; i++) {

						Toast.makeText(
								getApplicationContext(),
								"Sending "
										+ new File(filesToSend[i].getPath())
												.getName(), Toast.LENGTH_LONG)
								.show();

						mNFCService.writeToFile(readBytes(filesToSend[i]));
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				mNFCService.stop();
				Toast.makeText(getApplicationContext(),
						"Connection Terminated", Toast.LENGTH_LONG).show();
				finish();
			}

		}
	}

	/** This handler receives a message from onNdefPushComplete */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case MESSAGE_SENT:
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
					Log.d("debug", "connected");
					break;
				case NFCService.STATE_CONNECTING:
					// setStatus(R.string.title_connecting);
					Log.d("debug", "connecting");
					break;
				case NFCService.STATE_LISTEN:
				case NFCService.STATE_NONE:
					// setStatus(R.string.title_not_connected);
					Log.d("debug", "closed");
					break;
				}
				break;

			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				int pos = msg.arg1;
				// construct a string from the valid bytes in the buffer

				byte[] buffer = new byte[1024];

				byte[] totalBytes = new byte[512];

				// Log.d("totalBytes",Integer.toString(wrapped.getInt()));

				// Toast.makeText(getApplicationContext(),
				// new String(headerBuf), Toast.LENGTH_SHORT)
				// .show();

				Log.d("pos", Integer.toString(pos));

				// Log.d("string", new String(readBuf));

				// Array.Resize(readBuf, 5);
				if (pos <= 1024 && !flag) {

					// get filename
					System.arraycopy(readBuf, 0, buffer, 0, 512);
					recievedFilepath = new String(buffer).trim();
					recievedFilename = new File(recievedFilepath).getName();

					// get totalsize
					System.arraycopy(readBuf, 511, totalBytes, 0, 512);

					ByteBuffer wrapped = ByteBuffer.wrap(totalBytes);
					IntBuffer ib = wrapped.asIntBuffer();
					totalSize = ib.get(0);
					
					mNFCService.setSize(totalSize);

					Log.d("totalBytes", Integer.toString(totalSize));

					File path = Environment.getExternalStorageDirectory();
					File file = new File(path + "/NFN", recievedFilename);

					try {
						fos = new FileOutputStream(file);
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					// Hide original dialog and show progressbar
					if (waitBar != null) {
						if (waitBar.isShowing()) {
							waitBar.dismiss();
						}

						// Log.d("debug","hey this is here");

						if (progressBar == null || !progressBar.isShowing()) {
							progressBar = new ProgressDialog(NFCActivity.this);
							progressBar
									.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
							progressBar.setTitle("Downloading");

							progressBar
									.setOnCancelListener(new OnCancelListener() {
										@Override
										public void onCancel(
												DialogInterface dialog) {
											mNFCService.stop();
											Toast.makeText(
													getApplicationContext(),
													"Connection Terminated",
													Toast.LENGTH_LONG).show();
											finish();

										}
									});
						}
						progressBar.setMessage("File: " + recievedFilename);
						progressBar.setMax(totalSize);

						if (!progressBar.isShowing()) {
							progressBar.show();
						}

						// prevSize = pos;
						flag = true;

					}
				}

				else if (pos > 1024) {

					if (pos - totalSize > 0) {
						buffer = new byte[pos - totalSize];
						System.arraycopy(readBuf, 0, buffer, 0, pos - totalSize);
					}

					// else
					// Log.d("size", Integer.toString(msg.arg2));
					buffer = new byte[msg.arg2];
					System.arraycopy(readBuf, 0, buffer, 0, msg.arg2);

					progressBar.setProgress(pos);
					// prevSize = pos;

					// Copy entire contents of file

					try {
						fos.write(buffer);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				// String readMessage = new String(readBuf, 0, msg.arg1);
				// Toast.makeText(getApplicationContext(), readMessage,
				// Toast.LENGTH_LONG).show();

				if (pos >= totalSize) {

					try {
						fos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					Toast.makeText(getApplicationContext(),
							recievedFilename + " succesfully downloaded",
							Toast.LENGTH_SHORT).show();

					// Reset for next iteration
					//pos = 0;
					flag = false;

					// try {
					// synchronized (this) {
					// wait(1000);
					// }
					// } catch (InterruptedException e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// }
					//
					// Log.d("debug", "not right");

					// if (progressBar.isShowing())
					// progressBar.dismiss();
					// mNFCService.stop(); //ONLY SHOULD BE DONE AFTER ALL FILES
					// RECEIVED
					// finish();
				}

				break;
			// case MESSAGE_DONE:
			// Toast.makeText(getApplicationContext(),
			// msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
			// .show();
			// mNFCService.stop();
			// finish();
			// break;
			case MESSAGE_UPDATE:
				int progress = msg.getData().getInt(PROGRESS);

				// if (sendNextFile)
				// {
				// progressSBar.setMessage("File: " + fname);
				// }

				if (progress < totalSize) {
					progressSBar.setProgress(progress);
					// sendNextFile = false;
				} else {
					progressSBar.setProgress(progress);
					
					Toast.makeText(getApplicationContext(),
							"Files sent succesfully", Toast.LENGTH_SHORT)
							.show();

					//sendNextFile = true;

					// try {
					// synchronized (this) {
					// wait(1000);
					// }
					// } catch (InterruptedException e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// }

					// FINISH only if last file
					// progressSBar.dismiss();
					// mNFCService.stop();
					// finish();
				}
				break;
			}
		}
	};

	// converts file to bytes
	public byte[] readBytes(Uri uri) throws IOException {
		// this dynamically extends to take the bytes you read

		// for ()

		InputStream inputStream = getContentResolver().openInputStream(uri);
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

		String fname = (new File(uri.getPath())).getName();
		Log.d("debug", uri.getPath());
		progressSBar.setMessage("File: " + fname);

		int headerSize = 1024;
		byte[] headerBuffer = new byte[headerSize];

		System.arraycopy(uri.getPath().getBytes(), 0, headerBuffer, 0, uri
				.getPath().getBytes().length);
		byteBuffer.write(headerBuffer);

		// this is storage overwritten on each iteration with bytes
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];

		// we need to know how may bytes were read to write them to the
		// byteBuffer

		int len = 0;
		totalSize = 1024;
		while ((len = inputStream.read(buffer)) != -1) {
			totalSize += len;
			byteBuffer.write(buffer, 0, len);
			byteBuffer.flush();
		}

		ByteBuffer b = ByteBuffer.allocate(4);
		b.putInt(totalSize);
		progressSBar.setMax(totalSize);

		if (!progressSBar.isShowing())
			progressSBar.show();

		// and then we can return your byte array.
		byte[] bb = byteBuffer.toByteArray();

		// Log.d("sender", totalBytes.toString());

		// Add total number of bytes to header
		System.arraycopy(b.array(), 0, bb, 511, b.array().length);

		// byte[] tB = new byte[512];
		// System.arraycopy(bb,511,tB,0,512);
		//
		// int tByte;

		// ByteBuffer wrapped = ByteBuffer.wrap(tB);
		// IntBuffer ib = wrapped.asIntBuffer();
		// int i0 = ib.get(0);
		//
		// Log.d("totalBytes",Integer.toString(i0));

		return bb;
	}

}
