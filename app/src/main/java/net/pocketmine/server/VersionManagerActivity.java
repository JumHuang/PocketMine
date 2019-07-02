package net.pocketmine.server;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import java.io.*;
import java.net.*;

public class VersionManagerActivity extends BaseActivity
{
	public ArrayAdapter<CharSequence> adapter;
	private Boolean install = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		install = getIntent().getBooleanExtra("install", false);
		setContentView(R.layout.version_manager);

		start();
	}

	public String getPageContext(String url) throws IOException
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(new URL(
																		 url).openStream()));
		StringBuilder sb = new StringBuilder();
		String str;
		while ((str = in.readLine()) != null)
		{
			sb.append(str);
		}
		in.close();
		return sb.toString();
	}

	private void start()
	{
		final ProgressBar pbar = (ProgressBar) findViewById(R.id.loadingBar);
		final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
		final Button skip = (Button) findViewById(R.id.skipBtn);
		skip.setOnClickListener(new OnClickListener() 
			{
				@Override
				public void onClick(View v)
				{
					finish();
				}
			});

		pbar.setVisibility(View.VISIBLE);
		scrollView.setVisibility(View.GONE);
		skip.setVisibility(View.GONE);

		new Thread(new Runnable() 
			{
				@Override
				public void run()
				{
					try
					{
						runOnUiThread(new Runnable()
							{
								@Override
								public void run()
								{
									TextView softVersionView = (TextView) findViewById(R.id.soft_version);
									TextView softDateView = (TextView) findViewById(R.id.soft_date);
									Button softDownload = (Button) findViewById(R.id.download_soft);
									TextView stableVersionView = (TextView) findViewById(R.id.stable_version);
									TextView stableDateView = (TextView) findViewById(R.id.stable_date);
									Button stableDownload = (Button) findViewById(R.id.download_stable);
									TextView betaVersionView = (TextView) findViewById(R.id.beta_version);
									TextView betaDateView = (TextView) findViewById(R.id.beta_date);
									Button betaDownload = (Button) findViewById(R.id.download_beta);
									TextView devVersionView = (TextView) findViewById(R.id.dev_version);
									TextView devDateView = (TextView) findViewById(R.id.dev_date);
									Button devDownload = (Button) findViewById(R.id.download_dev);

//									softVersionView.setText("版本：" + softVersion + " (API：" + softAPI + ")");
//									softDateView.setText(softDate);
//									softDownload.setOnClickListener(new OnClickListener() 
//										{
//											@Override
//											public void onClick(View v)
//											{
//												download(softDownloadURL, softVersion);
//											}
//										});
//
//									stableVersionView.setText("版本：" + stableVersion + " (API：" + stableAPI + ")");
//									stableDateView.setText(stableDate);
//									stableDownload.setOnClickListener(new OnClickListener()
//										{
//											@Override
//											public void onClick(View v)
//											{
//												download(stableDownloadURL, stableVersion);
//											}
//										});
//
//									betaVersionView.setText("版本: " + betaVersion + " (API：" + betaAPI + ")");
//									betaDateView.setText(betaDate);
//									betaDownload.setOnClickListener(new OnClickListener()
//										{
//											@Override
//											public void onClick(View v)
//											{
//												download(betaDownloadURL, betaVersion);
//											}
//										});
//
//									devVersionView.setText("版本: " + devVersion + " (API：" + devAPI + ")");
//									devDateView.setText(devDate);
//									devDownload.setOnClickListener(new OnClickListener()
//										{
//											@Override
//											public void onClick(View v)
//											{
//												download(devDownloadURL, devVersion);
//											}
//										});

									pbar.setVisibility(View.GONE);
									scrollView.setVisibility(View.VISIBLE);
									if (install)
									{
										skip.setVisibility(ServerUtils.checkIfInstalled() ? View.VISIBLE: View.GONE);
									}
								}
							});
					}
					catch (Exception err)
					{
						err.printStackTrace();
						if (install)
						{
							Toast("加载版本列表失败！5秒后重试！");
							try
							{
								Thread.sleep(5000);
							}
							catch (InterruptedException e)
							{
								e.printStackTrace();
								runOnUiThread(new Runnable()
									{
										@Override
										public void run()
										{
											finish();
										}
									});
								return;
							}
							start();
						}
						else
						{
							Toast("加载版本列表失败！");
							runOnUiThread(new Runnable() 
								{
									@Override
									public void run()
									{
										finish();
									}
								});
						}
					}
				}
			}).start();
	}

	private void download(final String address, final String fver)
	{
		File vdir = new File(ServerUtils.getDataDirectory() + "/versions/");
		if (!vdir.exists())
		{
			vdir.mkdirs();
		}

		final VersionManagerActivity ctx = this;
		runOnUiThread(new Runnable() 
			{
				@Override
				public void run()
				{
					final ProgressDialog dlDialog = new ProgressDialog(ctx);
					dlDialog.setMax(100);
					dlDialog.setTitle("下载这个版本中...");
					dlDialog.setMessage("请稍等...");
					dlDialog.setIndeterminate(false);
					dlDialog.setCancelable(false);
					dlDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					dlDialog.show();
					dlDialog.setProgress(0);
					new Thread(new Runnable() 
						{
							@Override
							public void run()
							{
								try
								{
									URL url = new URL(address);
									URLConnection connection = url.openConnection();
									connection.connect();
									int fileLength = connection.getContentLength();

									InputStream input = new BufferedInputStream(url.openStream());
									OutputStream output = new FileOutputStream(ServerUtils.getDataDirectory() + "/versions/" + fver + ".phar");

									byte data[] = new byte[1024];
									long total = 0;
									int count;
									int lastProgress = 0;
									while ((count = input.read(data)) != -1)
									{
										total += count;
										int progress = (int) (total * 100 / fileLength);
										if (progress != lastProgress)
										{
											dlDialog.setProgress(progress);
											lastProgress = progress;
										}
										output.write(data, 0, count);
									}

									output.flush();
									output.close();
									input.close();
								}
								catch (Exception e)
								{
									e.printStackTrace();
									Toast("下载这个版本失败！");
									dlDialog.dismiss();
									return;
								}
								dlDialog.dismiss();

								install(fver);
								// dlDialog.setTitle("安装这个版本中...");
								// dlDialog.show();
							}
						}).start();
				}
			});
	}

	private void install(CharSequence ver)
	{
		final VersionManagerActivity ctx = this;
		final CharSequence fver = ver;

		runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					final ProgressDialog iDialog = new ProgressDialog(ctx);
					iDialog.setMax(100);
					iDialog.setTitle("安装这个版本中...");
					iDialog.setMessage("请稍等...");
					iDialog.setIndeterminate(false);
					iDialog.setCancelable(false);
					iDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					iDialog.show();
					iDialog.setProgress(0);

					new Thread(new Runnable() 
						{
							@Override
							public void run()
							{
								try
								{
									new File(ServerUtils.getDataDirectory() + "/PocketMine-MP.php").delete();
									delete(new File(ServerUtils.getDataDirectory() + "/src/"));
									new File(ServerUtils.getDataDirectory() + "/PocketMine-MP.phar").delete();
									FileInputStream in = new FileInputStream(ServerUtils.getDataDirectory() + "/versions/" + fver + ".phar");

									FileOutputStream out = new FileOutputStream(ServerUtils.getDataDirectory() + "/PocketMine-MP.phar");
									byte[] buffer = new byte[1024];
									int len;
									while ((len = in.read(buffer)) > 0)
									{
										out.write(buffer, 0, len);
									}
									in.close();
									out.close();

									runOnUiThread(new Runnable()
										{
											@Override
											public void run()
											{
												if (install)
												{
													Intent ver = new Intent(VersionManagerActivity.this, ConfigActivity.class);
													ver.putExtra("install", true);
													startActivity(ver);
												}
												ctx.finish();
											}
										});
								}
								catch (Exception e)
								{
									Toast("安装这个版本失败！");
									e.printStackTrace();
								}
							}
						}).start();
				}
			});
	}

	public void delete(File f)
	{
		if (f.isDirectory())
		{
			File[] files = f.listFiles();
			for (File file : files)
			{
				delete(file);
			}
		}
		f.delete();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK && install)
		{
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
