package com.guojin.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.R.string;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Parcel;
import android.os.SystemClock;

public class FileStore {
	final String tag = "FileProcessor";

	static String appDir =Environment.getExternalStorageDirectory()+"/white_board/";
	public static String picDir = appDir+"pic/";
	static {
		File f=new File(appDir);
		if (!f.exists()) {
			f.mkdir();
		}
		f=new File(picDir);
		if (!f.exists()) {
			f.mkdir();
		}
	}

	public static String storeFile(String srcFilePath) {
		String filename=picDir+srcFilePath.substring(srcFilePath.lastIndexOf("/")+1);
		File srcFile = new File(srcFilePath);
		File destFile = new File(filename);
		int byteread = 0;
		InputStream in = null;
		OutputStream out = null;

		try {
			in = new FileInputStream(srcFile);
			out = new FileOutputStream(destFile);
			byte[] buffer = new byte[1024];

			while ((byteread = in.read(buffer)) != -1) {
				out.write(buffer, 0, byteread);
			}
			return filename;
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		} finally {
			try {
				if (out != null)
					out.close();
				if (in != null)
					in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
    public static String  storeFile(Bitmap b) throws IOException {  
    	String filename=picDir+SystemClock.currentThreadTimeMillis()+".jpg";
        File f = new File(filename);  
        f.createNewFile();  
        FileOutputStream fOut = null;  
        try {  
                fOut = new FileOutputStream(f);  
        } catch (FileNotFoundException e) {  
                e.printStackTrace();  
        }  
        b.compress(Bitmap.CompressFormat.JPEG, 100, fOut);  
        try {  
                fOut.flush();  
        } catch (IOException e) {  
                e.printStackTrace();  
        }  
        try {  
                fOut.close();  
        } catch (IOException e) {  
                e.printStackTrace();  
        }  
        return filename;
    }  
}
