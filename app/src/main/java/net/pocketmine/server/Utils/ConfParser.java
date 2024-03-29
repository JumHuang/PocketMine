/**
 * This file is part of DroidPHP
 *
 * (c) 2013 Shushant Kumar
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package net.pocketmine.server.Utils;

import java.io.*;
import java.util.*;
import org.apache.commons.io.*;

public class ConfParser
{
	/**
	 * @param args
	 */
	public HashMap<String, String> Parser(String fileName)
	{
		String s = "";
		try
		{
			s = FileUtils.readFileToString(new File(fileName), "UTF-8");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		String[] mStrArr = s.split("\n");
		HashMap<String, String> map = new HashMap<String, String>();

		for (int i = 0; i < mStrArr.length; ++i)
		{
			/**
			 * If String does not contain "var." just skip it
			 */
			if (false == mStrArr[i].contains("var."))
				continue;
			/**
			 * Now we have found what we are looking for just spit it from = and
			 * convert it in to key and value pair
			 */

			String[] NodeList = mStrArr[i].split("=", 2);// OffSet is 2
			map.put(NodeList[0].trim().replace("var.", ""), filterValue(NodeList[1]).substring(1).trim());
		}
		return map;
	}

	public static String filterValue(String mString)
	{
		StringBuffer sb = new StringBuffer(mString);

		if (mString.startsWith("\"") || mString.startsWith("'"))
		{
			sb.setCharAt(0, ' ');
		}
		if (mString.endsWith("\"") || mString.endsWith("'"))
		{
			sb.deleteCharAt(mString.length() - 1);
		}
		mString = null;

		return sb.toString().trim();
	}
}
