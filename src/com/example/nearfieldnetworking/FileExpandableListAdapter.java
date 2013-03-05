package com.example.nearfieldnetworking;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.R;

/**************************************************************************************
* class FileExpandableClassAdapter
*	An adapter for displaying a list of lists of files associated with a person 
* 
**************************************************************************************/
public class FileExpandableListAdapter extends BaseExpandableListAdapter{
	
		//private variables
		private Context context;
		private ArrayList<File> groups;
		private ArrayList<ArrayList<File>> children;
		private boolean addable;
	
		//constructor
		FileExpandableListAdapter(Context context,ArrayList<File> groups,ArrayList<ArrayList<File>> children,boolean addable){
			this.context = context;
			this.groups = groups;
			this.children = children;
			this.addable = addable;
			
			//if addable, add an extra object to click on to add a file
			if (addable){
				for(int i = 0; i < groups.size();i++){
					children.get(i).add(new File(""));
				}
			}
		}
	
		@Override
		public Object getChild(int arg0, int arg1) {
			// TODO Auto-generated method stub
			return children.get(arg0).get(arg1);
		}

		@Override
		public long getChildId(int arg0, int arg1) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getChildView(final int arg0, final int arg1, boolean arg2, View arg3,
				ViewGroup arg4) {
			// TODO Auto-generated method stub
			TextView t = new TextView(context);
			t.setPadding(100, 0, 0, 0);
			t.setTextSize(20);
			
			if(addable && arg1 == getChildrenCount(arg0)-1){
				//add link to choose file
				t.setText("Add File");
				t.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						try
						{
							Intent intent = new Intent(context, SelectFileActivity.class);
			            	((Activity) context).startActivityForResult(intent,EditPersonActivity.ADD_FILE + arg0);
						}
						catch (ActivityNotFoundException e)
						{
							Toast.makeText(context, "Could not open select file activity", Toast.LENGTH_SHORT).show();
						}
					}
				});
				
			}else{
				//View File
				t.setText((CharSequence) ((File) getChild(arg0,arg1)).getName());
				t.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						try
						{
							Intent intentUrl = new Intent(Intent.ACTION_VIEW);
							intentUrl.setDataAndType(Uri.fromFile((File) getChild(arg0,arg1)), "application/*");
							intentUrl.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							context.startActivity(intentUrl);
						}
						catch (ActivityNotFoundException e)
						{
							Toast.makeText(context, "No PDF Viewer Installed", Toast.LENGTH_SHORT).show();
						}
					}
				});
				
				//Delete File on long click
				t.setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						//Toast.makeText(context, "Long Click Detected", Toast.LENGTH_SHORT).show();
						AlertDialog diaBox = AskOption((CharSequence) ((File) getChild(arg0,arg1)).getName(), ((File) getChild(arg0,arg1)).getPath());
				        	diaBox.show();
						return false;
					}
				});
				
			}
			return t;
		}

		@Override
		public int getChildrenCount(int arg0) {
			// TODO Auto-generated method stub
			return children.get(arg0).size();
		}

		@Override
		public Object getGroup(int arg0) {
			// TODO Auto-generated method stub
			return groups.get(arg0);
		}

		@Override
		public int getGroupCount() {
			// TODO Auto-generated method stub
			return groups.size();
		}

		@Override
		public long getGroupId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getGroupView(int arg0, boolean arg1, View arg2,
				ViewGroup arg3) {
			// TODO Auto-generated method stub
			TextView t = new TextView(context);
			t.setText((CharSequence) ((File) getGroup(arg0)).getName());
			t.setPadding(50, 0, 0, 0);
			t.setTextSize(30);
			return t;
		}

		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isChildSelectable(int arg0, int arg1) {
			// TODO Auto-generated method stub
			return false;
		}
		
		
	    /*****************************************************************************
	     * 
	     * Private function AskOption opens a dialog to confirm delete on long click.
	     * 
	    *****************************************************************************/
	    private AlertDialog AskOption(CharSequence name, final String selectedFilePath)
	    {
	       AlertDialog myQuittingDialogBox = new AlertDialog.Builder(context) 
	           //set message, title, and icon
	           .setTitle("Delete") 
	           .setMessage("Are you sure you want to delete " + name + "?") 
	           .setIcon(R.drawable.ic_delete)
	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int whichButton) { 
	            	   File file = new File(selectedFilePath);
	            	   //add confirmations about deleting
	                   boolean deleted = file.delete();
	                   /*if(deleted == true)
	                   {
	             		//DisplayPersonActivity.loadList();
	                	//Intent reload = new Intent(context, DisplayPersonActivity.class);
	                  	//context.startActivity(reload);
	                   }
	                   else
	                   	//Error Message
	                   */
	                 
	            	   dialog.dismiss();
	            	   ((Activity)context).finish();
	            	   ((Activity)context).startActivity(((Activity)context).getIntent());
	            	   //((Activity) context).onRestart();
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
	    
	    
	    
	    
		
}
