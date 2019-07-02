package net.pocketmine.server;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.preference.*;
import android.support.v7.widget.*;
import android.view.*;
import java.io.*;
import java.util.zip.*;

public class SettingActivity extends BaseActivity implements Preference.OnPreferenceChangeListener
{
	private Toolbar toolbar;
	private Preference php;
	private Preference opensource;
	private Preference about;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		toolbar = (Toolbar)findViewById(R.id.setting_toolbar);

		toolbar.setTitle("设置"); 
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true); 
		toolbar.setNavigationOnClickListener(new View.OnClickListener()
			{ 
				@Override 
				public void onClick(View v)
				{ 
					finish(); 
				} 
			}); 

		PreferenceFragment fragment=new PreferenceFragment()
		{
            public void onCreate(Bundle savedInstanceState)
			{
                super.onCreate(savedInstanceState);
                addPreferencesFromResource(R.xml.settings);

				php = (Preference)findPreference("php");
				opensource = (Preference)findPreference("opensource");
				about = (Preference)findPreference("about");

				php.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
					{
						@Override
						public boolean onPreferenceClick(Preference p1)
						{
							Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
							intent.setType("*/*");
							intent.addCategory(Intent.CATEGORY_OPENABLE);
							startActivityForResult(intent, 1);
							return false;
						}
					});

				opensource.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
					{
						@Override
						public boolean onPreferenceClick(Preference p1)
						{
							/*Intent intent=new Intent(getActivity(), BrowserActivity.class);
							 intent.putExtra("URL", "file:///android_asset/license.html");
							 getActivity().startActivity(intent);*/
							return false;
						}
					});

				about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
					{
						@Override
						public boolean onPreferenceClick(Preference p1)
						{
							startActivity(new Intent(getActivity(), About.class));
							return false;
						}
					});
            }
        };

        getFragmentManager()
            .beginTransaction()
            .replace(R.id.setting_content, fragment)
            .commit(); 
    }

	private void copyStream(InputStream is, OutputStream os, boolean close) throws IOException
	{
		int cou=0;
		byte[] buffer=new byte[8192];
		while ((cou = is.read(buffer)) != -1)
		{
			os.write(buffer, 0, cou);
		}
		if (close)
		{
			is.close();
			os.close();
		}
	}

	private Boolean extract(String zipName)
	{
		try
		{
			InputStream in;
			in = new FileInputStream(zipName);
			ZipInputStream zin = new ZipInputStream(in);
			ZipEntry ze = null;

			while ((ze = zin.getNextEntry()) != null)
			{
				if (!ze.isDirectory())
				{
					File f = new File(ServerUtils.getAppDirectory() + "/" + ze.getName());
					f = new File(f.getParent());
					if (!f.isDirectory())
						f.mkdirs();
					FileOutputStream fout = new FileOutputStream(ServerUtils.getAppDirectory() + "/" + ze.getName());

					byte[] buffer = new byte[4096 * 10];
					int length = 0;
					while ((length = zin.read(buffer)) != -1)
					{
						fout.write(buffer, 0, length);
					}
					zin.closeEntry();
					fout.close();
				}
			}
			zin.close();
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

    @Override
    public boolean onPreferenceChange(Preference preference, Object obj)
	{
        preference.setSummary(obj);
        return true;
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (data == null)
		{
			return;
		}
		final Uri choose=data.getData();
		if (resultCode == Activity.RESULT_OK)
		{
			switch (requestCode)
			{
				case 1:
					ServerUtils.stopServer();
					HomeActivity.btn_runServer.setEnabled(true);
					HomeActivity.btn_stopServer.setEnabled(false);
					new Thread(new Runnable()
						{
							public void run()
							{
								try
								{
									File inside=new File(ServerUtils.getAppDirectory(), "php");
									inside.delete();
									copyStream(getContentResolver().openInputStream(choose), new FileOutputStream(inside), true);
									runOnUiThread(new Runnable()
										{
											public void run()
											{
												Toast("安装成功！");
											}
										});
								}
								catch (Exception e)
								{
									final String ex=e.getMessage();
									runOnUiThread(new Runnable()
										{
											public void run()
											{
												Toast("安装失败！" + ex);
											}
										});
								}
							}
						}).start();
					break;
				default:
					break;
			}
		}
		else if (resultCode == Activity.RESULT_CANCELED)
		{
			Toast("文件选择取消！");
		}
	}
}

