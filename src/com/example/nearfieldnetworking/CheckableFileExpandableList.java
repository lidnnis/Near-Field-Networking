package com.example.nearfieldnetworking;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

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
		
		//constructor
		CheckableFileExpandableList(Context context,ArrayList<File> groups,ArrayList<ArrayList<File>> children,boolean addable){
			this.context = context;
			this.groups = groups;
			this.children = children;


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
				//View File
				t.setText((CharSequence) ((File) getChild(arg0,arg1)).getName());	
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
			return false;
		}

}