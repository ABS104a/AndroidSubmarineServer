package com.abs104a.com.client.androidsubmarine.httpservice;

import android.content.res.AssetManager;

import java.io.IOException;

import http.NanoHTTPD;


public class GuardHTTPD extends NanoHTTPD  {
	
	
	public GuardHTTPD(int port, AssetManager am) throws IOException {
		super(port, am);
		
	}
	
	
	@Override
	protected void finalize() throws Throwable {
		
		super.finalize();
	}

}
