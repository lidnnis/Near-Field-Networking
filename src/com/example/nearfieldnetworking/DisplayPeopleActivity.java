package com.example.nearfieldnetworking;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


//********************************************************\
// DisplayPeopleActivity
//		Displays people in list format
//********************************************************
public class DisplayPeopleActivity extends ListActivity {

	//private variables
	private List<String> item = null;
	private List<String> path = null;
	private TextView num_contacts;
	private String people_path = "/";
 

    //when activity is created
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_people);
        num_contacts = (TextView)findViewById(R.id.path);
        
        //fetch data from the intent
        Bundle extras = getIntent().getExtras(); 
        if(extras !=null)
    	{
			people_path = extras.getString("people_directory");
    	}
        
        //check to make sure path is a directory and can be read
        File people_dir = new File(people_path);
        if(people_dir.isDirectory() && !people_dir.canRead()){
        	Toast.makeText(getApplicationContext(), "Invalid people directory", Toast.LENGTH_SHORT).show();
			finish();
        }
        
        //create list
        setList(people_path);
    }

    //check to make sure file is valid
    private boolean checkFile(File f){
    	
    	//return true only if is directory and directory contains .person file
    	if(f.isDirectory()){
    		String person_object_path = f.getPath() + "/.person";
    		File person_object_file = new File(person_object_path);
    		if(person_object_file.exists()){
    			return true;
    		}
    	}
    	
    	//else
    	return false;
    }
    
    //set list to current location
    private void setList(String dirPath)
    {
    	
    	item = new ArrayList<String>();
    	path = new ArrayList<String>();

    	//get this file and subfiles
    	File f = new File(dirPath);
    	File[] files = f.listFiles();
    	Arrays.sort(files);


    	//all subfiles
    	for(int i=0; i < files.length; i++)
    	{	
    		File file = files[i];
    		if(checkFile(file)){    			
    			path.add(file.getPath());
    			item.add(file.getName());
    		}
    	}

    	
    	//set index
    	num_contacts.setText(item.size() + " People");
    	
    	//list files
    	//ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, R.layout.row, item); 
    	//setListAdapter(fileList);
    	setListAdapter(new FileArrayAdapter(this, item));
    }



    //when clicked, respond appropriately
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

    	Intent intent = new Intent(getBaseContext(), DisplayPersonActivity.class);
       	intent.putExtra("person_directory",path.get(position));
       	intent.putExtra("editable", false);
       	startActivity(intent);

    }

    
    
    //private adapter class
    private class FileArrayAdapter extends ArrayAdapter<String> {
    	private final Context context;
    	private final List <String> values;
     
    	//initialize array adappter
    	public FileArrayAdapter(Context context, List<String> values) {
    		super(context, R.layout.person_row, values);
    		this.context = context;
    		this.values = values;
    	}
     
    	//get view
    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		LayoutInflater inflater = (LayoutInflater) context
    			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     
    		//get fields
    		View rowView = inflater.inflate(R.layout.row, parent, false);
    		TextView textView = (TextView) rowView.findViewById(R.id.label);
    		ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);
    		textView.setText(values.get(position));
     
    		// Set icon depending on file type
    		String s = values.get(position);
     
    		System.out.println(s);
     
    		imageView.setImageResource(R.drawable.file_small);
    		     
    		//return the view
    		return rowView;
    	}
    }
    
    
}
