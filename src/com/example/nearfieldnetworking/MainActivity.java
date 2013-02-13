package com.example.nearfieldnetworking;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	//private variables
	static final String main_dir =  Environment.getExternalStorageDirectory() + "/near_field_networking";
	static final String my_profile_path = main_dir + "/my_profile";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		//create main directory if does not exist
		File main_public_dir = new File(main_dir);
	    if(!main_public_dir.exists()){
	    	main_public_dir.mkdir();
	    }
	    Toast.makeText(getApplicationContext(), "On Create", Toast.LENGTH_SHORT).show();
	    //create my_profile directory if does not exist
	    File my_profile_dir = new File(my_profile_path);
	    if(!my_profile_dir.exists()){
	    	my_profile_dir.mkdir();
	    }
	    
	    //create my profile's person object if does not exist
	    Person user = new Person("Your Name");
    	File person_file = new File(my_profile_path + "/person");
	    if(!person_file.exists()){
			try {
				FileOutputStream fout = new FileOutputStream(person_file);
				ObjectOutputStream oout = new ObjectOutputStream(fout);
		    	oout.writeObject(user);
		    	oout.flush();
		    	oout.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	    }
	       
	    //listen for my profile button click
	    Button options = (Button) findViewById(R.id.button1);
	    options.setOnClickListener(new View.OnClickListener() {
	    	public void onClick(View v) {
	    		Intent intent = new Intent(getBaseContext(), DisplayPersonActivity.class);
	           	intent.putExtra("person_directory",my_profile_path);
	           	intent.putExtra("editable", true);
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
