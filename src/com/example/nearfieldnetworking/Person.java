package com.example.nearfieldnetworking;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


/*
 * Person Class Contains Basic Information about a Person
 * Name should be unique
 */
public class Person implements Serializable {
	
	//private variables
	private String name;
	private String email_address;
	private String phone_number;
	
	//constructor
	Person(String name){
		this.name = name;
	}
	
	//set name
	public boolean setName(String name){
		this.name = name;
		return true;
	}
	
	//set email
	public boolean setEmail(String email_address){
		this.email_address = email_address;
		return true;
	}

	//set phone number
	public boolean setPhoneNumber(String phone_number){
		this.phone_number = phone_number;
		return true;
	}
	
	//get name
	public String getName(){
		return this.name;
	}
	
	//get email_address
	public String getEmailAddress(){
		return this.email_address;
	}
	
	//get phone_number
	public String getPhoneNumber(){
		return this.phone_number;
	}
	
	//write to file
	public boolean writeToFile(File person_file){
		try {
			FileOutputStream fout = new FileOutputStream(person_file);
			ObjectOutputStream oout = new ObjectOutputStream(fout);
	    	oout.writeObject(this);
	    	oout.flush();
	    	oout.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

}