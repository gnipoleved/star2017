

import bwapi.Position;

public class __Util {

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
		__DebugOut.stackTrace(e);
		MyBotModule.Broodwar.printf(e.toString());
	}
	
	
	public static void println(String text) {
		__DebugOut.println(text);
		MyBotModule.Broodwar.printf(text);
	}


	public static int getDistanceSquared(Position pos1, Position pos2) {
		int dx = pos1.getX() - pos2.getX();
		int dy = pos1.getY() - pos2.getY();
		//Util.println(ui.getUnit().getPosition().getX() + " - " + targetPosition.getX() + " = " + dx);
		//Util.println(ui.getUnit().getPosition().getY() + " - " + targetPosition.getY() + " = " + dy);
		return (dx*dx) + (dy*dy);
	}
	

}