/**
 * This file is part of DroidPHP
 *
 * (c) 2013 Shushant Kumar
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package net.pocketmine.server;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.os.*;
import android.preference.*;
import android.support.v7.widget.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import java.net.*;
import java.util.*;
import org.apache.http.conn.util.*;

import android.support.v7.widget.Toolbar;

/**
 * Activity to Home Screen
 */

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class HomeActivity extends BaseActivity
{
	// private final String TAG = "com.github.com.DroidPHP";
	final static int PROJECT_CODE = 143;
	final static int VERSION_MANAGER_CODE = PROJECT_CODE + 1;
	final static int FILE_MANAGER_CODE = VERSION_MANAGER_CODE + 1;
	final static int PROPERTIES_EDITOR_CODE = FILE_MANAGER_CODE + 1;
	final static int FORCE_CLOSE_CODE = PROPERTIES_EDITOR_CODE + 1;
	final static int ABOUT_US_CODE = FORCE_CLOSE_CODE + 1;
	final static int SETTINGS=ABOUT_US_CODE + 1;
	final static int CONSOLE_CODE = SETTINGS + 1;
	final static int PM_EDIT_CODE=CONSOLE_CODE + 1;
	final static int DEV_CODE = PM_EDIT_CODE + 1;
	public static HashMap<String, String> server;
	public static SharedPreferences prefs;

	private final Context mContext = HomeActivity.this;
	public static HomeActivity ha = null;

	public static Boolean statsShown = false;
	public static String online = "Unknown";
	public static String ram = "Unknown";
	public static String download = "Unknown";
	public static String upload = "Unknown";
	public static String tps = "Unknown";
	public static String[] players = null;

	/**
	 * Buttons for managing server state
	 */
	public static Button btn_runServer;
	public static Button btn_stopServer;
	public static Intent servInt;
	public static Boolean isStarted = false;

	public static LayoutInflater inflater;

	private Toolbar toolbar;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		inflater = getLayoutInflater();

		ha = this;

		toolbar = (Toolbar)findViewById(R.id.home_toolbar);

		toolbar.setTitle("PocketMine");  
		setSupportActionBar(toolbar);   

		// startService(new Intent(mContext, ServerService.class));
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		ServerUtils.setContext(mContext);

		btn_runServer = (Button) findViewById(R.id.RunTime_Http);
		btn_stopServer = (Button) findViewById(R.id.RunTime_Http_Kill);

		btn_runServer.setEnabled(!isStarted);
		btn_runServer.setOnClickListener(new OnClickListener() 
			{
				@Override
				public void onClick(View v)
				{
					btn_runServer.setEnabled(false);
					btn_stopServer.setEnabled(true);
					btn_runServer.setText(R.string.server_online);
					String msg = "服务器启动失败！";
					if (ServerUtils.isRunning())
					{
						msg = "开启服务器中...";
					}
					Toast(msg, Toast.LENGTH_LONG);

					servInt = new Intent(mContext, ServerService.class);

					// i.putExtra(ServerService.EXTRA_PORT,
					// prefs.getString("k_server_port", "8080"));

					startService(servInt);
					isStarted = true;
					showStats(true);

					ServerUtils.runServer();
				}
			});

		btn_stopServer.setEnabled(isStarted);
		btn_stopServer.setOnClickListener(new OnClickListener() 
			{
				@Override
				public void onClick(View v)
				{
					if (ServerUtils.isRunning())
					{
						LogActivity.log("[PocketMine] 关闭服务器中...");
						ServerUtils.executeCMD("stop");
					}
				}
			});

		Button btn_op = (Button) findViewById(R.id.action_op);
		btn_op.setOnClickListener(new OnClickListener() 
			{
				@Override
				public void onClick(View arg0)
				{
					actionPlayer("op");
				}
			});

		Button btn_deop = (Button) findViewById(R.id.action_deop);
		btn_deop.setOnClickListener(new OnClickListener() 
			{
				@Override
				public void onClick(View arg0)
				{
					actionPlayer("deop");
				}
			});

		if (isStarted)
		{
			showStats(false);
		}
	}

	// http://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device
	public static String getIPAddress(boolean useIPv4)
	{
		try
		{
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces)
			{
				List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
				for (InetAddress addr : addrs)
				{
					if (!addr.isLoopbackAddress())
					{
						String sAddr = addr.getHostAddress().toUpperCase(Locale.US);
						boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
						if (useIPv4)
						{
							if (isIPv4)
								return sAddr;
						}
						else
						{
							if (!isIPv4)
							{
								int delim = sAddr.indexOf('%'); // drop ip6 port
								// suffix
								return delim < 0 ? sAddr : sAddr.substring(0, delim);
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return "127.0.0.1";
	}

	private void actionPlayer(final String cmd)
	{
		if (players == null)
			return;

		AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
		builder.setTitle("OP玩家...");
		final CharSequence[] list = new CharSequence[players.length + 1];
		list[0] = "返回";
		for (int i = 0; i < players.length; i++)
		{
			list[i + 1] = players[i];
		}
		builder.setItems(list, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface di, int pos)
				{
					if (pos == 0)
					{
						// nothing
					}
					else
					{
						ServerUtils.executeCMD(cmd + " " + list[pos]); // in
						// a
						// worst
						// case
						// the
						// player
						// array
						// can
						// change
					}
				}
			});
		builder.show();
	}

	public static void showStats(Boolean reset)
	{
		if (reset)
		{
			online = "Unknown";
			ram = "Unknown";
			download = "Unknown";
			upload = "Unknown";
			tps = "Unknown";
			players = null;
		}
		if (ha != null)
		{
			ha.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						LinearLayout layout = (LinearLayout) ha.findViewById(R.id.stats);
						layout.setVisibility(View.VISIBLE);
						statsShown = true;
						TextView ip = (TextView) ha.findViewById(R.id.stat_ip);
						ip.setText("IP地址：" + getIPAddress(true));
						setStats(online, ram, download, upload, tps);
						updatePlayerList(players);
					}
				});
		}
	}

	public static void setStats(final String nOnline, final String nRAM, final String nUpload, final String nDownload, final String nTPS)
	{
		online = nOnline;
		ram = nRAM;
		upload = nUpload;
		download = nDownload;
		tps = nTPS;

		if (ha != null)
		{
			ha.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						if (!statsShown)
						{
							showStats(true);
						}

						TextView online = (TextView) ha.findViewById(R.id.stat_online);
						online.setText("在线：" + nOnline);
						TextView ram = (TextView) ha.findViewById(R.id.stat_ram);
						ram.setText("内存：" + nRAM);
						TextView download = (TextView) ha.findViewById(R.id.stat_download);
						download.setText("下载：" + nDownload + " kB/s");
						TextView upload = (TextView) ha.findViewById(R.id.stat_upload);
						upload.setText("上传：" + nUpload + " kB/s");
						TextView tps = (TextView) ha.findViewById(R.id.stat_tps);
						tps.setText("TPS：" + nTPS);
					}
				});
		}
	}

	public static void hideStats()
	{
		if (ha != null)
		{
			ha.runOnUiThread(new Runnable() 
				{
					@Override
					public void run()
					{
						LinearLayout layout = (LinearLayout) ha.findViewById(R.id.stats);
						layout.setVisibility(View.GONE);
						statsShown = false;
					}
				});
		}
	}

	public static int dip2px(float dips)
	{
		return (int) (dips * ha.getResources().getDisplayMetrics().density + 0.5f);
	}

	public static void updatePlayerList(final String[] nPlayers)
	{
		players = nPlayers;

		if (ha != null && inflater != null)
		{
			ha.runOnUiThread(new Runnable() 
				{
					@Override
					public void run()
					{
						LinearLayout layout = (LinearLayout) ha.findViewById(R.id.players);
						layout.removeAllViews();

						if (players != null && players.length > 0)
						{
							for (final String player : players)
							{
								View v = inflater.inflate(R.layout.player, layout, false);
								TextView playerName = (TextView) v.findViewById(R.id.player_name);
								playerName.setText(player);
								final Button kickBtn = (Button) v.findViewById(R.id.player_kick);
								kickBtn.setOnClickListener(new OnClickListener() 
									{
										@Override
										public void onClick(View arg0)
										{
											LinearLayout ll = new LinearLayout(kickBtn.getContext());
											final EditText input = new EditText(kickBtn.getContext());
											LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
												LinearLayout.LayoutParams.MATCH_PARENT,
												LinearLayout.LayoutParams.WRAP_CONTENT);
											layoutParams.setMargins(dip2px(8), 0, dip2px(8), 0);
											input.setLayoutParams(layoutParams);
											ll.addView(input);
											new AlertDialog.Builder(kickBtn.getContext())
												.setTitle("踢玩家")
												.setMessage("踢玩家的原因")
												.setView(ll)
												.setPositiveButton("踢出", new DialogInterface.OnClickListener()
												{
													public void onClick(DialogInterface dialog, int whichButton)
													{
														ServerUtils.executeCMD("kick " + player + " " + input.getText().toString());
													}
												}).setNegativeButton("取消", null).show();
										}
									});
								final Button banBtn = (Button) v.findViewById(R.id.player_ban);
								banBtn.setOnClickListener(new OnClickListener() 
									{
										@Override
										public void onClick(View arg0)
										{
											AlertDialog.Builder builder = new AlertDialog.Builder(banBtn.getContext());
											builder.setTitle("Ban玩家...");
											builder.setItems(new CharSequence[] { "Ban玩家名","Ban玩家IP", "取消" }, new DialogInterface.OnClickListener() 
												{
													@Override
													public void onClick(DialogInterface dialog, int which)
													{
														if (which == 0)
														{
															ServerUtils.executeCMD("ban add " + player);
														}
														else if (which == 1)
														{
															ServerUtils.executeCMD("banip add " + player);
														}
													}
												});
											builder.show();
										}
									});
								layout.addView(v);
							}
						}
					}
				});
		}
	}

	public static void stopNotifyService()
	{
		if (ha != null && servInt != null)
		{
			ha.runOnUiThread(new Runnable()
				{
					public void run()
					{
						isStarted = false;
						ha.stopService(servInt);
						btn_runServer.setEnabled(true);
						btn_stopServer.setEnabled(false);
					}
				});
		}
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		if (!ServerUtils.checkIfInstalled())
		{
			startActivity(new Intent(mContext, InstallActivity.class));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(0, CONSOLE_CODE, 0, "控制台")
			.setIcon(R.drawable.hardware_dock)
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		SubMenu sub = menu.addSubMenu(getString(R.string.abs_settings));
		/**
		 * Set Icon for Submenu
		 */
		sub.setIcon(R.drawable.action_settings);
		/**
		 * Build navigation for submenu
		 */
		// sub.add(0, PROJECT_CODE, 0, getString(R.string.abs_project));
		// sub.add(0, DEV_CODE, 0, getString(R.string.abs_dev));
		sub.add(0, VERSION_MANAGER_CODE, 0, getString(R.string.abs_version_manager));
		// sub.add(0, FILE_MANAGER_CODE, 0,
		// getString(R.string.abs_file_manager));
		sub.add(0, PROPERTIES_EDITOR_CODE, 0, getString(R.string.abs_properties_editor));
		sub.add(0, FORCE_CLOSE_CODE, 0, getString(R.string.abs_force_close));
		sub.add(0, SETTINGS, 0, "设置");
		sub.add(0, ABOUT_US_CODE, 0, getString(R.string.abs_about));
		if (BuildConfig.DEBUG)
		{
			sub.add(0, PM_EDIT_CODE, 0, "配置编辑");
			sub.add(0, DEV_CODE, 0, "开发者");
		}
		// sub.add(0, SETTING_CODE, 0, getString(R.string.abs_settings));
		sub.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case FILE_MANAGER_CODE:
				startActivity(new Intent(mContext, FileManagerActivity.class));
				break;
			case VERSION_MANAGER_CODE:
				startActivity(new Intent(mContext, VersionManagerActivity.class));
				break;
			case PROPERTIES_EDITOR_CODE:
				startActivity(new Intent(mContext, ConfigActivity.class));
				break;
			case FORCE_CLOSE_CODE:
				btn_runServer.setEnabled(true);
				btn_stopServer.setEnabled(false);
				ServerUtils.stopServer();
				if (servInt != null)
					stopService(servInt);
				isStarted = false;
				break;
			case SETTINGS:
				startActivity(new Intent(mContext, SettingActivity.class));
				break;
			case ABOUT_US_CODE:
				startActivity(new Intent(mContext, About.class));
				break;
			case CONSOLE_CODE:
				startActivity(new Intent(mContext, LogActivity.class));
				break;
			case PM_EDIT_CODE:
				startActivity(new Intent(mContext, ConfigurationActivity.class));
				break;
			case DEV_CODE:
				startActivity(new Intent(mContext, DeveloperActivity.class));
				break;
			default:
				break;
		}
		return true;
	}

	public static void hangUp()
	{
		ha.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					hangUp();
				}
			});
	}

	/*
	 * final protected boolean isServerRunning() throws IOException {
	 * InputStream is; java.io.BufferedReader bf; boolean isRunning = false; try
	 * { is = Runtime.getRuntime().exec("ps").getInputStream(); bf = new
	 * java.io.BufferedReader(new java.io.InputStreamReader(is));
	 * 
	 * String r; while ((r = bf.readLine()) != null) { if (r.contains("php")) {
	 * isRunning = true; break; }
	 * 
	 * } is.close(); bf.close();
	 * 
	 * } catch (IOException e) { e.printStackTrace();
	 * 
	 * } return isRunning;
	 * 
	 * }
	 */
}
