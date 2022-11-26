package com.warmthdawn.liang_sweep.sweeper;

import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.*;

public class ServerUtils {
    public static MinecraftServer server;

    public static void sendMessagesToAll(ITextComponent textComponent) {
        server.getPlayerList().func_232641_a_(textComponent, ChatType.SYSTEM, Util.DUMMY_UUID);
    }

    public static void sendMessagesToAll(String str, Object... val) {
        server.getPlayerList().func_232641_a_(new StringTextComponent(String.format(str, val)), ChatType.SYSTEM, Util.DUMMY_UUID);
    }

}
