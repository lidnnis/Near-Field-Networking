package com.example.nearfieldnetworking;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;


import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DisplayPersonActivity extends Activity {

	private String person_directory = "";
	private boolean editable;
	private Person person = new Person("");
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_person);
        
        //fetch data from intent
        Bundle extras = getIntent().getExtras(); 
		if(extras !=null)
    	{
			person_directory = extras.getString("person_directory");
			editable = extras.getBoolean("editable", false);
    	}
		
		//open person's object file
		File person_file = new File(person_directory + "/.person");
		if(!person_file.exists()){
			Toast.makeText(getApplicationContext(), "No person file", Toast.LENGTH_SHORT).show();
			finish();
		}else{
			try{
				FileInputStream fin = new FileInputStream(person_file);
				ObjectInputStream oin = new ObjectInputStream(fin);
		    	person = (Person) oin.readObject();
			}catch(Exception e){
				Toast.makeText(getApplicationContext(), "Error reading person object", Toast.LENGTH_SHORT).show();
				finish();
			}
		}
		
		
		//set values layout
		TextView name_text = (TextView) findViewById(R.id.textView2);
		name_text.setText(person.getName());
		
		TextView email_text = (TextView) findViewById(R.id.TextView1);
		email_text.setText(person.getEmailAddress());
		
		TextView phone_text = (TextView) findViewById(R.id.TextView03);
		phone_text.setText(person.getPhoneNumber());
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    //upon return from activity with file
    protected void onActivityResult (int requestCode, int resultCode, Intent data){
    	//act according to request code
    	
    }
    
    
    
    
}
