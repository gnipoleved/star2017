

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;


public class __DebugOut {
	
	public static int MAX_BUFFER = 1024 * 1024 * 2;
	public static String DEFAULT_LOG_FILE_PFX = "log_";
	
	public static final String LD = System.getProperty("line.separator");
	
	public static enum DEBUG_MODE {
		ON("ON")
		,OFF("OFF")
		;
		private String value;
		private DEBUG_MODE(String value) {
			this.value = value;
		}
		public String getValue() {
			return this.value;
		}
	}
	
	public static enum DEBUG_FILE_MODE {
		ON("ON")
		,OFF("OFF")
		;
		private String value;
		private DEBUG_FILE_MODE(String value) {
			this.value = value;;
		}
		public String getValue() {
			return this.value;
		}
	}
	 
	//public static DEBUG_MODE CURRENT_DEBUG_MODE = (Config.getProperty("DEBUG_MODE")==null) ? DEBUG_MODE.OFF : DEBUG_MODE.valueOf(Config.getProperty("DEBUG_MODE"));
	//public static DEBUG_FILE_MODE CURRENT_FILE_MODE = (Config.getProperty("DEBUG_FILE_MODE")==null) ? DEBUG_FILE_MODE.OFF : DEBUG_FILE_MODE.valueOf(Config.getProperty("DEBUG_FILE_MODE"));
	public static DEBUG_MODE CURRENT_DEBUG_MODE = DEBUG_MODE.ON;
	public static DEBUG_FILE_MODE CURRENT_FILE_MODE = DEBUG_FILE_MODE.ON;
	
	public static String LOG_FILE_PFX = "C:\\Starcraft\\logs\\umojan_";
	public static StringBuffer logs = new StringBuffer(MAX_BUFFER);
	
	public static File getNewLogFileWithCurrentDate(Date currDate) {
		File newFile = new File(LOG_FILE_PFX  + __DateUtil.TO_STRING(currDate, "yyyyMMdd_HHmmss") + ".log");
		if (newFile.exists()) {
			newFile = new File(LOG_FILE_PFX  + __DateUtil.TO_STRING(new Date(System.currentTimeMillis() + 1000), "yyyyMMdd_HHmmss") + ".log");
		}
		return newFile;
	}
	
	private static boolean isFileMode() {
		return (CURRENT_FILE_MODE==DEBUG_FILE_MODE.ON);
	}
	
	private static boolean isDebugMode() {
		return (CURRENT_DEBUG_MODE==DEBUG_MODE.ON);
	}
	
	// 이 method 는 logs 가 synchronized 되어 있는 block 안에서만 호출되어야 함..
	private static void filelog() throws DebugException {
		File file = getNewLogFileWithCurrentDate(new Date());
		try {
			__FileUtil.writeTextToFile(file, logs.toString());
		} catch (Throwable e) {
			throw new DebugException("Logging failed.", e);
		}
	}
		
	private static void log(String text) throws DebugException {
		synchronized(logs) {
			logs.append(text);
			// logs 의 사이즈가 특정 이상이면 여기서 일단 한번 파일로 저장하고 다시 logs = new StringBuffer(); 로  새로 할당 해 준다.
			if (logs.length() > MAX_BUFFER) {
				filelog();
				logs = new StringBuffer(MAX_BUFFER);
			}
		}
	}
	
	private static String makeMillisFormatted(long currentMillis) {
		return "[" + __DateUtil.TO_STRING(new Date(currentMillis), "yyyy-MM-dd HH:mm:ss") + "] ";
	}
	
	
	private static class DebugOutputStream extends OutputStream {
		
		private static final int defaultBufferSize = 800;
		private byte[] bytes;
		private int occupiedCnt;

		private DebugOutputStream() {
			init();
		}

		private void init() {
			bytes = new byte[defaultBufferSize];
			occupiedCnt = 0;
		}
		
		@Override
		public void write(int b) throws IOException {
			if (occupiedCnt >= bytes.length) allocate();
			bytes[occupiedCnt] = (byte) b;
			occupiedCnt++;
		}

		@Override
		public void flush() throws IOException {
			rndebugln(new String(bytes));
			init();
		}
		

		private void allocate() {
			byte[] newBytes = new byte[bytes.length + defaultBufferSize];
			for (int index = 0; index < bytes.length; index++) {
				newBytes[index] = bytes[index];
			}
			bytes = newBytes;
		}
		
	}
	
	public static void stackTrace(Throwable e) throws DebugException {
		DebugOutputStream dos = new DebugOutputStream();
		e.printStackTrace(new PrintStream(dos));
		try {
			dos.flush();
			dos.close();
		} catch (IOException ie) {
			throw new DebugException("Logging failed. - ", ie);
		}
//		e.printStackTrace(new DebugPrintStream(new DebugOutputStream()));
	}
	
	
	// \r\n 이 byte 단위로 나뉘어져서 write 되니 두개가 짤라져서 각각 라인이 내려가는 듯...
	// 정확한 PrintStream의 write 로직을 알기 전에는 이 class 사용은 보류하자...
	@SuppressWarnings("unused")
	private static class DebugPrintStream extends PrintStream {

