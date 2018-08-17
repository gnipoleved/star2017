import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

public class Common {

	// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
	// appendTextToFile 등 메소드를 static 으로 수정

	/// 로그 유틸
	public static void appendTextToFile(final String logFile, final String msg)
	{
		try {
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(logFile), true))  ;
			bos.write(msg.getBytes());
			bos.flush();
			bos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/// 로그 유틸
	public static void overwriteToFile(final String logFile, final String msg)
	{
		try {
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(logFile)))  ;
			bos.write(msg.getBytes());
			bos.flush();
			bos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/// 파일 유틸 - 텍스트 파일을 읽어들인다
	public static String readFile(final String filename)
	{
		BufferedInputStream bis;
		StringBuilder sb = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(new File(filename)));
	        sb = new StringBuilder();

	        while (bis.available() > 0) {
	            sb.append((char) bis.read());
	        }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}
	
	// BasicBot 1.1 Patch End //////////////////////////////////////////////////

	public static class State {
		int value;
		State(int value) {this.value = value;}
	}

	public static State initState = new State(0);
	public static State defense = new State(100);
	public static State scout = new State(200);
	public static State scout_drone = new State(201);
	public static State scout_overlord = new State(202);
	public static State scout_first_zergling = new State(203);
	public static State attack = new State(300);
	public static State wait_attack = new State(400);
	public static State eli = new State(500);


	public static int WholeUnitCount(UnitType unitType)
	{
		//return CountCompleted(unitType) + CountIncompleted(unitType) + CountInBuildQ(unitType);
		//return MyBotModule.Broodwar.self().allUnitCount(unitType) + CountInBuildQ(unitType) + CountInConstQ(unitType);
		return CountCompleted(unitType) + CountIncompleted(unitType) + CountInBuildQ(unitType) + CountInConstQ(unitType);
	}

	public static int CountInConstQ(UnitType unitType)
	{
		return ConstructionManager.Instance().getConstructionQueueItemCount(unitType, null);
	}

	public static int CountInBuildQ(UnitType unitType)
	{
		return BuildManager.Instance().buildQueue.getItemCount(unitType);
	}
	
	public static int CountIncompleted(UnitType unitType)
	{
		return MyBotModule.Broodwar.self().incompleteUnitCount(unitType);
	}
	
	public static int CountCompleted(UnitType unitType)
	{
		return MyBotModule.Broodwar.self().completedUnitCount(unitType);
	}


	public static int getDistanceSquared(Position pos1, Position pos2) {
		int dx = pos1.getX() - pos2.getX();
		int dy = pos1.getY() - pos2.getY();
		return (dx*dx) + (dy*dy);
	}

    public static boolean IsAlive(Unit unit) {
		return unit != null && unit.exists() && unit.getHitPoints() > 0;
    }


}