package com.example.nearfieldnetworking;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MyProfile extends Activity {

	private String main_dir = "";
	private String my_profile_path = "";
	private String resume_path = "";
	private String portfolio_path = "";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_profile);
        
        ActionBar actionBar = getActionBar();
	actionBar.setDisplayHomeAsUpEnabled(true);
	
        //fetch data from intent
        Bundle extras = getIntent().getExtras(); 
		if(extras !=null)
    	{
			main_dir = extras.getString("main_dir");
    	}
		
		//create portfolio directory if does not exist
		my_profile_path = main_dir + "/my_profile";
	    File my_profile_dir = new File(my_profile_path); 
	    if(!my_profile_dir.exists()){
	    	my_profile_dir.mkdir();
	    }
	    
	    //create resume directory if does not exist
	    resume_path = my_profile_path + "/resume";
		File resume_dir = new File(resume_path);
		if(!resume_dir.exists()){
			resume_dir.mkdir();
		}
		
		//create portfolio directory if does not exist
		portfolio_path = my_profile_path + "/portfolio";
		File portfolio_dir = new File(portfolio_path);
		if(!portfolio_dir.exists()){
			portfolio_dir.mkdir();
		}
	    	   
        //add resume file
        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent intent = new Intent(getBaseContext(), SelectFileActivity.class);
            	startActivityForResult(intent,1);
            
            }
        });
        
      //add portfolio code
        Button button2 = (Button) findViewById(R.id.Button01);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent intent = new Intent(getBaseContext(), SelectFileActivity.class);
            	startActivityForResult(intent,2);
            
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
        switch (item.getItemId()) {
        case android.R.id.home: 
            onBackPressed();
            return true;
        }
    	return super.onOptionsItemSelected(item);
    }
    
    //upon return from activity with file
    protected void onActivityResult (int requestCode, int resultCode, Intent data){
    	//act according to request code
    	switch(requestCode){
    		//get file for resume
    		case 1:
    			if(resultCode == 37){
    				Bundle extras = data.getExtras(); 
        			if(extras !=null)
        	    	{
        				String file_name = data.getStringExtra("file");		
        				try {
							copyFile(resume_path, file_name);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
        	    	}
    			}
    			break;
    		//get file for portfolio
    		case 2:
    			if(resultCode == 37){
    				Bundle extras = data.getExtras(); 
        			if(extras !=null)
        	    	{
        				String file_name = data.getStringExtra("file");
        				try {
							copyFile(portfolio_path,file_name);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
        	    	}
    			}	
    			break;
    		default:
    			break;
    	}
    }
    
    
    //copy file from one directory to another
    private void copyFile(String dest_dir_path, String src_path) throws IOException{
    	File src_file = new File(src_path);
    	File dest_file = new File(dest_dir_path + "/" + src_file.getName());
    	
    	InputStream in = new FileInputStream(src_file);
        OutputStream out = new FileOutputStream(dest_file);
        
        // Copy the bits from input stream to output stream
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    	
    }
    
}
