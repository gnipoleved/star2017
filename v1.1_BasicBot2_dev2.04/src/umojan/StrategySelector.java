package umojan;

public class StrategySelector {
	
	private static SampleStrategy sampleStrategy = new SampleStrategy();

	public static Strategy select() {
		return sampleStrategy;
	}

}
