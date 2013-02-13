package com.example.nearfieldnetworking;

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
	

}