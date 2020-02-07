package jp.gmail.kogecha05.utils;

public class Timer {
	private long startTime;

	public void timerStart() {
		startTime = System.currentTimeMillis();
	}

	public long getElapsedTime() {
		return System.currentTimeMillis() - startTime;
	}
}
