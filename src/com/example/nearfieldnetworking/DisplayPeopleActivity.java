package com.example.nearfieldnetworking;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
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
        
       	ActionBar actionBar = getActionBar();
	actionBar.setDisplayHomeAsUpEnabled(true);
	
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
    	setListAdapter(new FileArrayAdapter(this, item,path));
    	
    	ListView lv = getListView();
    	lv.setOnItemLongClickListener( new AdapterView.OnItemLongClickListener
    			(){
    		@Override
    		public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id){
    			onLongListItemClick(v, pos,id);
    			return false;
    		}
    		
    	});
    }

    protected void onLongListItemClick(View v, int pos, long id){

    	AlertDialog diaBox = AskOption((path.get(pos)));
    	diaBox.show();
    	
    }

    public AlertDialog AskOption(final String path)
    {
       AlertDialog myQuittingDialogBox = new AlertDialog.Builder(this) 
           //set message, title, and icon
           .setTitle("Delete") 
           .setMessage("Are you sure you want to delete?") 
           //.setIcon(R.drawable.ic_delete)
           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int whichButton) { 
            	   File dir = new File(path);
            	   DeleteDir.deleteDirectory(dir);
            	   dialog.dismiss();  
            	   finish();
       	       		startActivity(getIntent());
               }      
           })
           .setNegativeButton("No", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int which) {
                   dialog.dismiss();
               }
           })   
           .create();
           return myQuittingDialogBox;
       }
    
    //when clicked, respond appropriately
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

    	Intent intent = new Intent(getBaseContext(), DisplayPersonActivity.class);
       	intent.putExtra("person_directory",path.get(position));
       	intent.putExtra("editable", false);
       	startActivity(intent);

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
    
    //private adapter class
    private class FileArrayAdapter extends ArrayAdapter<String> {
    	private final Context context;
    	private final List <String> values;
    	private final List <String> paths;
     
    	//initialize array adapter
    	public FileArrayAdapter(Context context, List<String> values,List<String> paths) {
    		super(context, R.layout.person_row, values);
    		this.context = context;
    		this.values = values;
    		this.paths = paths;
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
    		
    		//set text
    		textView.setText(values.get(position));
     
    		//set image
        	File image_file = new File(paths.get(position) + File.separator + DisplayPersonActivity.PROFILE_PIC_FILE_NAME);
        	//if such a file exists, try to load person
        	if(image_file.exists()){
        		try{
        			
        			BitmapFactory.Options options=new BitmapFactory.Options();
        			options.inSampleSize = 8;
        			Bitmap image_bitmap = BitmapFactory.decodeFile(image_file.getAbsolutePath(),options);
        			imageView.setImageBitmap(image_bitmap);
        			
        		}catch(Exception e){
        			Toast.makeText(getApplicationContext(), "Could not map image", Toast.LENGTH_SHORT).show();
        		}
        	}
    	
    		     
    		//return the view
    		return rowView;
    	}
    }
    
    
}


//fxn to delete recursively
class DeleteDir {
	  public static void main(String args[]) {
	    deleteDirectory(new File(args[0]));
	  }

	  static public boolean deleteDirectory(File path) {
	    if( path.exists() ) {
	      File[] files = path.listFiles();
	      for(int i=0; i<files.length; i++) {
	         if(files[i].isDirectory()) {
	           deleteDirectory(files[i]);
	         }
	         else {
	           files[i].delete();
	         }
	      }
	    }
	    return( path.delete() );
   }
}
