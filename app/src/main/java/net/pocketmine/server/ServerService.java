/**
 * This file is part of DroidPHP
 *
 * (c) 2013 Shushant Kumar
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package net.pocketmine.server;

import android.app.*;
import android.content.*;
import android.os.*;
import android.support.v4.app.*; 

public class ServerService extends Service
{
	private boolean isRunning = false;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		run();

		return (START_NOT_STICKY);
	}

	@Override
	public void onDestroy()
	{
		stop();
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return (null);
	}

	@SuppressWarnings("deprecation")
	private void run()
	{
		if (!isRunning)
		{
			isRunning = true;

			Context context = getApplicationContext();

			NotificationCompat.Builder builder=(NotificationCompat.Builder)new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("PocketMine-MP运行中...")
				.setContentText("点击这里打开PocketMine-MP");

			Intent intent = new Intent(context, HomeActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 1337, intent, 0);
			builder.setContentIntent(pendingIntent); 

			NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(1337, builder.build());
		}
	}

	private void stop()
	{
		if (isRunning)
		{
			isRunning = false;
			stopForeground(true);
		}
	}
}
