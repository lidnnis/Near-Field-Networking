package com.example.nearfieldnetworking;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.content.DialogInterface;
import android.os.Bundle;

public class FileSelectDialog extends DialogFragment {
	private String path;
	
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
		
		CheckableFileExpandableList adapter = new CheckableFileExpandableList(getActivity(), categories,subcategories);

		// Set this blank adapter to the list view
	    list_view.setAdapter(adapter);
		list_view.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
		    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		    	CheckBox cb = (CheckBox)v.findViewById( R.id.checkBox1 );
		        if( cb != null )
		            cb.toggle();
		        return false;
		    }
		});
		
//		String[] dialog_options = subcategories.toArray(new String[subcategories.size()]);
        
		//Toast.makeText(getActivity(), dialog_options, Toast.LENGTH_SHORT).show();
		//Tracks selected items
		
		builder.setView(list_view);
		
	/*	
		final List<Integer> mSelectedItems = new ArrayList<Integer>();
        
		builder.setMultiChoiceItems(dialog_options, null, new DialogInterface.OnMultiChoiceClickListener() 
        		{
        			@Override
        			public void onClick(DialogInterface dialog, int which, boolean isChecked)
        			{
        				if (isChecked) 
        				{
        					// If user selects, add item to selected items
        					mSelectedItems.add(which);
        				}
        				else if(mSelectedItems.contains(which))
        				{
        					mSelectedItems.remove(Integer.valueOf(which));
        				}
        			}
        		})
        		
        		//Buttom buttons
        	*/
               builder.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // Send Files
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
               } );
        // Create the AlertDialog object and return it
        return builder.create();
    }
	

    
}
