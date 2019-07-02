package net.pocketmine.server;

import android.os.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;

import android.support.v7.widget.Toolbar;

public class ConfigurationActivity extends BaseActivity
{
	private Toolbar toolbar;

	private TextView tv;

	private LinkedHashMap<String,String> data=new LinkedHashMap<String,String>();
	private ArrayList<HashMap<String,String>> datas=new ArrayList<HashMap<String,String>>();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_configuration);

		toolbar = (Toolbar)findViewById(R.id.configuration_toolbar);
		tv = (TextView)findViewById(R.id.configuration_tv);

		setSupportActionBar(toolbar);   
		//getSupportActionBar().setTitle("配置编辑");  
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

		readFile();

		new AlertDialog.Builder(ConfigurationActivity.this)
			.setTitle("提示")
			.setMessage(data.toString())
			.create().show();

		for (int i=0;i < datas.size();i++)
		{
			tv.setText(tv.getText().toString()+"\n"+datas.get(i).toString());
		}
	}

	private void readFile()
	{
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(ServerUtils.getDataDirectory() + "/pocketmine.yml"));
			try
			{
				String line;

				while ((line = reader.readLine()) != null)
				{
					if (!line.startsWith(" #"))
					{
						int iof = line.indexOf(":");
						if (iof == -1)
						{
							Log.e("Configuration parser", "Invalid entry: " + line);
						}
						else
						{
							String name = line.substring(0, iof);
							String value = line.substring(iof + 1);
							Log.d("Configuration parser", "[Parsing] Name: " + name + " Value: " + value);
							data.put(name, value);
							
							HashMap<String,String> map=new HashMap<String,String>();
							map.put(name,value);
							datas.add(map);
						}
					}
				}
			}
			finally
			{
				reader.close();
			}
		}
		catch (FileNotFoundException e)
		{
			// File not found, it's all
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
