package net.tsu.TCPort;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;



public class TorLoader {
	private static Process process;
	private static Object loadLock = new Object();

	public static void loadTor() {
		final TorLoading tl = new TorLoading();
		tl.getProgressBar1().setIndeterminate(true);
		tl.setVisible(true);
		ThreadManager.registerWork(ThreadManager.DAEMON, new Runnable() {

			@Override
			public void run() {
				
				boolean exists = (new File(Config.BASE_DIR+Config.DATA_DIR+Config.controlfile)).exists(); // check base dir
				if (!exists) {
					Logger.log(Logger.FATAL, "TorLoader", "Wrong base dir, the controlfile is not find:"+Config.BASE_DIR+Config.DATA_DIR+Config.controlfile);
					TCPort.getLogInstance().setVisible(true);
					tl.getProgressBar1().setValue(0);
					tl.getProgressBar1().setIndeterminate(false);
					tl.gettextArea1().setText("Wrong base dir, the controlfile is not find!");
				} else {

				if (Config.answer!=null) // if a language file found
				{
				Logger.log(Logger.FATAL, "TorLoader", Config.answer);
				TCPort.getLogInstance().setVisible(true);
				tl.getProgressBar1().setValue(0);
				tl.getProgressBar1().setIndeterminate(false);
				tl.gettextArea1().setText(Config.answer);				
				}
				else {
				
					if (Config.offlineMod == 0)
					{
					if (Config.loadTor == 1) // only load portable tor if not testing
					{
                ProcessBuilder pb=null;
                String os = System.getProperty("os.name").toLowerCase(); // Operating System details as a CASE INSENSTIVE string
                Logger.log(Logger.NOTICE, "TorLoader ", "Checking OS: "+os);
				// PROCESSBUILDER settings for WINDOWS
				if (os.indexOf("win") >= 0) {
					Logger.log(Logger.NOTICE, "TorLoader",
							"Start portable tor in windows");// if so, then start tor.exe with torrc.txt
					pb = new ProcessBuilder(
							new String[] { Config.TOR_DIR + Config.Torbinary,
									"-f", Config.TOR_DIR + Config.Tortorrc });
					// PROCESSBUILDER settings for LINUX and UNIX
				} else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
					Logger.log(Logger.NOTICE, "TorLoader",
							"Start portable tor in *nix or linux");
					
					
					
					pb = new ProcessBuilder(
							new String[] { Config.TOR_DIR + Config.Torbinary, Config.torpid, Config.TOR_DIR		
					});

				}
           else {
                Logger.log(Logger.NOTICE, "TorLoader","Can't detect OS type, using system tor (need system tor to work)");
                pb = new ProcessBuilder(new
                String[] { "tor", "-f",     Config.TOR_DIR + Config.Tortorrc
                });
        }

				pb.directory(new File(Config.TOR_DIR)
						.getAbsoluteFile());
				System.out.println(new File(Config.TOR_DIR)
						.getAbsolutePath());
				pb.redirectErrorStream(true);
				try {
					process = pb.start();
					Scanner s = new Scanner(process.getInputStream());
					while (s.hasNextLine()) {
						String l = s.nextLine();
						Logger.log(Logger.INFO, "Tor", l);
						

						
						// New progress function (obfsproxy has more then three progress messages)
						if (l.contains("Bootstrapped ")) {
							String[] starting = l.split("%");
							if (starting.length == 2)
							{
							starting = starting[0].split(" ");
							if (starting.length == 6)	
							{
							int starting3 = Integer.parseInt(starting[5]);
							if (starting3 <= 100 || starting3 > 0)
							{
							tl.getProgressBar1().setValue(Integer.parseInt(starting[5]));
							tl.gettextArea1().setText("Everything works until now.");
							tl.getProgressBar1().setIndeterminate(false);
							if(starting3 == 100)
							{
								synchronized(loadLock) {
									Config.us = new Scanner(new FileInputStream(Config.TOR_DIR + "hidden_service/hostname")).nextLine().replace(".onion", "");
									Logger.log(Logger.NOTICE, "TorLoader", "Set 'us' to " + Config.us);
									loadLock.notifyAll();
									tl.dispose();
							}}}}}}
						
					
						if (l.contains("Is Tor already running?")) {
							try { // testing other tor
								
								new Socket("127.0.0.1", Config.SOCKS_PORT);
								
								synchronized(loadLock) {
									Logger.log(Logger.SEVERE, "TorLoader", language.langtext[46]);
									Config.us = new Scanner(new FileInputStream("Config.TOR_DIR + hidden_service/hostname")).nextLine().replace(".onion", "");
									Logger.log(Logger.NOTICE, "TorLoader", "Set 'us' to " + Config.us);
									loadLock.notifyAll();
									tl.dispose();
								} 
									
									
								
							} catch (Exception e) {
								// other tor not on our SOCKS_PORT
								Logger.log(Logger.FATAL, "TorLoader", "Tor exited with " + l + ", failed to use it.");							
								TCPort.getLogInstance().setVisible(true);
								tl.getProgressBar1().setValue(0);
								tl.getProgressBar1().setIndeterminate(false);
								tl.gettextArea1().setText(language.langtext[47]);
							}
						}
						

						
						if (l.contains("Failed to bind one of the listener")) {
							Logger.log(Logger.FATAL, "TorLoader", "The listener Port is in use.");
							TCPort.getLogInstance().setVisible(true);
							tl.getProgressBar1().setValue(0);
							tl.getProgressBar1().setIndeterminate(false);
							tl.gettextArea1().setText(language.langtext[48]);	
						}
						
						
						if (l.contains("Network is unreachable")) {
							Logger.log(Logger.FATAL, "TorLoader", "Network is unreachable.");
							TCPort.getLogInstance().setVisible(true);
							tl.getProgressBar1().setValue(0);
							tl.getProgressBar1().setIndeterminate(false);
							tl.gettextArea1().setText("Network is unreachable.");	
						}
						
						
						if (l.contains("broken state. Dying.")) {
							Logger.log(Logger.FATAL, "TorLoader", "Tor has died apparently, Ja.");
							TCPort.getLogInstance().setVisible(true);
							tl.getProgressBar1().setValue(0);
							tl.getProgressBar1().setIndeterminate(false);
							tl.gettextArea1().setText(language.langtext[49]);
						}
					}
				} catch (IOException e) {
					Logger.log(Logger.SEVERE, "TorLoader", e.getLocalizedMessage());
					if (e.getLocalizedMessage().contains("Cannot"))
					{
						TCPort.getLogInstance().setVisible(true);
						tl.getProgressBar1().setValue(0);
						tl.getProgressBar1().setIndeterminate(false);
						tl.gettextArea1().setText(language.langtext[50]);

					}
				}
			}
					
					
					
					
					
			}
					else
					{
						tl.dispose();
						
					}
				}
			}}
			

			
			
			
		}, "Starting Tor.", "Tor Monitor Thread");
		
	if (Config.offlineMod == 0 & Config.loadTor == 1)
	{
		try {
			synchronized(loadLock) {
				loadLock.wait();
			}
		} catch (InterruptedException e) {
		}
	}
	String error = TCServ.init();
	if (error != null)
	{
		TCPort.getLogInstance().setVisible(true);
		tl.getProgressBar1().setValue(0);
		tl.getProgressBar1().setIndeterminate(false);
		tl.gettextArea1().setText(error);
	}
	else
	{
		tl.dispose();
	}
	}

	public static void cleanUp() {
		synchronized(loadLock) {
			loadLock.notifyAll();
		}
		Logger.log(Logger.INFO, "TorLoader", "ACleaning up.");
		String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
		try {
		ProcessBuilder torclose = new ProcessBuilder(
			new String[] { Config.TOR_DIR + Config.Torclose, Config.torpid, Config.TOR_DIR});
		Logger.log(Logger.INFO, "TorLoader", "Kill Tor with "+Config.TOR_DIR + Config.Torclose);
			torclose.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		if (process != null) {
			Logger.log(Logger.INFO, "TorLoader", "Cleaning up.");
			process.destroy();
		}
	}

}

