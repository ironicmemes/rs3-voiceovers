package com.rs3voiceovers;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RS3VoiceoversPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(RS3VoiceoversPlugin.class);
		RuneLite.main(args);
	}
}