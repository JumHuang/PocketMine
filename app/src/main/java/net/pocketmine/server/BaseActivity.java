package net.pocketmine.server;

import android.os.*;
import android.support.v7.app.*;
import android.view.*;
import android.widget.*;
import android.content.*;

public class BaseActivity extends AppCompatActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		//解决键盘遮挡编辑框方法
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
		{
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(
				WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
				WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // Translucent navigation bar
            /*window.setFlags(
			 WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
			 WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);*/
        }
	}

	public void Toast(String message)
	{
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}

	public void Toast(String message, int time)
	{
		Toast.makeText(getApplicationContext(), message, time).show();
	}

	public static void Toast(Context context, String message)
	{
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	public static void Toast(Context context, String message, int time)
	{
		Toast.makeText(context, message, time).show();
	}
}
