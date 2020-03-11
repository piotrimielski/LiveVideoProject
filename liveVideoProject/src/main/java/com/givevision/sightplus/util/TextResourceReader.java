package com.givevision.sightplus.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class TextResourceReader {
	public static String readTextFileFromResource(Context context,int resourceId) {
		StringBuilder body = new StringBuilder();
		try {
			InputStream inputStream =context.getResources().openRawResource(resourceId);
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String nextLine;
			while ((nextLine = bufferedReader.readLine()) != null) {
				body.append(nextLine);
				body.append('\n');
			}

			if(bufferedReader!=null)
				bufferedReader.close();
			if(inputStreamReader!=null)
				inputStreamReader.close();
			if(inputStream!=null)
				inputStream.close();

		} catch (IOException e) {
			throw new RuntimeException("Could not open resource: " + resourceId, e);
		} catch (Resources.NotFoundException nfe) {
			throw new RuntimeException("Resource not found: " + resourceId, nfe);
		}
		return body.toString();
	}

	public List<String> readLineInFile(String path) {
		Log.d("debug", "TextResourceReader :: readLineInFile path= " + path);
		List<String> mLines = new ArrayList<>();
		try {
			// open the file for reading
			InputStream fis = new FileInputStream(path);
			// if file the available for reading
			if (fis != null) {
				// prepare the file for reading
				InputStreamReader chapterReader = new InputStreamReader(fis);
				BufferedReader reader = new BufferedReader(chapterReader);
				String line;
				while ((line = reader.readLine()) != null) {
					mLines.add(line);
				}
				if(reader!=null)
					reader.close();
				if(chapterReader!=null)
					chapterReader.close();
				if(fis!=null)
					fis.close();
			}
		} catch (IOException e) {
			Log.e("debug", "TextResourceReader :: readLineInFile IOException= " + e);
		}
		return mLines;
	}

	public void writeLineInFile(String path, String txt, boolean multi) {
		Log.d("debug", "TextResourceReader :: writeLineInFile path= " + path+" txt= "+txt);
		FileWriter fw = null;
		BufferedWriter bw  = null;
		PrintWriter out = null;
		try {
			if(multi){
				fw = new FileWriter(path, true);
			}else{
				fw = new FileWriter(path);
			}
			bw  = new BufferedWriter(fw);
			out = new PrintWriter(bw);
			out.println(txt);
			if(out  != null)
				out .close();
			if(bw  != null)
				bw .close();
			if(fw  != null)
				fw .close();
		} catch (IOException ex) {
			Log.e("debug", "TextResourceReader :: writeLineInFile IOException= " + ex);
		}
	}
}
