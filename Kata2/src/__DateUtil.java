import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class __DateUtil {
	
	public static String TO_STRING(Date source, String format){
		return new SimpleDateFormat(format).format(source);
	}
	
	public static Date TO_DATE(String source, String format) throws ParseException{
		return new SimpleDateFormat(format).parse(source);
	}
	
	public static Date ADD_DAYS(Date source, int days){
		Calendar cal = Calendar.getInstance();
		cal.setTime(source);
		cal.add(Calendar.DATE, days);
		return cal.getTime();
	}
	
	public static Date MID_DATE(Date first, Date second){
		// return the middle date of two argument dates.
		if (first.after(second)) {
			Date temp = new Date(first.getTime());
			first = new Date(second.getTime());
			second = temp;
		}
		return new Date(first.getTime() + (second.getTime() - first.getTime()) / 2);
	}
	

}
