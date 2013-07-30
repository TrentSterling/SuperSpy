package com.trentsterling.superspy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public class SuperSpyPlugin extends JavaPlugin implements Listener
{

	public void onEnable()
	{
		super.onEnable();
		getServer().getPluginManager().registerEvents(this, this);
	}

	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3)
	{
		if (!(sender instanceof Player))
		{
			sender.sendMessage("This command can only be run by a PLAYER.");
			return true;
		}
		Player player = (Player) sender;
		if (!player.isOp())
		{
			sender.sendMessage("This command can only be run by an OPERATOR.");
			return true;
		}
		World world = player.getWorld();
		world.playSound(player.getLocation(), Sound.LEVEL_UP, 10.0F, 10.0F);
		Boolean testbool = Boolean.valueOf(((MetadataValue) player.getMetadata("SuperSpyEnabled").get(0)).asBoolean());
		if (testbool.booleanValue() == true)
		{
			sender.sendMessage(ChatColor.AQUA + "SuperSpy Disabled!");
			player.setMetadata("SuperSpyEnabled", new FixedMetadataValue(this, Boolean.valueOf(false)));
		}
		else
		{
			sender.sendMessage(ChatColor.AQUA + "SuperSpy Enabled!");
			player.setMetadata("SuperSpyEnabled", new FixedMetadataValue(this, Boolean.valueOf(true)));
		}
		return true;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player p = event.getPlayer();
		if (p.isOp())
		{
			p.sendMessage("SuperSpy Disabled");
			p.setMetadata("SuperSpyEnabled", new FixedMetadataValue(this, Boolean.valueOf(false)));
		}
	}

	@EventHandler
	public void onChatMessage(AsyncPlayerChatEvent e)
	{
		Player p = e.getPlayer();

		// INTEGRATE WITH LOCALCHATPLUGIN
		String chatmode = String.valueOf(p.getMetadata("ChatMode").get(0).asString());

		if (chatmode == "LOCAL")
		{
			messageOps("CHAT: <" + p.getDisplayName() + "> " + e.getMessage());

		}

	}

	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e)
	{
		Player p = e.getPlayer();
		String message = e.getMessage();

		if (message.startsWith("/login"))
		{
			messageOps("CMD: <" + p.getDisplayName() + "> " + ChatColor.DARK_RED + "LOGGED IN");
		}
		else if (message.startsWith("/register"))
		{
			// messageOps("CMD: <" + p.getDisplayName() + "> " + message);
			messageOps("CMD: <" + p.getDisplayName() + "> " + ChatColor.DARK_RED + "REGISTERED");
		}
		else
		{
			messageOps("CMD: <" + p.getDisplayName() + "> " + message);
		}
	}

	public void messageOps(String msg)
	{
		for (Player p : Bukkit.getOnlinePlayers())
		{
			if (p.isOp())
			{
				Boolean testbool = Boolean.valueOf(((MetadataValue) p.getMetadata("SuperSpyEnabled").get(0)).asBoolean());
				if (testbool.booleanValue() == true)
				{
					p.sendMessage("SS " + msg);
				}
			}
		}
	}
}
