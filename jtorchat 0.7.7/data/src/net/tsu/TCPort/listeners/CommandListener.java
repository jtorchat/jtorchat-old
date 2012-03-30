package net.tsu.TCPort.listeners;

import net.tsu.TCPort.Buddy;

public interface CommandListener {
	public void onCommand(Buddy buddy, String s);
}
