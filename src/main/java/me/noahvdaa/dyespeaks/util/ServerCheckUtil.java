package me.noahvdaa.dyespeaks.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

public class ServerCheckUtil {
	public static boolean isOnDyescape() {
		ServerInfo server = MinecraftClient.getInstance().getCurrentServerEntry();

		if (server == null)
			return false; // Not on a server (singleplayer).

		String ip = server.address.toLowerCase();

		return ip.endsWith(".dyescape.com") || ip.equals("delayscape.com");
	}
}
