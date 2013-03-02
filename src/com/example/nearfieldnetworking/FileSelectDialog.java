package com.example.nearfieldnetworking;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.ExpandableListView;

public class FileSelectDialog extends DialogFragment {
	public final ArrayList<Uri> uris = new ArrayList<Uri>();
	private String path;
	
	
	public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
    
    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;
    
    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
	
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		path=getArguments().getString("passPath");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        //Toast.makeText(getActivity(), path, Toast.LENGTH_SHORT).show();
        
        //Set title of dialog box
        builder.setTitle(R.string.dialog_title);
       
        //Build list of files -- From Jamie's code
        ExpandableListView list_view = new ExpandableListView(getActivity());
        //add all directories to categories
        ArrayList<File> categories = new ArrayList<File>();
		File person_dir = new File(path);
		File[] files = person_dir.listFiles();
		for(int i = 0; i < files.length;i++){
			if(files[i].isDirectory()){
				categories.add(files[i]);
			}
		}
			
		final ArrayList<String> filePaths = new ArrayList<String>();
		ArrayList<String> fileNames = new ArrayList<String>();

		//add all subfiles to subcategories
		ArrayList<ArrayList<File>> subcategories = new ArrayList<ArrayList<File>>();
		for(int i = 0;i < categories.size();i++){
			ArrayList<File> sub_list = new ArrayList<File>();
			subcategories.add(sub_list);
			File[] sub_files = categories.get(i).listFiles();
			for(int j = 0; j  < sub_files.length;j++){
				if(!sub_files[j].isDirectory()){
					sub_list.add(sub_files[j]);
					filePaths.add(sub_files[j].getPath());
					fileNames.add(sub_files[j].getName());
				}
			}
		}
		
		
		final String[] check_list = fileNames.toArray(new String[fileNames.size()]);
		
		//CheckableFileExpandableList adapter = new CheckableFileExpandableList(getActivity(), categories,subcategories,false);

		// Set this blank adapter to the list view
	    //list_view.setAdapter(adapter);
		
		//final String[] dialog_options = subcategories.toArray(new String[subcategories.size()]);
        
		//Toast.makeText(getActivity(), dialog_options, Toast.LENGTH_SHORT).show();
		//Tracks selected items
		
		builder.setView(list_view);
        
		builder.setMultiChoiceItems(check_list, null, new DialogInterface.OnMultiChoiceClickListener() 
        		{
        			@Override
        			public void onClick(DialogInterface dialog, int which, boolean isChecked)
        			{
        				if (isChecked) 
        				{
        					// If user selects, add item to selected items
        					uris.add(Uri.fromFile(new File(filePaths.get(which))));
        				}
        				else if(uris.contains(which))
        				{
        					uris.remove(Uri.fromFile(new File(filePaths.get(which))));
        				}
        			}
        		});
        		
        		//Buttom buttons
        	
               builder.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // Send Files
                	   mListener.onDialogPositiveClick(FileSelectDialog.this);
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                	   mListener.onDialogNegativeClick(FileSelectDialog.this);
                   }
               } );
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
