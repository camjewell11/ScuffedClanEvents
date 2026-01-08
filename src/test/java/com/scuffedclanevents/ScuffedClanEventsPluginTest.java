package com.scuffedclanevents;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ScuffedClanEventsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ScuffedClanEventsPlugin.class);
		RuneLite.main(args);
	}
}