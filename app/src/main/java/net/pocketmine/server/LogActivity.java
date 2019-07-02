package net.pocketmine.server;

import android.annotation.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.support.v7.widget.*;
import android.text.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;

import android.support.v7.widget.Toolbar;
import android.text.ClipboardManager;

@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class LogActivity extends BaseActivity
{
	public static LogActivity logActivity;
	public static ScrollView sv;
	public static SpannableStringBuilder currentLog = new SpannableStringBuilder();

	private Toolbar toolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log);

		toolbar = (Toolbar)findViewById(R.id.log_toolbar);

		setSupportActionBar(toolbar);   
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true); 
		toolbar.setNavigationOnClickListener(new View.OnClickListener() 
			{ 
				@Override 
				public void onClick(View v)
				{ 
					finishAfterTransition(); 
				} 
			}); 

		logActivity = this;
		TextView logTV = (TextView) findViewById(R.id.logTextView);
		logTV.setText(currentLog);

		sv = (ScrollView) findViewById(R.id.logScrollView);
		
		HomeActivity.btn_runServer.setEnabled(!HomeActivity.isStarted);
		HomeActivity.btn_stopServer.setEnabled(HomeActivity.isStarted);
		
		Button btnCmd = (Button) findViewById(R.id.runCommand);
		btnCmd.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View arg0)
				{
					EditText et = (EditText) findViewById(R.id.textCmd);
					if (!et.getText().toString().trim().isEmpty())
					{
						if (et.getText().toString().trim().equals("start"))
						{
							HomeActivity.btn_runServer.setEnabled(false);
							HomeActivity.btn_stopServer.setEnabled(true);
							HomeActivity.btn_runServer.setText(R.string.server_online);
							String msg = "服务器启动失败！";
							if (ServerUtils.isRunning())
							{
								msg = "开启服务器中...";
							}
							Toast(msg, Toast.LENGTH_LONG);

							startService(new Intent(logActivity, ServerService.class));
							HomeActivity.isStarted = true;
							HomeActivity.showStats(true);

							ServerUtils.runServer();
							
							et.setText("");
						}
						else
						{
							log(">" + et.getText().toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"));
							ServerUtils.executeCMD(et.getText().toString());
							et.setText("");
						}
					}
					else
					{
						Toast("请输入指令！");
					}
				}
			});
	}

	final static int CLEAR_CODE = 143;
	final static int COPY_CODE = CLEAR_CODE + 1;

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == CLEAR_CODE)
		{
			currentLog = new SpannableStringBuilder();
			TextView logTV = (TextView) logActivity.findViewById(R.id.logTextView);
			logTV.setText(currentLog);

			return true;
		}
		else if (item.getItemId() == COPY_CODE)
		{
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			clipboard.setText(currentLog);
			Toast("成功复制！");
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		menu.add(0, COPY_CODE, 0, "复制").setIcon(R.drawable.content_copy).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add(0, CLEAR_CODE, 0, "清屏").setIcon(R.drawable.content_discard).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}

	/*public static void log(String str)
	 {
	 boolean ansiMode=true;
	 boolean nukkitMode=false;
	 if (ansiMode)
	 {
	 String stringBuffer = new StringBuffer().append("<font>").append(str.replace(" ", "&nbsp;").replace("\u001b[m", "</font>").replace("\u001b[0m", "</font>").replace("\u001b[1m", "</font><font style=\"font-weight:bold\">").replace("\u001b[3m", "</font><font style=\"font-style:italic\">").replace("\u001b[4m", "</font><font style=\"text-decoration:underline\">").replace("\u001b[8m", "</font><font>").replace("\u001b[9m", "</font><font style=\"text-decoration:line-through\">")).toString();
	 str = new StringBuffer().append(nukkitMode ? stringBuffer.replace("\u001b[0;30m", "</font><font color=\"#000000\">").replace("\u001b[0;34m", "</font><font color=\"#0000AA\">").replace("\u001b[0;32m", "</font><font color=\"#00AA00\">").replace("\u001b[0;36m", "</font><font color=\"#00AAAA\">").replace("\u001b[0;31m", "</font><font color=\"#AA0000\">").replace("\u001b[0;35m", "</font><font color=\"#AA00AA\">").replace("\u001b[0;33m", "</font><font color=\"#FFAA00\">").replace("\u001b[0;37m", "</font><font color=\"#AAAAAA\">").replace("\u001b[30;1m", "</font><font color=\"#555555\">").replace("\u001b[34;1m", "</font><font color=\"#5555FF\">").replace("\u001b[32;1m", "</font><font color=\"#55FF55\">").replace("\u001b[36;1m", "</font><font color=\"#55FFFF\">").replace("\u001b[31;1m", "</font><font color=\"#FF5555\">").replace("\u001b[35;1m", "</font><font color=\"#FF55FF\">").replace("\u001b[33;1m", "</font><font color=\"#FFFF55\">").replace("\u001b[37;1m", "</font><font color=\"#FFFFFF\">") : stringBuffer.replace("\u001b[38;5;16m", "</font><font color=\"#000000\">").replace("\u001b[38;5;19m", "</font><font color=\"#0000AA\">").replace("\u001b[38;5;34m", "</font><font color=\"#00AA00\">").replace("\u001b[38;5;37m", "</font><font color=\"#00AAAA\">").replace("\u001b[38;5;124m", "</font><font color=\"#AA0000\">").replace("\u001b[38;5;127m", "</font><font color=\"#AA00AA\">").replace("\u001b[38;5;214m", "</font><font color=\"#FFAA00\">").replace("\u001b[38;5;145m", "</font><font color=\"#AAAAAA\">").replace("\u001b[38;5;59m", "</font><font color=\"#555555\">").replace("\u001b[38;5;63m", "</font><font color=\"#5555FF\">").replace("\u001b[38;5;83m", "</font><font color=\"#55FF55\">").replace("\u001b[38;5;87m", "</font><font color=\"#55FFFF\">").replace("\u001b[38;5;203m", "</font><font color=\"#FF5555\">").replace("\u001b[38;5;207m", "</font><font color=\"#FF55FF\">").replace("\u001b[38;5;227m", "</font><font color=\"#FFFF55\">").replace("\u001b[38;5;231m", "</font><font color=\"#FFFFFF\">")).append("</font><br />").toString();
	 }
	 final CharSequence fromHtml = ansiMode ? Html.fromHtml(str) : new StringBuffer().append(str).append("\n").toString();
	 currentLog.append(fromHtml);

	 logActivity.runOnUiThread(new Runnable()
	 {
	 public void run()
	 {
	 TextView logTV = (TextView) logActivity.findViewById(R.id.logTextView);
	 logTV.append(fromHtml);
	 sv.fullScroll(ScrollView.FOCUS_DOWN);
	 }
	 });
	 }*/

	public static void log(final String whatToLog)
	{
		StringBuilder formatted = new StringBuilder();

		int currentColor = -1;
		boolean bold = false;

		String[] parts = whatToLog.split("\u001B\\[");
		boolean firstPart = true;
		for (String part : parts)
		{
			if (!firstPart)
			{
				int end = part.indexOf("m");
				if (end != -1)
				{
					String[] flags = part.substring(0, end).split(";");
					for (String flag : flags)
					{
						if (flag.startsWith(";"))
							flag = flag.substring(1);
						try
						{
							int n = Integer.parseInt(flag);
							if (n >= 30 && n < 40)
							{
								// text colour
								if (currentColor != -1)
								{
									formatted.append("</font>");
									n = -1;
								}
								// get color
								if (!bold)
								{
									// standard
									if (n == 30)
										currentColor = Color.argb(255, 0, 0, 0);
									else if (n == 31)
										currentColor = Color.argb(255, 128, 0, 0);
									else if (n == 32)
										currentColor = Color.argb(255, 0, 128, 0);
									else if (n == 33)
										currentColor = Color.argb(255, 128, 128, 0);
									else if (n == 34)
										currentColor = Color.argb(255, 0, 0, 128);
									else if (n == 35)
										currentColor = Color.argb(255, 128, 0, 128);
									else if (n == 36)
										currentColor = Color.argb(255, 0, 128, 128);
									else if (n == 37)
										currentColor = Color.argb(255, 128, 128, 128);
								}
								else
								{
									// lighter
									if (n == 30)
										currentColor = Color.argb(255, 85, 85, 85);
									else if (n == 31)
										currentColor = Color.argb(255, 255, 0, 0);
									else if (n == 32)
										currentColor = Color.argb(255, 0, 255, 0);
									else if (n == 33)
										currentColor = Color.argb(255, 255, 255, 0);
									else if (n == 34)
										currentColor = Color.argb(255, 0, 0, 255);
									else if (n == 35)
										currentColor = Color.argb(255, 255, 0, 255);
									else if (n == 36)
										currentColor = Color.argb(255, 0, 255, 255);
									else if (n == 37)
										currentColor = Color.argb(255, 255, 255, 255);
								}

								if (currentColor != -1)
								{
									formatted.append("<font color=\"" + (String.format("#%06X", (0xFFFFFF & currentColor))) + "\">");
								}
							}
							else if (n == 0)
							{
								if (currentColor != -1)
								{
									formatted.append("</font>");
									n = -1;
								}
								if (bold)
								{
									bold = false;
								}
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					part = part.substring(end + 1);
				}
				formatted.append(part);
			}
			else
			{
				formatted.append(part);
				firstPart = false;
			}
		}
		formatted.append("<br/>");
		final Spanned result = Html.fromHtml(formatted.toString());
		currentLog.append(result);

		if (logActivity != null)
		{
			logActivity.runOnUiThread(new Runnable()
				{
					public void run()
					{
						TextView logTV = (TextView) logActivity.findViewById(R.id.logTextView);
						logTV.append(result);
						//sv.fullScroll(ScrollView.FOCUS_DOWN);
						sv.smoothScrollBy(0, sv.getChildAt(sv.getChildCount() - 1).getBottom());
					}
				});
		}
	}
}
