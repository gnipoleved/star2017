package umojan.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class FileUtil {
	
	private static final int MAX_TEXT_FILE_ROWS = 65535;
	
	public static void appendTextToFile(File file, String text) throws IOException {
		writeTextToFile(file, text, true);		
	}

	public static void writeTextToFile(File file, String text) throws IOException {
		writeTextToFile(file, text, false);
	}
	
	public static ArrayList<String> readTextFromFile(File file) throws IOException {
		ArrayList<String> list = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			int cnt = 0;
			while ((line = reader.readLine()) != null) {
				if (cnt ++ >= MAX_TEXT_FILE_ROWS) throw new IOException("Too many text rows read.");
				list.add(line);
			}
			
		} finally {
			try	{	reader.close();	}	catch (Exception e) {};
		}
		return list;
	}
	
	
	public static void writeObjectToFile(File file, Object obj) throws IOException  {
		ObjectOutputStream os = null;
		try {
			os = new ObjectOutputStream(new FileOutputStream(file));
			os.writeObject(obj);
		} finally {
			try{	os.close();	}	catch (Exception e) {};
		}
	}
	
	public static Object readObjectFromFile(String filename) throws IOException, ClassNotFoundException {
		return readObjectFromFile(new File(filename));
	}
	
	public static Object readObjectFromFile(File file) throws IOException, ClassNotFoundException {
		ObjectInputStream os = null;
		Object obj = null;
		try {
			os = new ObjectInputStream(new FileInputStream(file));
			obj = os.readObject();
		} finally {
			try {	os.close();	}	catch (Exception e) {};
		}
		return obj;
		
	}

	
	public static boolean existsFilenameWithIncludeStringAndExtensionInDirectory(String directory, String includeStr, String extension) throws IOException {
		File[] files = dir(directory, includeStr, extension);
		return (files.length > 0);
	}

	public static File[] dir(String directoryName, String includeStr, String extension) throws IOException {
		final String innerIncludeStr = includeStr;
		final String innerExtension = extension;
		File directory = new File(directoryName);
		if (directory.isDirectory()==false) throw new IOException("Not a directory : " + directoryName);
		File files[] = directory.listFiles(new FileFilter(){
											@Override
											public boolean accept(File pathname) {
												return ((pathname.getName().indexOf(innerIncludeStr)) >= 0 && pathname.getName().endsWith(innerExtension));
										}

									 }
									 );
		return files;
	}

	public static boolean moveFile(String src, String dest, boolean overWritable) throws IOException {
		File srcFile = new File(src);
		File destFile = new File(dest);
		if (srcFile.exists() == false || srcFile.isFile() == false) throw new FileNotFoundException();
		if (overWritable) {
			if (destFile.exists()) destFile.delete();
		} else {
			if (destFile.exists()) throw new IOException("Can not overwrite by user option.");
		}
		return srcFile.renameTo(destFile);
	}

	
	
	private static void writeTextToFile(File file, String text, boolean append) throws IOException {
		BufferedWriter bWriter = null; 
		try {
			bWriter = new BufferedWriter(new FileWriter(file, append));
			bWriter.write(text);
		} finally {
			try {	bWriter.flush();	}	catch (Exception e) {};
			try {	bWriter.close();	}	catch (Exception e) {};
		}
	}

}