		public DebugPrintStream(OutputStream out) {
			super(out);
		}
		
		@Override
		public void println(String x) throws DebugException {
			super.println(x);
			try {
				out.flush();
			} catch (IOException e) {
				throw new DebugException(e);
			}
		}
	}
	
	
	@Deprecated
	public static PrintStream logStream = new PrintStream(new DebugOutputStream());
	
	
	
	private static void err(String output) {
		System.err.print(output);
		if (isFileMode()) log(output);
	}
	
	private static void errln(String output) {
		err(output + LD);
	}
	

	private static void out(String output) {
		System.out.print(output);
		if (isFileMode()) log(output);
	}
	
	private static void outln(String output) {
		out(output + LD);
	}
	
	
	public static void debug(Object o) {
		if (isDebugMode()) {
			print(o);
		}
	}
	
	public static void debugln(Object o) {
		if (isDebugMode()) {
			println(o);
		}
	}
	
	public static void ndebug(Object o) {
		if (isDebugMode()) {
			out(o.toString());
		}
	}
	
	public static void ndebugln(Object o) {
		if (isDebugMode()) {
			outln(o.toString());
		}
	}
	
	public static void rdebug(Object o) {
		rndebug(makeMillisFormatted(System.currentTimeMillis()) + o.toString());
	}
	
	public static void rdebugln(Object o) {
		rndebugln(makeMillisFormatted(System.currentTimeMillis()) + o.toString());
	}
	
	public static void rndebug(Object o) {
		if (isDebugMode()) {
			err(o.toString());
		}
	}
	
	public static void rndebugln(Object o) {
		if (isDebugMode()) {
			errln(o.toString());
		}
	}
	
	
	public static void print(boolean b){
		String output = makeMillisFormatted(System.currentTimeMillis()) + b;
		out(output);
	}
	
	public static void print(char c){
		String output = makeMillisFormatted(System.currentTimeMillis()) + c;
		out(output);
	}
	
	public static void print(char[] s){
		String output = makeMillisFormatted(System.currentTimeMillis()) + s.toString();
		out(output);
	}
	
	public static void print(double d){
		String output = makeMillisFormatted(System.currentTimeMillis())  + d;
		out(output);
	}
	
	public static void print(float f){
		String output = makeMillisFormatted(System.currentTimeMillis())  + f;
		out(output);
	}
	
	public static void print(int i){
		String output = makeMillisFormatted(System.currentTimeMillis())  + i;
		out(output);
	}
	
	public static void print(long l){
		String output = makeMillisFormatted(System.currentTimeMillis())  + l;
		out(output);
	}
	
	public static void print(String s){
		String output = makeMillisFormatted(System.currentTimeMillis()) + s;
		out(output);
	}
	
	public static void print(Object obj){
		String output = makeMillisFormatted(System.currentTimeMillis())  + obj;
		out(output);
	}
	
	public static void println(boolean x){
		String output = makeMillisFormatted(System.currentTimeMillis())  + x;
		outln(output);
	}
	
	public static void println(char x){
		String output = makeMillisFormatted(System.currentTimeMillis())  + x;
		outln(output);
	}
	
	public static void println(char[] x){
		String output = makeMillisFormatted(System.currentTimeMillis())  + x.toString();
		outln(output);
	}
	
	public static void println(double x){
		String output = makeMillisFormatted(System.currentTimeMillis())  + x;
		outln(output);
	}
	
	public static void println(float x){
		String output = makeMillisFormatted(System.currentTimeMillis())  + x;
		outln(output);
	}
	
	public static void println(int x){
		String output = makeMillisFormatted(System.currentTimeMillis())  + x;
		outln(output);
	}
	
	public static void println(long x){
		String output = makeMillisFormatted(System.currentTimeMillis())  + x;
		outln(output);
	}
	
	public static void println(String x){
		String output = makeMillisFormatted(System.currentTimeMillis())  + x;
		outln(output);
	}
	
	public static void println(List<String> x) {
		String output = makeMillisFormatted(System.currentTimeMillis()) + LD;
		for(String str : x) output += str + LD;
		println(output);
	}
	
	public static void println(Object x){
		String output = makeMillisFormatted(System.currentTimeMillis()) + x;
		outln(output);
	}
	
	public static void println(Object[] x){
		String output = makeMillisFormatted(System.currentTimeMillis()) + LD;
		for(Object obj : x) output += obj + LD; 
		println(output);
	}
	
	public static void println(){
		String output = makeMillisFormatted(System.currentTimeMillis());
		outln(output);
	}

	
	
	static
	{
		if (isFileMode()) {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					synchronized (logs) {
						if (logs.length() > 0) {
							filelog();
						}
					}
				}
			});
		}
	}
	
	
	public static class DebugException extends RuntimeException {
		
		private static final long serialVersionUID = -4563659926650551731L;

		public DebugException() {
			super();
		}
		
		public DebugException(String message) {
			super(message);
		}

		public DebugException(String message, Throwable cause) {
			super(message, cause);
		}
		
		public DebugException(Throwable cause) {
			super(cause);
		}
		
	}
	
	
}
