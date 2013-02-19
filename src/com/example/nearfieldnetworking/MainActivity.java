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
	static final String people_path = main_dir + "/people";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		//create main directory if does not exist
		File main_public_dir = new File(main_dir);
	    if(!main_public_dir.exists()){
	    	main_public_dir.mkdir();
	    }

	    //create my_profile directory if does not exist
	    File my_profile_dir = new File(my_profile_path);
	    if(!my_profile_dir.exists()){
	    	my_profile_dir.mkdir();
	    	File resume_dir = new File(my_profile_path + "/Resume");
	    	resume_dir.mkdir();
	    	File portfolio_dir = new File(my_profile_path + "/Profile");
	    	portfolio_dir.mkdir();
	    }

	  //create people directory if does not exist
	    File people_dir = new File(people_path);
	    if(!people_dir.exists()){
	    	people_dir.mkdir();
	    }
	    
	    //create my profile's person object if does not exist
	    Person user = new Person("Your Name");
    	File person_file = new File(my_profile_path + "/.person");
	    if(!person_file.exists()){
			user.writeToFile(person_file);
	    }
	       
	    //listen for my profile button click
	    Button my_profile_button = (Button) findViewById(R.id.button1);
	    my_profile_button.setOnClickListener(new View.OnClickListener() {
	    	public void onClick(View v) {
	    		Intent intent = new Intent(getBaseContext(), DisplayPersonActivity.class);
	           	intent.putExtra("person_directory",my_profile_path);
	           	intent.putExtra("editable", true);
	           	startActivity(intent);  
	    	}
	    });
	    
	  //listen for people button click
	    Button people_button = (Button) findViewById(R.id.button2);
	    people_button.setOnClickListener(new View.OnClickListener() {
	    	public void onClick(View v) {
	    		Intent intent = new Intent(getBaseContext(), DisplayPeopleActivity.class);
	           	intent.putExtra("people_directory",people_path);
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
