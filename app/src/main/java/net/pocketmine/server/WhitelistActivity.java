package net.pocketmine.server;

import android.app.*;
import android.content.*;
import android.os.*;
import android.support.v7.widget.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.*;
import java.io.*;
import java.util.*;

import android.support.v7.widget.Toolbar;

public class WhitelistActivity extends BaseActivity
{
	private Toolbar toolbar;

	ActionMode actionMode = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_whitelist);

		toolbar = (Toolbar)findViewById(R.id.whitelist_toolbar);

		setSupportActionBar(toolbar);   
		//getSupportActionBar().setTitle("Browser");  
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

		final ListView list = (ListView) findViewById(R.id.whitelist_list);

		load();

		// list.setSelector(android.R.color.darker_gray);
		list.setOnItemClickListener(new OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
				{
					SparseBooleanArray arr = list.getCheckedItemPositions();
					boolean checked = false;
					for (int i = 0; i < arr.size() && !checked; i++)
					{
						checked = arr.valueAt(i);
					}

					if (actionMode == null && checked)
					{
						actionMode = startActionMode(new ActionMode.Callback()
							{
								@Override
								public boolean onPrepareActionMode(ActionMode mode, Menu menu)
								{
									return false;
								}

								@Override
								public void onDestroyActionMode(ActionMode mode)
								{
									list.clearChoices();
									for (int i = 0; i < list.getChildCount(); i++)
										list.setItemChecked(i, false);
									actionMode = null;
								}

								@Override
								public boolean onCreateActionMode(ActionMode mode, Menu menu)
								{
									menu.add(0, 1, 0, "清除").setIcon(R.drawable.ic_action_remove).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
									return true;
								}

								@Override
								public boolean onActionItemClicked(ActionMode mode, MenuItem item)
								{
									if (item.getItemId() == 1)
									{
										SparseBooleanArray arr = list.getCheckedItemPositions();
										Boolean needsSave = false;
										for (int i = arr.size() - 1; i >= 0; i--)
										{
											if (arr.valueAt(i))
											{
												entries.remove(arr.keyAt(i));
												needsSave = true;
											}
										}

										if (needsSave)
										{
											save();
											load();
										}

										actionMode.finish();
										return true;
									}
									return false;
								}
							});
					}
					else if (actionMode != null && !checked)
					{
						actionMode.finish();
						actionMode = null;
					}
				}
			});
		list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	}

	ArrayList<String> entries;
	ArrayAdapter<String> adapter;

	public void save()
	{
		try
		{
			PrintWriter writer = new PrintWriter(ServerUtils.getDataDirectory() + "/white-list.txt");
			for (String entry : entries)
			{
				writer.println(entry);
			}
			writer.flush();
			writer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void load()
	{
		ListView list = (ListView) findViewById(R.id.whitelist_list);

		entries = new ArrayList<String>();
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(ServerUtils.getDataDirectory() + "/white-list.txt"));
			try
			{
				String line;

				while ((line = reader.readLine()) != null)
				{
					if (line.length() > 0)
					{
						entries.add(line);
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
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, entries.toArray(new String[entries.size()]));
		list.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater mi=new MenuInflater(this);
		mi.inflate(R.menu.whitelist, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.whitelist_add)
		{
			LinearLayout ll = new LinearLayout(this);
			final EditText input = new EditText(this);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(HomeActivity.dip2px(8), 0, HomeActivity.dip2px(8), 0);
			input.setLayoutParams(layoutParams);
			ll.addView(input);
			new AlertDialog.Builder(this)
				.setTitle("添加白名单玩家")
				.setMessage("请输入您想白名单的玩家")
				.setView(ll)
				.setPositiveButton("添加",
				new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int whichButton)
					{
						entries.add(input.getText().toString().toLowerCase());
						save();
						load();
					}
				}).setNegativeButton("取消", null).show();
			return true;
		}
		return false;
	}
}
