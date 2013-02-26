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
import java.util.ArrayList;


import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

/********************************************************
 *  DisplayPersonActivity
 * 		This Activity is responsible for displaying a person
 * 		It should be passed the directory of the person
 * 		and a boolean indicating whether or the profile can be edited
 *
 *******************************************************/
public class DisplayPersonActivity extends Activity {

	static final String PROFILE_PIC_FILE_NAME = ".profile_pic.jpg";
	static final String PERSON_FILE_NAME = ".person";
	
	//private variables
	private String person_path = "";
	private boolean editable;
	private Person person = new Person("");
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_person);
        
        ActionBar actionBar = getActionBar();
	actionBar.setDisplayHomeAsUpEnabled(true);
        
        //fetch data from intent
        Bundle extras = getIntent().getExtras(); 
		if(extras !=null)
    	{
			person_path = extras.getString("person_directory");
			editable = extras.getBoolean("editable", false);
    	}
		
		//make sure person_directory exists
		File person_dir = new File(person_path);
		if(!person_dir.exists() || !person_dir.isDirectory()){
			Toast.makeText(getApplicationContext(), "No person directory", Toast.LENGTH_SHORT).show();
			finish();
		}
	
		//if can edit, set on click listener
		Button edit_button = (Button) findViewById(R.id.button1);
		if (editable){
			edit_button.setOnClickListener(new View.OnClickListener() {
		    	public void onClick(View v) {
		    		Intent intent = new Intent(getBaseContext(), EditPersonActivity.class);
		           	intent.putExtra("person_directory",person_path);
		           	startActivityForResult(intent,0);  
		    	}
		    });;
		}else{
			//else hide the button
			edit_button.setVisibility(View.GONE);
		}
			
		//load image profile image from file
		loadImage();
		
		//load person from file
		loadPerson();
		
		//load list of files associated with the person
		loadList();
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
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    //upon return from activity with file
    protected void onActivityResult (int requestCode, int resultCode, Intent data){
    	//act according to request code
    	
    	//refresh the person and files (they may have changed after editing)
    	loadPerson();
    	loadList();
    	loadImage();
    }
    
    //load image from file
    private void loadImage(){
    	
    	//get image view
    	ImageView image_view = (ImageView) findViewById(R.id.image1);
    	
    	//file where picture is stored
    	File image_file = new File(person_path + File.separator + PROFILE_PIC_FILE_NAME);
    	//if such a file exists, try to load person
    	if(image_file.exists()){
    		try{
    			BitmapFactory.Options options=new BitmapFactory.Options();
    			options.inSampleSize = 8;
    			Bitmap image_bitmap = BitmapFactory.decodeFile(image_file.getAbsolutePath(),options);
    			image_view.setImageBitmap(image_bitmap);
    			
    		}catch(Exception e){
    			Toast.makeText(getApplicationContext(), "Could not map image", Toast.LENGTH_SHORT).show();
    		}
    	}
    			
    			
    	//set values layout
    	TextView name_text = (TextView) findViewById(R.id.textView2);
    	name_text.setText(person.getName());
    	
    }
    
    
    
    //load person from file
    private void loadPerson(){
    	
    	//open person's object file
    	File person_file = new File(person_path + File.separator + PERSON_FILE_NAME);
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
    
    //load list
    private void loadList(){
    	
    		//expandable list view
    		ExpandableListView list_view = (ExpandableListView) findViewById(R.id.expandableListView1);
    	        
    		//add all directories to categories
    		ArrayList<File> categories = new ArrayList<File>();
    		File person_dir = new File(person_path);
    		File[] files = person_dir.listFiles();
    		for(int i = 0; i < files.length;i++){
    			if(files[i].isDirectory()){
    				categories.add(files[i]);
    			}
    		}
    			
    		//add all subfiles to subcategories
    		ArrayList<ArrayList<File>> subcategories = new ArrayList<ArrayList<File>>();
    		for(int i = 0;i < categories.size();i++){
    			ArrayList<File> sub_list = new ArrayList<File>();
    			subcategories.add(sub_list);
    			File[] sub_files = categories.get(i).listFiles();
    			for(int j = 0; j  < sub_files.length;j++){
    				if(!sub_files[j].isDirectory()){
    					sub_list.add(sub_files[j]);
    				}
    			}
    		}
    			
    			
    		FileExpandableListAdapter adapter = new FileExpandableListAdapter(this, categories,subcategories,false);

    		// Set this blank adapter to the list view
    	    list_view.setAdapter(adapter);
    	        
    	     //expand groups to start
    	     for(int i = 0; i < adapter.getGroupCount();i++){
    	    	 list_view.expandGroup(i);
    	      }
    	
    }
}
