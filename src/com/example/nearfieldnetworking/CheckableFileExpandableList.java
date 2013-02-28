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
		private ArrayList<ArrayList<File>> files;
		private ArrayList<ArrayList<CheckableFile>> children;
		private LayoutInflater inflater;
		
		//constructor
		CheckableFileExpandableList(Context context,ArrayList<File> groups,ArrayList<ArrayList<File>> files){
			this.context = context;
			this.groups = groups;
			this.files = files;
			
			//Create a double nested Arraylist of type CheckableFiles rather than File
			ArrayList<ArrayList<CheckableFile>> childrenTemp = new ArrayList<ArrayList<CheckableFile>>();
			for(int i =0; i<files.size(); i++)
			{
				ArrayList<CheckableFile> temp = new ArrayList<CheckableFile>();
				for(int j=0; j< files.get(i).size(); j++)
				{
					CheckableFile c = new CheckableFile(files.get(i).get(j),false);
					temp.add(c);
				}
				childrenTemp.add(temp);
			}
			
			this.children = childrenTemp;
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

		@Override
		public View getChildView(final int arg0, final int arg1, boolean arg2, View arg3,
				ViewGroup arg4) {
			// TODO Auto-generated method stub
			View t =null;
			if(arg3 != null)
				t=arg3;
			else
				t=inflater.inflate(R.layout.checkable_row,arg4,false);
			CheckableFile c = (CheckableFile)getChild(arg0,arg1);
			TextView fileName = (TextView)t.findViewById(R.id.label);
			if(fileName!=null)
			{
			fileName.setPadding(100, 0, 0, 0);
			fileName.setTextSize(20);
				//View File
			fileName.setText((CharSequence) ((CheckableFile)getChild(arg0,arg1)).getFile().getName());
			}
			CheckBox cb = (CheckBox)t.findViewById( R.id.checkBox1 );
		    cb.setChecked( c.getState() );
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