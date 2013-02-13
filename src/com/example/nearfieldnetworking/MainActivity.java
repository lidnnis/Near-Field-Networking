package com.example.nearfieldnetworking;

import java.io.File;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	//private variables
	static final String main_dir =  Environment.getExternalStorageDirectory() + "/near_field_networking";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		//create main directory if does not exist
		File main_public_dir = new File(main_dir);
	    if(!main_public_dir.exists()){
	    	main_public_dir.mkdir();
	    }
	       
	    //listen for my profile button click
	    Button options = (Button) findViewById(R.id.button1);
	    options.setOnClickListener(new View.OnClickListener() {
	    	public void onClick(View v) {
	    		Intent intent = new Intent(getBaseContext(), MyProfile.class);
	           	intent.putExtra("main_dir",main_dir);
	           	startActivity(intent);  
	    	}
	    });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.menu_nfc:
	            launchNFCActivity();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public void launchNFCActivity()
	{
		   Intent intent = new Intent(this, NFCActivity.class);
		   startActivity(intent);
	}

}
