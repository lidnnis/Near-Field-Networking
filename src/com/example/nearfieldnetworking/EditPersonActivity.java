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
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

/*************************************************************************
*	EditPersonActivity
*		called to edit a person
*
*************************************************************************/

public class EditPersonActivity extends Activity {

	//private variables
	private String person_path = "";
	private boolean editable;
	private Person person = new Person("");
	ArrayList<File> categories;
	ArrayList<ArrayList<File>> subcategories;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_person);
        
        //fetch data from intent
        Bundle extras = getIntent().getExtras(); 
		if(extras !=null)
    	{
			person_path = extras.getString("person_directory");
    	}
		
		//make sure person_directory exists
		File person_dir = new File(person_path);
		if(!person_dir.exists() || !person_dir.isDirectory()){
			Toast.makeText(getApplicationContext(), "No person directory", Toast.LENGTH_SHORT).show();
			finish();
		}
		
		//open person's object file
		final File person_file = new File(person_path + "/.person");
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
		final EditText name_text = (EditText) findViewById(R.id.textView2);
		name_text.setText(person.getName());
		
		final EditText email_text = (EditText) findViewById(R.id.TextView1);
		email_text.setText(person.getEmailAddress());
		
		final EditText phone_text = (EditText) findViewById(R.id.TextView03);
		phone_text.setText(person.getPhoneNumber());
		
		//if can edit, set on click listener
		Button save_button = (Button) findViewById(R.id.button1);
		save_button.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	person.setName(name_text.getText().toString());
		    	person.setEmail(email_text.getText().toString());
		    	person.setPhoneNumber(phone_text.getText().toString());
		    	if (person.writeToFile(person_file))
		    		Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
		    	else
		    		Toast.makeText(getApplicationContext(), "Could not Save", Toast.LENGTH_SHORT).show();
		    }
		});;

		
		//load list
		loadList();
		
        
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
    	
    	//if select file
    	if(resultCode == 37){
    		Bundle extras = data.getExtras(); 
        	if(extras !=null)
        	   {
        		String file_name = data.getStringExtra("file");		
        		try {
					copyFile(categories.get(requestCode).getPath(), file_name);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	 }
    	}
    	
    	
    	loadList();
    	
    }
    
    //load list
    private void loadList(){
    	
    	//expandable list view
    	ExpandableListView list_view = (ExpandableListView) findViewById(R.id.expandableListView1);
    	        
    	//add all directories to categories
    	categories = new ArrayList<File>();
    	File person_dir = new File(person_path);
    	File[] files = person_dir.listFiles();
    	for(int i = 0; i < files.length;i++){
    		if(files[i].isDirectory()){
    			categories.add(files[i]);
    		}
    	}
    			
    	//add all subfiles to subcategories
    	subcategories = new ArrayList<ArrayList<File>>();
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
    	 		
    			
    	FileExpandableListAdapter adapter = new FileExpandableListAdapter(this, categories,subcategories,true);
 
    	// Set this blank adapter to the list view
    	list_view.setAdapter(adapter);
    	        
    	 //expand groups to start
    	 for(int i = 0; i < adapter.getGroupCount();i++){
    		 list_view.expandGroup(i);
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
