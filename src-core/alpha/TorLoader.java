package alpha;

import gui.KillTor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

import util.ConfigWriter;

public class TorLoader {
	private static final String CLASS_NAME = TorLoader.class.getName();
	private static Process process;
	private static Object loadLock = new Object();

	public static void loadTor() {
		final TorLoading tl = new TorLoading();
		tl.getProgressBar1().setIndeterminate(true);
		tl.setVisible(true);
		ThreadManager.registerWork(ThreadManager.DAEMON, new Runnable() {

			@Override
			public void run() {

				final String controlFilePath = Config.BASE_DIR + Config.DATA_DIR + Config.controlfile;
				boolean exists = (new File(controlFilePath)).exists(); // check base dir
				if (!exists) {
					Logger.log(Logger.FATAL, CLASS_NAME, "Wrong base dir, the controlfile is not find:" + controlFilePath);
					TCPort.getLogInstance().setVisible(true);
					tl.getProgressBar1().setValue(0);
					tl.getProgressBar1().setIndeterminate(false);
					tl.gettextArea1().setText("Wrong base dir, the controlfile is not find!");
				} else {

					if (Config.answer != null) // if a language file NOT found
					{
						Logger.log(Logger.FATAL, CLASS_NAME, Config.answer);
						TCPort.getLogInstance().setVisible(true);
						tl.getProgressBar1().setValue(0);
						tl.getProgressBar1().setIndeterminate(false);
						tl.gettextArea1().setText(Config.answer);
					} else {

						if (Config.offlineMod == 0) {
							Boolean ssfail = false;
							try {
								ServerSocket ss = new ServerSocket(Config.LOCAL_PORT);
								ss.close();
							} catch (IOException e) {
								ssfail = true;
							}

							if (ssfail) {
								Logger.log(Logger.FATAL, "TCServ", "Failed to test local server.");
								tl.getProgressBar1().setValue(0);
								tl.gettextArea1().setText("Can not bind a port for jtorchat, is another jtorchat instance activ?");
								tl.getProgressBar1().setIndeterminate(false);
							} else {

								if (Config.loadTor == 1) // only load portable tor if not testing
								{
									ProcessBuilder pb = null;
									pb = instantiateProcessBuilder();

									pb.directory(new File(Config.TOR_DIR).getAbsoluteFile());
									System.out.println(new File(Config.TOR_DIR).getAbsolutePath());
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
												if (starting.length == 2) {
													starting = starting[0].split(" ");
													if (starting.length == 6) {
														int starting3 = Integer.parseInt(starting[5]);
														if (starting3 <= 100 || starting3 > 0) {
															tl.getProgressBar1().setValue(Integer.parseInt(starting[5]));
															tl.gettextArea1().setText(language.langtext[51]);
															tl.getProgressBar1().setIndeterminate(false);
															if (starting3 == 100) {
																synchronized (loadLock) {

																	Config.us = new Scanner(new FileInputStream(Config.TOR_DIR + "hidden_service/hostname")).nextLine().replace(".onion", "");
																	Logger.log(Logger.NOTICE, CLASS_NAME, "Set 'us' to " + Config.us);
																	loadLock.notifyAll();
																	tl.dispose();
																}
															}
														}
													}
												}
											}

											if (l.contains("Failed to bind one of the listener")) {
												Logger.log(Logger.FATAL, CLASS_NAME, "The listener Port is in use.");

												if (KillTor.newpb == 0) {
													tl.gettextArea1().setText("Wait for a answer!");
													KillTor.listenerport();
													process.destroy();
													pb = instantiateProcessBuilder();
													pb.directory(new File(Config.TOR_DIR).getAbsoluteFile());
													System.out.println(new File(Config.TOR_DIR).getAbsolutePath());
													pb.redirectErrorStream(true);
													process = pb.start();
													s = new Scanner(process.getInputStream());
													KillTor.newpb = 2;

												} else {
													tl.getProgressBar1().setValue(0);
													tl.getProgressBar1().setIndeterminate(false);
													tl.gettextArea1().setText(language.langtext[48]);
												}
											}

											if (l.contains("Network is unreachable")) {
												Logger.log(Logger.FATAL, CLASS_NAME, "Network is unreachable.");
												tl.getProgressBar1().setValue(0);
												tl.getProgressBar1().setIndeterminate(false);
												tl.gettextArea1().setText("Network is unreachable.");
											}

											if (l.contains("broken state. Dying.")) {
												Logger.log(Logger.FATAL, CLASS_NAME, "Tor has died apparently, Ja.");
												TCPort.getLogInstance().setVisible(true);
												tl.getProgressBar1().setValue(0);
												tl.getProgressBar1().setIndeterminate(false);
												tl.gettextArea1().setText(language.langtext[49]);
											}
										}
									} catch (IOException e) {
										Logger.log(Logger.SEVERE, CLASS_NAME, e.getLocalizedMessage());
										if (e.getLocalizedMessage().contains("Cannot")) {
											TCPort.getLogInstance().setVisible(true);
											tl.getProgressBar1().setValue(0);
											tl.getProgressBar1().setIndeterminate(false);
											tl.gettextArea1().setText(language.langtext[50]);

										}
									}
								}
							}

						} else {
							tl.dispose();

						}
					}
				}
			}

		}, "Starting Tor.", "Tor Monitor Thread");

		if (Config.offlineMod == 0 & Config.loadTor == 1) {
			try {
				synchronized (loadLock) {
					loadLock.wait();
				}
			} catch (InterruptedException e) {
			}
		}
	}

	public static void cleanUp() {
		synchronized (loadLock) {
			loadLock.notifyAll();
		}
		Logger.log(Logger.INFO, CLASS_NAME, "ACleaning up.");

		if (Config.os.indexOf("nix") >= 0 || Config.os.indexOf("nux") >= 0) {
			try {
				Config.firststart = 0;
				ConfigWriter.saveall(2);
				ProcessBuilder torclose = new ProcessBuilder(new String[] { Config.TOR_DIR + Config.Torclose, Config.torpid, Config.TOR_DIR });
				Logger.log(Logger.INFO, CLASS_NAME, "Kill Tor with " + Config.TOR_DIR + Config.Torclose);
				torclose.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (process != null) {
			Logger.log(Logger.INFO, CLASS_NAME, "Cleaning up.");
			process.destroy();
		}
	}

	private static ProcessBuilder instantiateProcessBuilder() {
		ProcessBuilder pb;
		Logger.log(Logger.NOTICE, "TorLoader ", "Checking OS");
		// PROCESSBUILDER settings for WINDOWS
		if (Config.os.contains("win")) {

			// if so, then start tor.exe with torrc.txt
			Logger.log(Logger.NOTICE, CLASS_NAME, "Start portable tor in windows");
			pb = new ProcessBuilder(new String[] { Config.TOR_DIR + Config.Torbinary, "-f", Config.TOR_DIR + Config.Tortorrc });

		} else if (Config.os.contains("nix") || Config.os.contains("nux")) {

			// PROCESSBUILDER settings for LINUX and UNIX
			Logger.log(Logger.NOTICE, CLASS_NAME, "Start portable tor in *nix or linux");
			pb = new ProcessBuilder(new String[] { Config.TOR_DIR + Config.Torbinary, Config.torpid, Config.TOR_DIR });

		} else {
			Logger.log(Logger.NOTICE, CLASS_NAME, "Can't detect OS type, using system tor (need system tor to work)");
			pb = new ProcessBuilder(new String[] { "tor", "-f", Config.TOR_DIR + Config.Tortorrc });
		}
		return pb;
	}

}
