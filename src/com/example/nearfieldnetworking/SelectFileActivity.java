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


//********************************************************\
// SelectFileActivity
//		Allows the user to select a file from their phone
//********************************************************
public class SelectFileActivity extends ListActivity {

	public static final int SUCCESFULLY_SELECTED_FILE = 37;
	
	//private variables
	private List<String> item = null;
	private List<String> path = null;
	private String root="/";
	private TextView myPath;
	private File start_dir = Environment.getExternalStorageDirectory();
 

    //when activity is created
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        myPath = (TextView)findViewById(R.id.path);
        
        //fetch data from the intent
        Bundle extras = getIntent().getExtras(); 
        
        //default to starting directory
        if(!start_dir.canRead())
        	start_dir = new File(root);
        getDir(start_dir.getPath());
    }

    
    //set list to current location
    private void getDir(String dirPath)
    {
    	//set index
    	myPath.setText("Index: " + dirPath);
    	item = new ArrayList<String>();
    	path = new ArrayList<String>();

    	//get this file and subfiles
    	File f = new File(dirPath);
    	File[] files = f.listFiles();
    	Arrays.sort(files);

    	//if path not root, then add links to root, parent
    	if(!dirPath.equals(root))
    	{
    		item.add(root);
    		path.add(root);
    		item.add("../");
    		path.add(f.getParent());
    	}

    	//all subfiles
    	for(int i=0; i < files.length; i++)
    	{
    		File file = files[i];
    		path.add(file.getPath());

    		//if directory
    		if(file.isDirectory())
    				item.add(file.getName() + "/");
    		//if normal file
    		else
    			item.add(file.getName());
    	}

    	//list files
    	//ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, R.layout.row, item); 
    	//setListAdapter(fileList);
    	setListAdapter(new FileArrayAdapter(this, item));
    }



    //when clicked, respond appropriately
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	final File file = new File(path.get(position));

    	//if can read file
    	if(file.canRead()){
    		if (file.isDirectory()){
    			getDir(path.get(position));
    	
    		//choose file
    		}else{
    			new AlertDialog.Builder(this)
    			.setIcon(R.drawable.file_small)
    			.setTitle("Choose [" + file.getName() + "]")
    			.setPositiveButton("OK", 
    					new DialogInterface.OnClickListener() {
    					@Override
    					public void onClick(DialogInterface dialog, int which) {
    						//Select file
    						Intent intent = new Intent();
    		    			intent.putExtra("file",file.getPath());
    		    			setResult(37, intent);
    		    			finish();
    					}
    			}).show();
    		}
    	
    	//cannot read file
    	}else{
    		new AlertDialog.Builder(this)
    		.setIcon(R.drawable.error_small)
    		.setTitle("Cannot read [" + file.getName() + "]")
    		.setPositiveButton("OK", 
    			new DialogInterface.OnClickListener() {
   				@Override
    			public void onClick(DialogInterface dialog, int which) {
   					// Do nothing, return
    			}
    		}).show();
    	}

    }

    
    
    //private adapter class
    private class FileArrayAdapter extends ArrayAdapter<String> {
    	private final Context context;
    	private final List <String> values;
     
    	//initialize array adapter
    	public FileArrayAdapter(Context context, List<String> values) {
    		super(context, R.layout.row, values);
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
     
    		//if folder
    		if (s.endsWith("/")) {
    			imageView.setImageResource(R.drawable.folder_orange_small);
    		//if file
    		} else {
    			imageView.setImageResource(R.drawable.file_small);
    		}
    		     
    		//return the view
    		return rowView;
    	}
    }
    
    
}
