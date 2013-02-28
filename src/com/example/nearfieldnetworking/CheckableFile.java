package com.example.nearfieldnetworking;

import java.io.File;

public class CheckableFile {
    public File file = null;
    public boolean state = false;

    public CheckableFile( File file, boolean state ) {
        this.file = file;
        this.state = state;
    }

    public File getFile() {
	    return file;
    }

    public boolean getState() {
	    return state;
    }
    

}