package net.pocketmine.server;

import android.app.*;
import android.os.*;
import android.widget.*;
import java.io.*;
import java.util.zip.*;

public class InstallerAsync extends AsyncTask<Void, String, Void>
{
	Boolean fromAssets;
	Integer fromWhichAct;
	String orgLoc, toLoc;
	Activity ctx;
	TextView tv_install_exec;

	@Override
	protected Void doInBackground(Void... arg0)
	{
		// floc = ServerUtils.getAppDirectory() + "/";
		// sdloc = ServerUtils.getDataDirectory() + "/";

		if (extract(fromAssets, toLoc, orgLoc))
		{
			// if(extract(sdloc, "sd_data.zip")){
			publishProgress("ok");
			return null;
			// }
		}
		publishProgress("error");

		return null;
	}

	protected Boolean extract(Boolean isAsset, String loc, String zipName)
	{
		try
		{
			// dirChecker("");
			InputStream in;
			if (isAsset)
			{
				in = ctx.getAssets().open(zipName);
			}
			else
			{
				in = new FileInputStream(zipName);
			}
			ZipInputStream zin = new ZipInputStream(in);
			ZipEntry ze = null;

			while ((ze = zin.getNextEntry()) != null)
			{
				if (ze.isDirectory())
				{
					// dirChecker(ze.getName());
				}
				else
				{
					File f = new File(loc + ze.getName());
					f = new File(f.getParent());
					if (!f.isDirectory())
						f.mkdirs();
					FileOutputStream fout = new FileOutputStream(loc + ze.getName());
					publishProgress("解压中：" + ze.getName());

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
	protected void onProgressUpdate(String... values)
	{
		super.onProgressUpdate(values);

		String text = "出错";
		tv_install_exec.setVisibility(1);
		if (values[0] == "error")
			text = ctx.getString(R.string.bin_error);
		else if (values[0] == "ok")
		{
			text = ctx.getString(R.string.bin_installed);
			HomeActivity.btn_runServer.setEnabled(true);
			if (fromWhichAct == 0)
			{
				InstallActivity ia = (InstallActivity) ctx;
				ia.contiuneInstall();
			}
		}
		else
			text = values[0];

		tv_install_exec.setText(text);
		HomeActivity.isStarted = false;
	}

	@Override
	protected void onPostExecute(Void result)
	{
		super.onPostExecute(result);
	}
	
	private void dirChecker(String loc, String dir)
	{
		File f = new File(loc + dir);
		if (!f.isDirectory())
		{
			f.mkdirs();
		}
	}
}
