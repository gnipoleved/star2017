package umojan.util;

import mybot.MyBotModule;

public class Util {

//	private static boolean FILE_LOG_MODE = false;
//	private static String logfile = "C:\\Starcraft\\logs\\umojan_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".log";
//	private static OutputStream fos;
//	
//	public static void setUpOutputStream() {
//		try {
//			fos = new BufferedOutputStream(new FileOutputStream(logfile, true));
//		} catch (Exception e) {
//			System.err.println(e.getMessage());
//		}
//	}
//	
//	public static void closeOutputStream() {
//		try {
//			fos.close();
//		} catch (Exception e) {
//			System.err.println(e.getMessage());
//		}
//	}
//	
//	public static void setFileLogMode(boolean fileLogMode) {
//		FILE_LOG_MODE = fileLogMode;
//	}
//	
//	public static void println(String text) {
//		if (FILE_LOG_MODE) {
//			try {
//				fos.write(text.getBytes());
//				fos.flush();
//			} catch (Exception e) {
//				System.err.println(e.getMessage());	
//			}
//		}
//		MyBotModule.Broodwar.printf(text);
//		System.out.println(text);
//	}
//	
//	public static void println(Exception e) {
//		for (StackTraceElement ste : e.getStackTrace()) {
//			println(ste.toString());
//		}
//	}
	
	public static void println(Exception e) {
		DebugOut.stackTrace(e);
		MyBotModule.Broodwar.printf(e.toString());
	}
	
	
	public static void println(String text) {
		DebugOut.println(text);
		MyBotModule.Broodwar.printf(text);
	}
	

}