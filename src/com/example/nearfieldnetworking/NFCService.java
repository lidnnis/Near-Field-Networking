package com.example.nearfieldnetworking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class NFCService {
	private static final String NAME = "NFN";
	private static final UUID MY_UUID = UUID
			.fromString("e8b7fc40-7735-11e2-bcfd-0800200c9a66");;
	private BluetoothAdapter mBluetoothAdapter;
	private Handler mFileHandler;
	private AcceptThread mAcceptThread;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private int mState;
	private int totalBytes = Integer.MAX_VALUE;
	// private AsyncTask fileWrite;

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_LISTEN = 1; // now listening for incoming
												// connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing
													// connection
	public static final int STATE_CONNECTED = 3; // now connected to a remote
													// device

	public NFCService(Context context, Handler handler) {
		// Use a temporary object that is later assigned to mmServerSocket,
		// because mmServerSocket is final
		mState = STATE_NONE;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mFileHandler = handler;

	}

	public void setSize(int size) {
		totalBytes = size;
		// Log.d("size",Integer.toString(totalBytes));
	}

	private synchronized void setState(int state) {
		mState = state;

		// Give the new state to the Handler so the UI Activity can update
		mFileHandler.obtainMessage(NFCActivity.MESSAGE_STATE_CHANGE, state, -1)
				.sendToTarget();
	}

	/**
	 * Return the current connection state.
	 */
	public synchronized int getState() {
		return mState;
	}

	public synchronized void start() {
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		setState(STATE_LISTEN);

		// Start the thread to listen on a BluetoothServerSocket
		if (mAcceptThread == null) {
			mAcceptThread = new AcceptThread();
			mAcceptThread.start();
		}

	}

	public synchronized void connect(BluetoothDevice device) {
		// Log.d("debug", "connect to: " + device);

		// Cancel any thread attempting to make a connection
		if (mState == STATE_CONNECTING) {
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Start the thread to connect with the given device
		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
		setState(STATE_CONNECTING);
	}

	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 * 
	 * @param socket
	 *            The BluetoothSocket on which the connection was made
	 * @param device
	 *            The BluetoothDevice that has been connected
	 */
	public synchronized void connected(BluetoothSocket socket,
			BluetoothDevice device) {
		// Log.d("debug", "connected, Socket Type:");

		// Cancel the thread that completed the connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Cancel the accept thread because we only want to connect to one
		// device
		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}

		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread(socket);
		mConnectedThread.start();

		// Send the name of the connected device back to the UI Activity
		Message msg = mFileHandler
				.obtainMessage(NFCActivity.MESSAGE_DEVICE_NAME);
		Bundle bundle = new Bundle();
		bundle.putString(NFCActivity.DEVICE_NAME, device.getName());
		msg.setData(bundle);
		mFileHandler.sendMessage(msg);

		setState(STATE_CONNECTED);
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop() {
		// if (D) Log.d(TAG, "stop");

		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}

		setState(STATE_NONE);
	}

	/**
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	private void connectionFailed() {
		// Send a failure message back to the Activity
		Message msg = mFileHandler.obtainMessage(NFCActivity.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(NFCActivity.TOAST, "Unable to connect device");
		msg.setData(bundle);
		mFileHandler.sendMessage(msg);

		// Start the service over to restart listening mode
		NFCService.this.start();
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionLost() {
		// Send a failure message back to the Activity
		Message msg = mFileHandler.obtainMessage(NFCActivity.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(NFCActivity.TOAST, "Device connection was lost");
		msg.setData(bundle);
		mFileHandler.sendMessage(msg);

		// Start the service over to restart listening mode
		NFCService.this.start();
	}

	public void writeToFile(byte[] bytes, String filename) {
		// Create temporary object
		ConnectedThread r;
		// Synchronize a copy of the ConnectedThread
		synchronized (this) {
			if (mState != STATE_CONNECTED)
				return;
			r = mConnectedThread;
		}

		new writeFileTask().execute(bytes, r, filename);

		// Message msg = mFileHandler.obtainMessage(NFCActivity.MESSAGE_DONE);
		// Bundle bundle = new Bundle();
		// bundle.putString(NFCActivity.TOAST, "File Transfer Completed");
		// msg.setData(bundle);
		// mFileHandler.sendMessage(msg);

		// NFCService.this.stop();
	}

	public class AcceptThread extends Thread {

		private final BluetoothServerSocket mmServerSocket;

		public AcceptThread() {
			// Use a temporary object that is later assigned to mmServerSocket,
			// because mmServerSocket is final

			BluetoothServerSocket tmp = null;
			try {
				// MY_UUID is the app's UUID string, also used by the client
				// code
				tmp = mBluetoothAdapter
						.listenUsingInsecureRfcommWithServiceRecord(NAME,
								MY_UUID);
				// tmp =
				// InsecureBluetooth.listenUsingRfcommWithServiceRecord(mBluetoothAdapter,
				// NAME, MY_UUID, true);
			} catch (IOException e) {
			}
			mmServerSocket = tmp;
		}

		public void run() {
			BluetoothSocket socket = null;
			// Keep listening until exception occurs or a socket is returned
			while (mState != STATE_CONNECTED) {
				try {
					socket = mmServerSocket.accept();
				} catch (IOException e) {
					break;
				}
				// If a connection was accepted
				if (socket != null) {
					synchronized (NFCService.this) {
						switch (mState) {
						case STATE_LISTEN:
						case STATE_CONNECTING:
							// Situation normal. Start the connected thread.
							connected(socket, socket.getRemoteDevice());
							break;
						case STATE_NONE:
						case STATE_CONNECTED:
							// Either not ready or already connected. Terminate
							// new socket.
							try {
								socket.close();
							} catch (IOException e) {
								// Log.e(TAG, "Could not close unwanted socket",
								// e);
							}
							break;
						}
					}
				}
			}
		}

		/** Will cancel the listening socket, and cause the thread to finish */
		public void cancel() {
			try {
				mmServerSocket.close();
			} catch (IOException e) {
			}
		}
	}

	public class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;
			mmDevice = device;

			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app's UUID string, also used by the server
				// code

				// Log.d("debug",MY_UUID.toString());
				tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
				// tmp =
				// InsecureBluetooth.createRfcommSocketToServiceRecord(device,
				// MY_UUID, true);
			} catch (IOException e) {
			}
			mmSocket = tmp;
		}

		public void run() {
			// Cancel discovery because it will slow down the connection
			mBluetoothAdapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				mmSocket.connect();
			} catch (IOException e) {
				// Close the socket
				try {
					mmSocket.close();
				} catch (IOException e2) {
					// Log.e(TAG, "unable to close() " + mSocketType +
					// " socket during connection failure", e2);
				}
				connectionFailed();
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (NFCService.this) {
				mConnectThread = null;
			}

			// Start the connected thread
			connected(mmSocket, mmDevice);
		}

		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}

	public class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			// byte[] buffer = new byte[1024];
			// byte[] fileBuffer = new byte[1024]; // buffer store for the
			// stream
			int bytes; // bytes returned from read()
			int pos = 0;

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					byte[] buffer = new byte[990];
					// Read from the InputStream
					bytes = mmInStream.read(buffer);
					// System.arraycopy(buffer,0,fileBuffer,0,bytes);

					pos += bytes;

					// byte[] temp;
					// if (totalBytes - pos < 1024)
					// {
					// temp = new byte[totalBytes - pos];
					// System.arraycopy(buffer,0,temp,0,totalBytes - pos);
					//
					// mFileHandler.obtainMessage(NFCActivity.MESSAGE_READ, pos,
					// bytes, temp).sendToTarget();
					// }
					//
					// // Log.d("string", new String(buffer));
					//
					// else
					// {
					mFileHandler.obtainMessage(NFCActivity.MESSAGE_READ, pos,
							bytes, buffer).sendToTarget();
					// }

					if (pos >= totalBytes)
						pos = 0;

				} catch (IOException e) {
					Log.e("debug", "disconnected", e);
					connectionLost();
					// Start the service over to restart listening mode
					NFCService.this.start();
					break;
				}
			}
		}

		/* Call this from the main activity to send data to the remote device */
		public void write(byte[] bytes) {
			try {
				mmOutStream.write(bytes);
			} catch (IOException e) {
			}
		}

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}

	private class writeFileTask extends AsyncTask<Object, Void, Void> {

		@Override
		protected Void doInBackground(Object... args) {

			byte[] bytes = (byte[]) args[0];
			ConnectedThread r = (ConnectedThread) args[1];
			String filename = (String) args[2];

			int totalBytes = bytes.length;

			int pos = 0;

			while (pos <= totalBytes) {
				byte[] tempBuffer = new byte[990];

				if (pos == totalBytes) {
				}

				else if (totalBytes - pos < 990) {
					tempBuffer = new byte[totalBytes - pos];
					System.arraycopy(bytes, pos, tempBuffer, 0, totalBytes
							- pos);
					pos += (totalBytes - pos);
				} else {
					System.arraycopy(bytes, pos, tempBuffer, 0, 990);
					pos += 990;
				}

				Log.d("totalBytes", Integer.toString(totalBytes));
				Log.d("curPos", Integer.toString(pos));
				// Log.d("toString",new String(tempBuffer));
				// Perform the write unsynchronized
				r.write(tempBuffer);

				Message msg = mFileHandler
						.obtainMessage(NFCActivity.MESSAGE_UPDATE);
				Bundle bundle = new Bundle();
				bundle.putInt(NFCActivity.PROGRESS, pos);
				bundle.putInt(NFCActivity.TOTAL, totalBytes);
				bundle.putString(NFCActivity.FNAME, filename);
				// bundle.putString(NFCActivity.PROGRESS, pos);
				msg.setData(bundle);
				mFileHandler.sendMessage(msg);

				if (pos == totalBytes) {
					pos++;
				}

			}

			Log.d("end", "File sent");
			return null;
		}
	}
}
