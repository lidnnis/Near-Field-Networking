package com.example.nearfieldnetworking;

import java.io.File;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateBeamUrisCallback;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.Settings;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

//, CreateBeamUrisCallback

public class NFCActivity extends Activity implements CreateNdefMessageCallback,
		OnNdefPushCompleteCallback {
	NfcAdapter mNfcAdapter;
	BluetoothAdapter mBluetoothAdapter;
	TextView mInfoText;
	private static final int MESSAGE_SENT = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	private static final int PHOTO_INTENT = 3;
	public static final String PREFS_NAME = "NFCPrefsFile";

	private String filePath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nfc);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		mInfoText = (TextView) findViewById(R.id.textView);
		// Check for available NFC Adapter
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter == null) {
			mInfoText = (TextView) findViewById(R.id.textView);
			mInfoText.setText("NFC is not available on this device.");
		} else {
			// Register callback to set NDEF message
			// mNfcAdapter.setNdefPushMessageCallback(this, this);
			// Register callback to listen for message-sent success
			mNfcAdapter.setOnNdefPushCompleteCallback(this, this);

			// mNfcAdapter.setBeamPushUris(new Uri[] {uri1, uri2}, this);
			//mNfcAdapter.setBeamPushUrisCallback(this, this);
		}

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			// Device does not support Bluetooth
		}

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		filePath = settings.getString("filePath", "");
		
		Log.d("debug",filePath);

	}

//	@Override
//	public Uri[] createBeamUris(NfcEvent event) {
//
//		ArrayList<Uri> uris = new ArrayList<Uri>();
//		
//		uris.add(Uri.fromFile(new File(filePath)));
//		
//		Log.d("debug",uris.get(0).toString());
//		
//		
//		Uri[] ret = new Uri[uris.size()];
//		uris.toArray(ret);
//		
//		return ret;
//	}

	/**
	 * Implementation for the CreateNdefMessageCallback interface
	 */
	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		Time time = new Time();
		time.setToNow();
		String text = ("Beam me up!\n\n" + "Beam Time: " + time
				.format("%H:%M:%S"));
		NdefMessage msg = new NdefMessage(NdefRecord.createMime(
				"application/com.example.nearfieldnetworking", text.getBytes())
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
				Toast.makeText(getApplicationContext(), "Message sent!",
						Toast.LENGTH_LONG).show();
				break;
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
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
		//mInfoText.setText(new String(msg.getRecords()[0].getPayload()));
		mInfoText.setText(new String("Transfer Complete"));
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
			startActivityForResult(intent, PHOTO_INTENT);
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
		if (requestCode == PHOTO_INTENT && resultCode==RESULT_OK) {
			
			mNfcAdapter.setBeamPushUris(new Uri[] {intent.getData()}, this);
			
			
			
//			if (intent != null) {
//				// Log.d(LOG_TAG, "idButSelPic Photopicker: " +
//				// intent.getDataString());
//				Cursor cursor = getContentResolver().query(intent.getData(),
//						null, null, null, null);
//				cursor.moveToFirst(); // if not doing this, 01-22 19:17:04.564:
//										// ERROR/AndroidRuntime(26264): Caused
//										// by:
//										// android.database.CursorIndexOutOfBoundsException:
//										// Index -1 requested, with a size of 1
//				int idx = cursor.getColumnIndex(ImageColumns.DATA);
//				String fileSrc = cursor.getString(idx);
//				// Log.d(LOG_TAG, "Picture:" + fileSrc);
//				// m_Tv.setText("Image selected:"+fileSrc);
//				Toast.makeText(getApplicationContext(), fileSrc,
//						Toast.LENGTH_LONG).show();
//
//				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
//				SharedPreferences.Editor editor = settings.edit();
//				editor.putString("filePath", fileSrc);
//
//				// Commit the edits!
//				editor.commit();
//
//				// Bitmap bitmapPreview = BitmapFactory.decodeFile(fileSrc);
//				// //load preview image
//				// BitmapDrawable bmpDrawable = new
//				// BitmapDrawable(bitmapPreview);
//				// m_Image.setBackgroundDrawable(bmpDrawable);
//			} else {
//				// Log.d(LOG_TAG, "idButSelPic Photopicker canceled");
//				// m_Tv.setText("Image selection canceled!");
//				Toast.makeText(getApplicationContext(),
//						"Image selection canceled!", Toast.LENGTH_LONG).show();
//			}
		}
	}

}