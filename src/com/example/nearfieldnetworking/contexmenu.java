package com.example.nearfieldnetworking;  
  
import java.io.File;
import android.app.Activity;  
import android.os.Bundle;  
import android.view.ContextMenu;  
import android.view.MenuItem;  
import android.view.View;  
import android.view.ContextMenu.ContextMenuInfo;  
import android.widget.Button;  
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class contexmenu extends Activity {  
    /** Called when the activity is first created. */  
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.display_person);  
  
        //registerForContextMenu(listView);  
    }  
  
    @Override  
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
    super.onCreateContextMenu(menu, v, menuInfo);  
        menu.setHeaderTitle("Context Menu");  
        menu.add(0, v.getId(), 0, "Open");  
        menu.add(0, v.getId(), 0, "Delete");  
    }  
  
    @Override  
    public boolean onContextItemSelected(MenuItem item) {  
        if(item.getTitle()=="Open"){open_function(item.getItemId());}  
        else if(item.getTitle()=="Delete"){delete_function(item.getItemId());}  
        else {return false;}  
    return true;  
    }  
  
    public void open_function(int id){  
        Toast.makeText(this, "Open Function called", Toast.LENGTH_SHORT).show();
        //open using filepath
    }  
    
    public void delete_function(int id){  
        Toast.makeText(this, "Delete Function called", Toast.LENGTH_SHORT).show();
        AlertDialog diaBox = AskOption();
        diaBox.show();
    }  
    
    private AlertDialog AskOption()
    {
       AlertDialog myQuittingDialogBox = new AlertDialog.Builder(this) 
           //set message, title, and icon
           .setTitle("Delete") 
           .setMessage("Are you sure you want to delete?") 
           //.setIcon(R.drawable.delete)
           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int whichButton) { 
            	   //File file = new File(selectedFilePath);
                   //boolean deleted = file.delete();
            	   dialog.dismiss();
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