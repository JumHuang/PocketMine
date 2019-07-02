package net.pocketmine.server;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.net.*;
import android.net.http.*;
import android.os.*;
import android.support.annotation.*;
import android.support.v4.app.*;
import android.support.v4.view.*;
import android.support.v4.view.ViewPager.*;
import android.support.v4.widget.*;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import java.util.regex.*;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.TaskStackBuilder;


public class FloatWindowService extends Service
{
	private FrameLayout mFloatLayout,note;
	private LinearLayout mdefault,webs,websearch,webtool;
	private ViewPager container;
	private WindowManager.LayoutParams wmParams= new WindowManager.LayoutParams();;
    private WindowManager mWindowManager;

	private int lastX, lastY;
	private int paramX, paramY;

    private ImageView mFloatView,cancel,done,close,focus,web,goback,home,goforward,move;
	private EditText title,search;
	private WebView webview;
	private SwipeRefreshLayout wsr;
	private ProgressBar progressbar;

    @Override
    public void onCreate()
	{
        super.onCreate();
		LayoutInflater inflater = LayoutInflater.from(getApplication());
        mFloatLayout = (FrameLayout) inflater.inflate(R.layout.floatwindow, null);
        createFloatView(mFloatLayout, 0, 0, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

		mFloatView = (ImageView) mFloatLayout.findViewById(R.id.floatwindow_icon);
		mdefault = (LinearLayout)mFloatLayout.findViewById(R.id.floatwindow_default);
		webs = (LinearLayout)mFloatLayout.findViewById(R.id.floatwindow_webs);
		websearch = (LinearLayout)mFloatLayout.findViewById(R.id.floatwindow_websearch);
		webtool = (LinearLayout)mFloatLayout.findViewById(R.id.floatwindow_webtool);
		container = (ViewPager)mFloatLayout.findViewById(R.id.floatwindow_container);
		note = (FrameLayout)mFloatLayout.findViewById(R.id.floatwindow_note);
		cancel = (ImageView)mFloatLayout.findViewById(R.id.floatwindow_cancel);
		done = (ImageView)mFloatLayout.findViewById(R.id.floatwindow_done);
		close = (ImageView)mFloatLayout.findViewById(R.id.floatwindow_close);
		focus = (ImageView)mFloatLayout.findViewById(R.id.floatwindow_focus);
		web = (ImageView)mFloatLayout.findViewById(R.id.floatwindow_web);
		goback = (ImageView)mFloatLayout.findViewById(R.id.floatwindow_goback);
		home = (ImageView)mFloatLayout.findViewById(R.id.floatwindow_home);
		goforward = (ImageView)mFloatLayout.findViewById(R.id.floatwindow_goforward);
		move = (ImageView)mFloatLayout.findViewById(R.id.floatwindow_move);
		search = (EditText)mFloatLayout.findViewById(R.id.floatwindow_search);
		webview = (WebView)mFloatLayout.findViewById(R.id.floatwindow_webview);
		wsr = (SwipeRefreshLayout)mFloatLayout.findViewById(R.id.floatwindow_wsr);
		progressbar = (ProgressBar)mFloatLayout.findViewById(R.id.floatwindow_progressbar);

		Toast("本人表示悬浮窗开启后返回键会失效，暂无办法，如有建议可以去〈关于〉中联系我们。");

		container.setAdapter(new FragmentPagerAdapter());

		LoadWeb();

		container.addOnPageChangeListener(new OnPageChangeListener()
			{
				@Override
				public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
				{

				}

				@Override
				public void onPageSelected(int position)
				{
					//findViewById(navigation.getMenu().getItem(position).getItemId()).callOnClick();
				}

				@Override
				public void onPageScrollStateChanged(int state)
				{

				}
			});

        mFloatView.setOnTouchListener(new View.OnTouchListener()
			{
				int lastX, lastY;
				int paramX, paramY;
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					switch (event.getAction())
					{
						case MotionEvent.ACTION_DOWN:
							lastX = (int) event.getRawX();
							lastY = (int) event.getRawY();
							paramX = wmParams.x;
							paramY = wmParams.y;
							break;
						case MotionEvent.ACTION_MOVE:
							wmParams.x = paramX + (int)event.getRawX() - lastX;
							wmParams.y = paramY + (int) event.getRawY() - lastY;
							mWindowManager.updateViewLayout(mFloatLayout, wmParams);
							break;
						case 1:
							if (paramX - wmParams.x == 0 && paramY - wmParams.y == 0)
							{
								note.setVisibility(View.VISIBLE);
								mFloatView.setVisibility(View.GONE);

								wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
								wmParams.x = paramX;
								wmParams.y = paramY;
								mWindowManager.updateViewLayout(mFloatLayout, wmParams);
							}
							break;
					}
					return false;
				}
			});

		mFloatView.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{

				}
			});

		cancel.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					if (webs.getVisibility() == View.VISIBLE)
					{
						search.setText(webview.getUrl());
					}
					else
					{
						title.setText(null);
						done.setVisibility(View.GONE);
						cancel.setVisibility(View.GONE);
					}
				}
			});

		done.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{

				}
			});

		done.setOnKeyListener(new View.OnKeyListener()
			{
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event)
				{
					if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) 
					{

					}
					return false;
				}
			});

		close.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					try
					{
						stopService(new Intent(getApplicationContext(), FloatWindowService.class));
					}
					catch (Exception e)
					{
						Toast.makeText(getApplicationContext(), "悬浮窗关闭失败！" + e.getCause().toString(), Toast.LENGTH_SHORT).show();
					}
				}
			});

		focus.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					note.setVisibility(View.GONE);
					mFloatView.setVisibility(View.VISIBLE);

					wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
					//取消后两行注释后，每缩放悬浮窗，图标就会在屏幕中间
					//wmParams.x = paramX;
					//wmParams.y = paramY;
					mWindowManager.updateViewLayout(mFloatLayout, wmParams);
				}
			});

		web.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					if (webs.getVisibility() == View.VISIBLE)
					{
						webs.setVisibility(View.GONE);
						websearch.setVisibility(View.GONE);
						webtool.setVisibility(View.GONE);
						mdefault.setVisibility(View.VISIBLE);
						cancel.setVisibility(View.GONE);
						done.setVisibility(View.GONE);
					}
					else
					{
						webs.setVisibility(View.VISIBLE);
						websearch.setVisibility(View.VISIBLE);
						webtool.setVisibility(View.VISIBLE);
						mdefault.setVisibility(View.GONE);
						cancel.setVisibility(View.VISIBLE);
						done.setVisibility(View.VISIBLE);
					}
					title.setText(null);
				}
			});

		goback.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					if (webview.canGoBack())
					{
						webview.goBack();
					}
					else
					{
						Toast("到底了！");
					}
				}
			});

		home.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					webview.loadUrl("http://wap.baidu.com");
				}
			});

		goforward.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					if (webview.canGoForward())
					{
						webview.goForward();
					}
					else
					{
						Toast("到顶了！");
					}
				}
			});

		move.setOnTouchListener(new View.OnTouchListener()
			{
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					switch (event.getAction())
					{
						case MotionEvent.ACTION_DOWN:
							lastX = (int) event.getRawX();
							lastY = (int) event.getRawY();
							paramX = wmParams.x;
							paramY = wmParams.y;
							break;
						case MotionEvent.ACTION_MOVE:
							wmParams.x = paramX + (int)event.getRawX() - lastX;
							wmParams.y = paramY + (int) event.getRawY() - lastY;
							mWindowManager.updateViewLayout(mFloatLayout, wmParams);
							break;
						case 1:
							if (paramX - wmParams.x == 0 && paramY - wmParams.y == 0)
							{
								Toast("yes");
							}
							break;
					}
					return false;
				}
			});

		move.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{

				}
			});

		search.setOnFocusChangeListener(new View.OnFocusChangeListener()
			{
				@Override
				public void onFocusChange(View v, boolean hasFocus)
				{
					if (hasFocus)
					{
						search.setText(webview.getUrl());
						search.setSelection(7);  //把焦点移动到最后一个“/”后面
					}
					else
					{
						search.setText(webview.getTitle());
					}
				}
			});
    }

	private class FragmentPagerAdapter extends FragmentStatePagerAdapter
	{
        public FragmentPagerAdapter(FragmentManager fm)
		{
            super(fm);
        }

		@Override
        public Fragment getItem(int position)
		{
            switch (position)
			{
                case 0:
                    return new CodeFragment();
                case 1:
                    return new ToolFragment();
				case 2:
                    return new ArticleFragment();
                default:
                    return new CodeFragment();
            }
        }

		@Override
        public int getCount()
		{
            return 3;
        }
    }

    private void createFloatView(View v, int x, int y, int height, int width)
	{
        mWindowManager = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
            wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
		else
		{
            wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.CENTER;//Gravity.LEFT | Gravity.TOP;  //初始窗口位置
        wmParams.x = x;
        wmParams.y = y;
        wmParams.width = width;
        wmParams.height = height;
		mWindowManager.addView(v, wmParams);
	}

	private void LoadWeb()
	{
		webview.loadUrl("http://wap.baidu.com");

		WebSettings settings=webview.getSettings();
		settings.setUserAgentString("Agent: Mozilla/5.0 (Linux; U; Android 5.1; zh-cn; HUAWEI TAG-TL00 Build/TAG-TL00) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/39.0.0.0 Mobile Safari/537.36");
		settings.setJavaScriptEnabled(true); //允许网页加载JS文件
		settings.setDefaultTextEncodingName("utf-8"); //utf-8网页编码
		settings.setAllowFileAccess(true); //可访问文件
		settings.setSupportZoom(true); //允许缩放
		settings.setBuiltInZoomControls(true);  //原网页基础上缩放
		settings.setUseWideViewPort(true); //任意比例缩放
		settings.setJavaScriptCanOpenWindowsAutomatically(true); //支持js弹窗
        settings.setGeolocationEnabled(true);
        settings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
		settings.setDisplayZoomControls(false); //隐藏原生的缩放控件
        settings.setLoadsImagesAutomatically(true); //支持自动加载图片
        settings.setTextSize(WebSettings.TextSize.NORMAL);
		settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		settings.setSavePassword(true);
		settings.setAppCacheEnabled(true);
		settings.setAppCacheMaxSize(10 * 1024);
		settings.setDatabaseEnabled(true);
		settings.setSupportMultipleWindows(true);
		settings.setSaveFormData(true);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
            webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

		wsr.setColorSchemeResources(android.R.color.holo_red_light, android.R.color.holo_green_light, android.R.color.holo_blue_light);
		wsr.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
			{
				@Override
				public void onRefresh()
				{
					new Handler().postDelayed(new Runnable()
						{
							@Override
							public void run()
							{
								webview.reload();
								wsr.setRefreshing(false);
							}
						}, 3000);
				}
			});

		// 设置子视图是否允许滚动到顶部
		wsr.setOnChildScrollUpCallback(new SwipeRefreshLayout.OnChildScrollUpCallback() 
			{
				@Override
				public boolean canChildScrollUp(SwipeRefreshLayout parent, @Nullable View child)
				{
					return webview.getScrollY() > 0;
				}
			});

		webview.setDownloadListener(new DownloadListener()
			{
				@Override  
				public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength)
				{             
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
				}  
			}); 

		webview.setOnTouchListener(new View.OnTouchListener()
			{
				@Override
				public boolean onTouch(View p1, MotionEvent p2)
				{
					switch (p2.getAction())
					{
						case MotionEvent.ACTION_DOWN:
							break;
						case MotionEvent.ACTION_UP:

							if (!p1.hasFocus())
							{
								p1.requestFocus();
							}
							break;
					}
					return false;
				}
			});

		webview.setWebChromeClient(new WebChromeClient()
			{
				@Override
				public void onProgressChanged(WebView view, int newProgress)
				{
					/*super.onProgressChanged(view, newProgress);
					 setProgress(newProgress * 100);*/
					progressbar.setProgress(newProgress * 100);
				}

				@Override
				public void onReceivedTitle(WebView view, String title)
				{
					super.onReceivedTitle(view, title);
				}

				@Override
				public void onReceivedIcon(WebView view, Bitmap icons)
				{
					super.onReceivedIcon(view, icons);
				}

				@Override
				public boolean onJsAlert(WebView view, String url, String message, JsResult result)
				{
					return super.onJsAlert(view, url, message, result);
				}

				@Override
				public boolean onJsConfirm(WebView view, String url, String message, JsResult result) 
				{
					return super.onJsConfirm(view, url, message, result);
				}

				@Override
				public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result)
				{
					return super.onJsPrompt(view, url, message, defaultValue, result);
				}
			});

		webview.setWebViewClient(new WebViewClient()
			{
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url)
				{
					webview.loadUrl(url);
					return true;
				}

				@Override
				public void onPageStarted(WebView view, final String url, Bitmap favicon) 
				{
					super.onPageStarted(view, url, favicon);
					progressbar.setVisibility(View.VISIBLE); 
				}

				@Override
				public void onPageFinished(WebView view, String url) 
				{
					super.onPageFinished(view, url);
					progressbar.setVisibility(View.GONE); 
				}

				@Override
				public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) 
				{
					super.onReceivedError(view, errorCode, description, failingUrl);
					progressbar.setVisibility(View.GONE); 
				}

				@Override
				public void onLoadResource(WebView view, String url)
				{
					super.onLoadResource(view, url);
				}

				//从API11至API21
				@Override
				public WebResourceResponse shouldInterceptRequest(WebView view, String url)
				{
					return super.shouldInterceptRequest(view, url);
				}

				@Override
				public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error)
				{
					super.onReceivedSslError(view, handler, error);
				}
			});
	}

	private void Search(String input)
	{
		if (input.isEmpty())
		{
			Toast("搜索内容不能为空！");
		}
		else if (URLUtil.isFileUrl(input) ||
				 URLUtil.isHttpsUrl(input) ||
				 URLUtil.isHttpUrl(input))
		{
			webview.loadUrl(input);
		}
		else if (isURL(input))
		{
			webview.loadUrl("http://" + input);
		}
		else
		{
			webview.loadUrl("https://m.baidu.com/s?from=1011440l&word=" + input);
		}
	}

	private void Toast(String message)
	{
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}

	private void Toast(String message, int time)
	{
		Toast.makeText(getApplicationContext(), message, time).show();
	}

	private boolean isURL(String str) 
	{
		Pattern pattern = Pattern.compile("((http|ftp|https)://)(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?|(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?");
		Matcher matcher = pattern.matcher(str);
		return matcher.matches();
	} 

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		simpleNotification();
		return super.onStartCommand(intent, flags, startId);
	}

	public void simpleNotification()
	{
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder =
			new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle("PocketMine-MP")
			.setAutoCancel(true)
			.setContentText("正在后台运行...");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
            NotificationChannel channel = new NotificationChannel("1", "channel1", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.setShowBadge(true);
            channel.enableVibration(true);

            if (mNotificationManager != null)
			{
                mNotificationManager.createNotificationChannel(channel);
            }
        }
		else
		{
            builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);
        }

        Intent intent =new Intent(getApplicationContext(), HomeActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(001, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        if (mNotificationManager != null)
		{
            mNotificationManager.notify(1, builder.build());
        }
    }

	@Nullable
    @Override
    public IBinder onBind(Intent intent)
	{
        return null;
    }

    @Override
    public void onDestroy()
	{
        super.onDestroy();
		webview.clearCache(true);
        if (mFloatLayout != null)
		{
            mWindowManager.removeView(mFloatLayout);
        }
    }
}
