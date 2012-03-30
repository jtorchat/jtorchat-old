package net.tsu.TCPort.listeners;

import java.io.InputStream;

import net.tsu.TCPort.Buddy;

public interface IncomingCommandListener {
	public void onCommand(Buddy buddy, String command, InputStream is);
}
