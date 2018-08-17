import bwapi.Position;

public class __Util {

	private static boolean releaseMode = false;
	
	public static void println(Exception e)	{
		if (releaseMode) return;
		__DebugOut.stackTrace(e);
		MyBotModule.Broodwar.printf(e.toString());
	}
	
	public static void println(String text) {
		if (releaseMode) return;
		__DebugOut.println(text);
		MyBotModule.Broodwar.printf(text);
	}
	
	
	public static int getDistanceSquared(Position pos1, Position pos2)
	{
		int dx = pos1.getX() - pos2.getX();
		int dy = pos1.getY() - pos2.getY();
		return (dx*dx) + (dy*dy);
	}
	
}
