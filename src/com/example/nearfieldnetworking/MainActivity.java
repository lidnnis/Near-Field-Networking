package com.example.nearfieldnetworking;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	//private variables
	public static final String MAIN_DIR =  Environment.getExternalStorageDirectory() + File.separator + "near_field_networking";
	public static final String MY_PROFILE_PATH = MAIN_DIR + File.separator + "my_profile";
	public static final String PEOPLE_PATH = MAIN_DIR + File.separator +  "people";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		//create main directory if does not exist
		File main_public_dir = new File(MAIN_DIR);
	    if(!main_public_dir.exists()){
	    	main_public_dir.mkdir();
	    }

	    //create my_profile directory if does not exist
	    File my_profile_dir = new File(MY_PROFILE_PATH);
	    if(!my_profile_dir.exists()){
	    	my_profile_dir.mkdir();
	    	File resume_dir = new File(MY_PROFILE_PATH + File.separator + "Resume");
	    	resume_dir.mkdir();
	    	File portfolio_dir = new File(MY_PROFILE_PATH + File.separator + "Portfolio");
	    	portfolio_dir.mkdir();
	    }

	  //create people directory if does not exist
	    File people_dir = new File(PEOPLE_PATH);
	    if(!people_dir.exists()){
	    	people_dir.mkdir();
	    }
	    
	    //create my profile's person object if does not exist
	    Person user = new Person("Your Name");
    	File person_file = new File(MY_PROFILE_PATH + "/.person");
	    if(!person_file.exists()){
			user.writeToFile(person_file);
	    }
	       
	    //listen for my profile button click
	    Button my_profile_button = (Button) findViewById(R.id.button1);
	    my_profile_button.setOnClickListener(new View.OnClickListener() {
	    	public void onClick(View v) {
	    		Intent intent = new Intent(getBaseContext(), DisplayPersonActivity.class);
	           	intent.putExtra("person_directory",MY_PROFILE_PATH);
	           	intent.putExtra("editable", true);
	           	startActivity(intent);  
	    	}
	    });
	    
	  //listen for people button click
	    Button people_button = (Button) findViewById(R.id.button2);
	    people_button.setOnClickListener(new View.OnClickListener() {
	    	public void onClick(View v) {
	    		
			    //launch intent
	    		Intent intent = new Intent(getBaseContext(), DisplayPeopleActivity.class);
	           	intent.putExtra("people_directory",PEOPLE_PATH);
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
//	        case R.id.menu_nfc:
//	            launchNFCActivity();
//	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
//	public void launchNFCActivity()
//	{
//		   Intent intent = new Intent(this, NFCActivity.class);
//		   startActivity(intent);
//	}
	
	//send  basic notification
	public void sendBasicNotification(String title,String message) {
		
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.nfc_icon)
		        .setContentTitle(title)
		        .setContentText(message);
	
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, MainActivity.class);
		
		mBuilder.setContentIntent(PendingIntent.getActivity(this,0,resultIntent,0));
		mBuilder.setAutoCancel(true);
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(13, mBuilder.build());
	}
	
	//send notification
	public void sendNotification(String title,String message, Intent intent) {
			
			NotificationCompat.Builder mBuilder =
			        new NotificationCompat.Builder(this)
			        .setSmallIcon(R.drawable.nfc_icon)
			        .setContentTitle(title)
			        .setContentText(message);
		
			// Creates an explicit intent for an Activity in your app
			//Intent resultIntent = new Intent(this, MainActivity.class);
			
			mBuilder.setContentIntent(PendingIntent.getActivity(this,0,intent,0));
			mBuilder.setAutoCancel(true);
			NotificationManager mNotificationManager =
			    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			// mId allows you to update the notification later on.
			mNotificationManager.notify(13, mBuilder.build());
		}
	
	
	//addNewPerson
	//assuming given directory containing all of the person's files
	public void addPerson(String given_dir_path){
		
		//make sure given_dir is a directory
		File given_dir = new File(given_dir_path);
		if(!given_dir.exists() || !given_dir.isDirectory()){
			Toast.makeText(getApplicationContext(), "No such directory does not exist", Toast.LENGTH_SHORT).show();
			return;
		}
		
		//if there is a person object, get it
		File person_file = new File(given_dir_path + File.separator + DisplayPersonActivity.PERSON_FILE_NAME);
		if(!person_file.exists()){
			Toast.makeText(getApplicationContext(), "No person file given", Toast.LENGTH_SHORT).show();
			return;
		}
		Person person;
		try{
			FileInputStream fin = new FileInputStream(person_file);
			ObjectInputStream oin = new ObjectInputStream(fin);
	    	person = (Person) oin.readObject();
		}catch(Exception e){
			Toast.makeText(getApplicationContext(), "Error reading person object", Toast.LENGTH_SHORT).show();
			return;
		}
		
		//new directory
		String new_dir_path = MainActivity.PEOPLE_PATH + File.separator + person.getName();
		File new_dir = new File(new_dir_path); 
		
		//handle conflicts
		for(int i = 1; new_dir.exists();i++){
			new_dir = new File(new_dir_path + Integer.toString(i));
		}
		
		//move directory
		if(given_dir.renameTo(new_dir)){
			Toast.makeText(getApplicationContext(), "Successfully added " + person.getName(), Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(getApplicationContext(), "Error adding person", Toast.LENGTH_SHORT).show();
		}
		
		//start activity
		Intent intent = new Intent(getBaseContext(), DisplayPersonActivity.class);
       	intent.putExtra("person_directory",new_dir_path);
       	intent.putExtra("editable", false);
       	startActivity(intent);
		
		//add notification
		sendNotification("Added " + person.getName(),"Click here to view",intent);
		
		
	}
	
}
