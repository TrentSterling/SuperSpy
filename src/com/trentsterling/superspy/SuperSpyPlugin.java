package com.trentsterling.superspy;

import java.util.List;

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
	/*
	 * UTILITIES - Metadata, Get players, check if console
	 * 
	 * FIXME: Move these utils to a seperate library.
	 */

	public Boolean isSenderPlayer(CommandSender sender)
	{
		if (sender instanceof Player)
		{
			return true;
		}
		return false;
	}

	public Boolean isSenderConsole(CommandSender sender)
	{
		if (sender instanceof Player)
		{
			return false;
		}
		return true;
	}

	public Boolean hasNeitherOpNorPermission(Player player, String perm)
	{
		if (!player.isOp() && !player.hasPermission(perm))
		{
			return true;
		}
		return false;
	}

	public Boolean hasOpOrPermission(Player player, String perm)
	{
		if (player.isOp() || player.hasPermission(perm))
		{
			return true;
		}
		return false;
	}

	public void setMetadata(Player player, String key, Object value)
	{
		player.setMetadata(key, new FixedMetadataValue(this, value));
	}

	public Object getMetadata(Player player, String key)
	{
		List<MetadataValue> values = player.getMetadata(key);
		// why are we iterating?
		for (MetadataValue value : values)
		{
			return value.value();
		}
		return null;
	}

	public Boolean hasMetadata(Player player, String key)
	{
		if (getMetadata(player, key) != null)
		{
			return true;
		}
		return false;
	}

	/**
	 * Meat of plugin below
	 */
	@Override
	public void onEnable()
	{
		super.onEnable();
		getServer().getPluginManager().registerEvents(this, this);
		for (Player p : Bukkit.getOnlinePlayers())
		{
			forceDisableSpy(p);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3)
	{
		// TODO: Set up onPlayerCommand and onConsoleCommand hooks
		// Check to see if sender is console
		if (isSenderConsole(sender))
		{
			sender.sendMessage("This command can't be run from CONSOLE.");
			return true;
		}

		// Cast sender as player
		Player player = (Player) sender;

		// Check to see if player has perms
		if (hasNeitherOpNorPermission(player, "superspy.admin"))
		{
			sender.sendMessage("This command requires OPERATOR or STAFF permissions.");
			return true;
		}

		// Play a twinkle!
		World world = player.getWorld();
		world.playSound(player.getLocation(), Sound.LEVEL_UP, 10.0F, 10.0F);

		// Temporary state saver. Load metadata into it if it exists.
		Boolean isCurrentlySpyEnabled = true;// fake on - means we disable on next steps
		if (hasMetadata(player, "SuperSpyEnabled"))
		{
			isCurrentlySpyEnabled = (Boolean) getMetadata(player, "SuperSpyEnabled");// get real stored true/false
		}
		else
		{
			// BUGPATCH: Somehow this player doesnt have the metadata. Surely we've added it on join? But miracles happen.
			forceDisableSpy(player);// sets flag disabled
			return true;
		}

		// OK, metadata was set before we ran the command. Lets toggle whatever the setting is.
		if (isCurrentlySpyEnabled.booleanValue() == true)
		{
			forceDisableSpy(player);
		}
		else
		{
			forceEnableSpy(player);
		}
		return true;
	}

	public void forceEnableSpy(Player player)
	{
		player.sendMessage(ChatColor.AQUA + "SuperSpy Enabled!");
		setMetadata(player, "SuperSpyEnabled", true);
	}

	public void forceDisableSpy(Player player)
	{
		player.sendMessage(ChatColor.AQUA + "SuperSpy Disabled!");
		setMetadata(player, "SuperSpyEnabled", false);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player p = event.getPlayer();
		// On join, lets set superspy off
		forceDisableSpy(p);
	}

	@EventHandler
	public void onChatMessage(AsyncPlayerChatEvent e)
	{
		Player p = e.getPlayer();

		// INTEGRATE WITH LOCALCHATPLUGIN
		String currentChatMode = "GLOBAL";

		// Check to see if metadata exists
		if (hasMetadata(p, "chatmode"))
		{
			currentChatMode = (String) getMetadata(p, "chatmode");
		}

		// TODO: Only display chat to the spy if its too far away to be heard with localchat Global and Admin chat would normally show up to anyone using superspy anyway.
		// Will there ever be someone with superspy and not adminchat? Technically people couldnt spy on adminchat or global - this spy is for local only at the moment.
		// Mostly because its easier than sorting all the chattypes. Too many duplicates if we spy on all chat commands existent.
		// This covers all cases in EG servers so far.
		// It is...sufficient.
		if (currentChatMode == "LOCAL")
		{
			messageOpsAndPlayersWithPermission("CHAT: <" + p.getDisplayName() + "> " + ChatColor.DARK_GREEN + e.getMessage());
		}

	}

	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e)
	{
		Player p = e.getPlayer();
		String message = e.getMessage();

		// Block login and register commands from sight
		if (commandMatchString(message, "/login"))
		{
			messageOpsAndPlayersWithPermission("CMD: <" + p.getDisplayName() + "> " + ChatColor.DARK_RED + "LOGGED IN");
		}
		else if (commandMatchString(message, "/register"))
		{
			messageOpsAndPlayersWithPermission("CMD: <" + p.getDisplayName() + "> " + ChatColor.DARK_RED + "REGISTERED");
		}
		else if (commandMatchString(message, "/l "))
		{
			messageOpsAndPlayersWithPermission("CMD: <" + p.getDisplayName() + "> " + ChatColor.DARK_RED + "Tried using /l for login or something");
		}
		else if (commandMatchString(message, "/change"))
		{
			messageOpsAndPlayersWithPermission("CMD: <" + p.getDisplayName() + "> " + ChatColor.DARK_RED + "Changed PW");
		}
		else if (commandMatchString(message, "/xauth"))
		{
			messageOpsAndPlayersWithPermission("CMD: <" + p.getDisplayName() + "> " + ChatColor.DARK_RED + "is messing with XAUTH");
		}
		else if (commandMatchString(message, "/msg") || commandMatchString(message, "/m ") || commandMatchString(message, "/r ") || commandMatchString(message, "/w ")
				|| commandMatchString(message, "/whisper") || commandMatchString(message, "/tell") || commandMatchString(message, "/t "))
		{
			messageOpsAndPlayersWithPermission("MSG: <" + p.getDisplayName() + "> " + ChatColor.YELLOW + message);
		}
		else
		{
			// Go ahead and forward command
			messageOpsAndPlayersWithPermission("CMD: <" + p.getDisplayName() + "> " + ChatColor.RED + message);
		}
	}

	public Boolean commandMatchString(String command, String match)
	{

		return command.toLowerCase().startsWith(match);
	}

	public void messageOpsAndPlayersWithPermission(String msg)
	{
		for (Player p : Bukkit.getOnlinePlayers())
		{
			// Checks to make sure player always has permission.
			// If demoted ingame, SS messages wont get through, even without disabling SS via command.
			// TODO: If demotion is detected (SS enabled, no perms), disable SS
			if (hasOpOrPermission(p, "superspy.admin"))
			{
				// If metadata exists
				if (this.hasMetadata(p, "SuperSpyEnabled"))
				{
					// Grab the data
					Boolean testbool = (Boolean) getMetadata(p, "SuperSpyEnabled");

					// If data == true, superspy is enabled on this player with permissions
					if (testbool == true)
					{
						p.sendMessage("SS " + msg);
					}
				}
			}
		}
	}
}
