/**
 * This file is part of DroidPHP
 *
 * (c) 2013 Shushant Kumar
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package net.pocketmine.server;

import android.content.*;
import android.os.*;
import android.util.*;
import java.io.*;
import java.nio.charset.*;

import java.lang.Process;

public final class ServerUtils
{
	final static String TAG = "com.MrARM.DroidPocketMine.ServerUtils";
	static Context mContext;
	private static OutputStream stdin;
	private static InputStream stdout;

	final public static void setContext(Context mContext)
	{
		ServerUtils.mContext = mContext;
	}

	final public static String getAppDirectory()
	{
		return mContext.getApplicationInfo().dataDir;
	}

	final public static String getDataDirectory()
	{
		return Environment.getExternalStorageDirectory().getPath() + "/PocketMine";
	}

	/**
	 * Instead of killing process by its PID you can use this method to kill
	 * process by specifying its name
	 * 
	 * @param mProcessName
	 *            Name Of Process that you want to kill
	 * @return boolean
	 */
	final public static Boolean killProcessByName(String mProcessName)
	{
		return execCommand(getAppDirectory() + "/killall " + mProcessName);
	}

	final public static void stopServer()
	{
		killProcessByName("php");
	}

	static Process serverProc;

	public static Boolean isRunning()
	{
		try
		{
			serverProc.exitValue();
		}
		catch (Exception e)
		{
			// do there the rest
			return true;
		}
		return false;
	}

	final public static void runServer()
	{
		File f = new File(getDataDirectory(), "tmp/");
		if (!f.exists())
		{
			f.mkdir();
		}
		else if (!f.isDirectory())
		{
			f.delete();
			f.mkdir();
		}
		setPermission();

		String file = "/PocketMine-MP.php";
		if (new File(getDataDirectory() + "/PocketMine-MP.phar").exists())
		{
			file = "/PocketMine-MP.phar";
		}
		else if (new File(getDataDirectory() + "/src/pocketmine/PocketMine-MP.php").exists())
		{
			file = "/src/pocketmine/PocketMine-MP.php";
		}

		File ini=new File(getDataDirectory() + "/php.ini");
		if (!ini.exists())
		{
			try
			{
				ini.createNewFile();
				FileOutputStream os=new FileOutputStream(ini);
				os.write(("zend.enable_gc=On\nzend.assertions=-1\n\nenable_dl=On\nallow_url_fopen=On\nmax_execution_time=0\nregister_argc_argv=On\n\nerror_reporting=-1\ndisplay_errors=stderr\ndisplay_startup_errors=On\n\ndefault_charset=\"UTF-8\"\n\nphar.readonly=Off\nphar.require_hash=On\n\nopcache.enable=1\nopcache.enable_cli=1\nopcache.save_comments=1\nopcache.load_comments=1\nopcache.fast_shutdown=0\nopcache.memory_consumption=128\nopcache.interned_strings_buffer=8\nopcache.max_accelerated_files=4000\nopcache.optimization_level=0xffffffff").getBytes("UTF8"));
				os.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		String[] serverCmd = { getAppDirectory() + "/php",getDataDirectory() + file };

		ProcessBuilder builder = new ProcessBuilder(serverCmd);
		builder.redirectErrorStream(true);
		builder.directory(new File(getDataDirectory()));
		builder.environment().put("TMPDIR", getDataDirectory() + "/tmp");
		try
		{
			serverProc = builder.start();
			stdout = serverProc.getInputStream();
			stdin = serverProc.getOutputStream();

			LogActivity.log("[PocketMine] 服务器开启中...");

			Thread tMonitor = new Thread() 
			{
				public void run()
				{
					InputStreamReader reader = new InputStreamReader(stdout, Charset.forName("UTF-8"));
					BufferedReader br = new BufferedReader(reader);

					while (isRunning())
					{
						try
						{
							char[] buffer = new char[8192];
							int size = 0;
							while ((size = br.read(buffer, 0, buffer.length)) != -1)
							{
								StringBuilder s = new StringBuilder();
								for (int i = 0; i < size; i++)
								{
									char c = buffer[i];
									if (c == '\r')
									{

									} 
									else if (c == '\n' || c == '\u0007')
									{
										String line = s.toString();
										Log.d(TAG, line);

										String lineNoDate = "";
										int iof = line.indexOf(" ");
										if (iof != -1)
											lineNoDate = line.substring(iof + 1);
										if (lineNoDate.startsWith("[CMD] There are ")
											&& requestPlayerRefresh
											&& requestPlayerRefreshCount == -1)
										{
											try
											{
												String num = lineNoDate.substring("[CMD] There are ".length());
												num = num.substring(0, num.indexOf("/"));
												requestPlayerRefreshCount = Integer.parseInt(num);

												if (requestPlayerRefreshCount == 0)
												{
													HomeActivity.updatePlayerList(null);
													requestPlayerRefresh = false;
												}
											}
											catch (Exception e)
											{
												e.printStackTrace();
											}
										}
										else if (lineNoDate.startsWith("[CMD] ")
												 && requestPlayerRefresh
												 && requestPlayerRefreshCount != -1)
										{
											String player = lineNoDate.substring(6);
											String[] players = player.split(", ");

											HomeActivity.updatePlayerList(players);

											requestPlayerRefresh = false;
										}
										else if (c == '\u0007' && line.startsWith("\u001B]0;"))
										{
											line = line.substring(4);
											System.out.println("[Stat] " + line);
											HomeActivity.setStats(
												getStat(line, "Online"),
												getStat(line, "RAM"),
												getStat(line, "U"),
												getStat(line, "D"),
												getStat(line, "TPS"));
										}
										else
										{
											LogActivity.log("[Server] " + (line.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")));

											if (line.contains("] logged in with entity id ") || line.contains("] logged out due to "))
											{
												refreshPlayers();
											}
										}
										s = new StringBuilder();
									}
									else
									{
										s.append(buffer[i]);
									}
								}
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
						finally
						{
							try
							{
								br.close();
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					}

					LogActivity.log("[PocketMine] 服务器已停止！");
					HomeActivity.stopNotifyService();
					HomeActivity.hideStats();
				}

			};
			tMonitor.start();

			Log.i(TAG, "PHP已开启！");
		}
		catch (Exception e)
		{
			Log.e(TAG, "无法开启PHP", e);
			LogActivity.log("[PocketMine] 无法开启PHP！");
			HomeActivity.stopNotifyService();
			HomeActivity.hideStats();
			killProcessByName("php");
		}
		return;
	}

	public static String getStat(String line, String stat)
	{
		stat = stat + " ";
		String result = line.substring(line.indexOf(stat) + stat.length());
		int iof = result.indexOf(" ");
		if (iof != -1)
		{
			result = result.substring(0, iof);
		}
		return result;
	}

	private static Boolean requestPlayerRefresh = false;
	private static int requestPlayerRefreshCount = -1;

	public static void refreshPlayers()
	{
		System.out.println("刷新玩家列表");
		requestPlayerRefreshCount = -1;
		requestPlayerRefresh = true;
		executeCMD("list");
	}

	/**
	 * 
	 * @param mCommand
	 *            hold the command which will be executed by invoking {@link
	 *            Runtime.getRuntime.exec(...)}
	 * @return boolean
	 * @throws IOException
	 *             if it unable to execute the command
	 */

	final public static boolean execCommand(String mCommand)
	{
		/*
		 * Create a new Instance of Runtime
		 */
		Runtime r = Runtime.getRuntime();
		try
		{
			/**
			 * Executes the command
			 */
			r.exec(mCommand);

		}
		catch (IOException e)
		{
			Log.e(TAG, "execCommand", e);

			r = null;

			return false;
		}
		return true;
	}

	final static private void setPermission()
	{
		try
		{
			execCommand("/system/bin/chmod 777 " + getAppDirectory() + "/php");
			execCommand("/system/bin/chmod 777 " + getAppDirectory() + "/killall");
		}
		catch (Exception e)
		{
			Log.e(TAG, "setPermission", e);
		}
	}

	public static boolean checkIfInstalled()
	{
		File mPhp = new File(getAppDirectory() + "/php");
		File mPM = new File(getDataDirectory() + "/PocketMine-MP.php");
		File mPMPhar = new File(getDataDirectory() + "/PocketMine-MP.phar");

		int saveVer = HomeActivity.prefs != null ? HomeActivity.prefs.getInt("filesVersion", 0) : 0;

		// File mMySql = new File(getAppDirectory() + "/mysqld");
		// File mLighttpd = new File(getAppDirectory() + "/lighttpd");
		// File mMySqlMon = new File(getAppDirectory() + "/mysql-monitor");

		if (mPhp.exists() && (mPM.exists() || mPMPhar.exists()) && saveVer == 6)
		{
			return true;
		}
		return false;
	}

	public static void executeCMD(String CCmd)
	{
		try
		{
			stdin.write((CCmd + "\r\n").getBytes());
			stdin.flush();
		}
		catch (Exception e)
		{
			// stdin.close();
			Log.e(TAG, "无法执行： " + CCmd, e);
		}
	}
}
