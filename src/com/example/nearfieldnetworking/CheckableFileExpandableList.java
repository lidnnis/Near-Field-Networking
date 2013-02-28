package com.example.nearfieldnetworking;

import java.io.File;
import java.util.ArrayList;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

/**************************************************************************************
* class FileExpandableClassAdapter
*	An adapter for displaying a list of lists of files associated with a person 
* 
**************************************************************************************/
public class CheckableFileExpandableList extends BaseExpandableListAdapter{

		//private variables
		private Context context;
		private ArrayList<File> groups;
		private ArrayList<ArrayList<File>> children;
		private ArrayList<ArrayList<Boolean>> checkBoxValues;
		private LayoutInflater inflater;
		
		//constructor
		CheckableFileExpandableList(Context context,ArrayList<File> groups,ArrayList<ArrayList<File>> children,ArrayList<ArrayList<Boolean>> defaultCheckedValues){
			this.context = context;
			this.groups = groups;		
			this.children = children;
			this.checkBoxValues = defaultCheckedValues;
			inflater = LayoutInflater.from(context);
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
		

		public Boolean getStatus(int arg0, int arg1) {
			return checkBoxValues.get(arg0).get(arg1);	
		}
		
		public void changeStatus(int arg0, int arg1) {
			Boolean curr = checkBoxValues.get(arg0).get(arg1);	
			ArrayList<Boolean> temp=checkBoxValues.get(arg0);
			temp.set(arg1, !curr);
			checkBoxValues.set(arg0,temp);
		}

		@Override
		public View getChildView(final int arg0, final int arg1, boolean arg2, View arg3,
				ViewGroup arg4) {
			// TODO Auto-generated method stub
			View t =null;
			if(arg3 != null)
				t=arg3;
			else
				t=inflater.inflate(R.layout.checkable_row,arg4,false);
			//CheckableFile c = (File)getChild(arg0,arg1);
			TextView fileName = (TextView)t.findViewById(R.id.label);
			if(fileName!=null)
			{
			fileName.setPadding(100, 0, 0, 0);
			fileName.setTextSize(20);
				//View File
			fileName.setText((CharSequence) ((File)getChild(arg0,arg1)).getName());
			}
			CheckBox cb = (CheckBox)t.findViewById( R.id.checkBox1 );
			cb.setChecked(getStatus(arg0,arg1));
			cb.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					changeStatus(arg0,arg1);
					// TODO Auto-generated method stub
//					Toast.makeText(context, Integer.toString(checkBoxValues.get(arg0).size()), Toast.LENGTH_SHORT).show();
				}
				
			});
		    //cb.setChecked( c.getState() );
			return t;}
		
		
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
			return true;
		}

}