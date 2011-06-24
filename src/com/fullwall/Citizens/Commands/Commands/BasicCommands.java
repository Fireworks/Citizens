package com.fullwall.Citizens.Commands.Commands;

import java.util.ArrayDeque;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.fullwall.Citizens.Citizens;
import com.fullwall.Citizens.Constants;
import com.fullwall.Citizens.Economy.EconomyHandler;
import com.fullwall.Citizens.Economy.EconomyHandler.Operation;
import com.fullwall.Citizens.NPCs.NPCDataManager;
import com.fullwall.Citizens.NPCs.NPCManager;
import com.fullwall.Citizens.Properties.PropertyManager;
import com.fullwall.Citizens.Utils.HelpUtils;
import com.fullwall.Citizens.Utils.MessageUtils;
import com.fullwall.Citizens.Utils.ServerUtils;
import com.fullwall.Citizens.Utils.StringUtils;
import com.fullwall.resources.redecouverte.NPClib.HumanNPC;
import com.fullwall.resources.sk89q.commands.Command;
import com.fullwall.resources.sk89q.commands.CommandContext;
import com.fullwall.resources.sk89q.commands.CommandPermissions;
import com.fullwall.resources.sk89q.commands.CommandRequirements;

@CommandRequirements(requireSelected = true, requireOwnership = true)
public class BasicCommands {

	@CommandRequirements()
	@Command(
			aliases = "citizens",
			usage = "",
			desc = "view Citizens info",
			modifiers = "",
			min = 0,
			max = 0)
	@CommandPermissions("admin")
	public static void viewInfo(CommandContext args, Player player, HumanNPC npc) {
		player.sendMessage(ChatColor.GREEN + "==========[ "
				+ StringUtils.wrap("Citizens") + " ]==========");
		player.sendMessage(ChatColor.GREEN + "  Version: "
				+ StringUtils.wrap(Citizens.getVersion()));
		player.sendMessage(ChatColor.GREEN + "  Authors: ");
		player.sendMessage(ChatColor.YELLOW + "      - fullwall");
		player.sendMessage(ChatColor.YELLOW + "      - aPunch");
	}

	@CommandRequirements()
	@Command(
			aliases = "citizens",
			usage = "help (page)",
			desc = "view the Citizens help page",
			modifiers = "help",
			min = 1,
			max = 1)
	@CommandPermissions("use.basic")
	public static void sendCitizensHelp(CommandContext args, Player player,
			HumanNPC npc) {
		int page = 1;
		if (args.argsLength() == 2) {
			page = Integer.parseInt(args.getString(1));
		}
		HelpUtils.sendHelpPage(player, page);
	}

	@CommandRequirements()
	@Command(
			aliases = "citizens",
			usage = "reload",
			desc = "reload Citizens",
			modifiers = "reload",
			min = 1,
			max = 1)
	@CommandPermissions("admin")
	public static void reload(CommandContext args, Player player, HumanNPC npc) {
		player.sendMessage(ChatColor.GREEN + "[Citizens] Reloading....");
		Constants.setupVariables();
		PropertyManager.registerProperties();
		player.sendMessage(ChatColor.GREEN + "[Citizens] Reloaded.");
	}

	@CommandRequirements()
	@Command(
			aliases = "basic",
			usage = "help (page)",
			desc = "view the Basic NPC help page",
			modifiers = "help",
			min = 1,
			max = 2)
	@CommandPermissions("use.basic")
	public static void sendBasicHelp(CommandContext args, Player player,
			HumanNPC npc) {
		int page = 1;
		if (args.argsLength() == 2) {
			page = Integer.parseInt(args.getString(1));
		}
		HelpUtils.sendBasicHelpPage(player, page);
	}

	@CommandRequirements()
	@Command(
			aliases = "npc",
			usage = "create [name] (text)",
			desc = "create an NPC",
			modifiers = "create",
			min = 2)
	@CommandPermissions("create.basic")
	public static void createNPC(CommandContext args, Player player,
			HumanNPC npc) {
		ArrayDeque<String> texts = new ArrayDeque<String>();
		String firstArg = args.getString(1);
		if (args.argsLength() >= 3) {
			texts.add(args.getJoinedStrings(2));
		}
		if (firstArg.length() > 16) {
			player.sendMessage(ChatColor.RED
					+ "The name of this NPC will be truncated - max name length is 16.");
			firstArg = args.getString(1).substring(0, 16);
		}
		int UID = NPCManager.register(firstArg, player.getLocation(),
				player.getName());
		NPCManager.setText(UID, texts);

		NPCManager.get(UID).getNPCData().setOwner(player.getName());

		player.sendMessage(ChatColor.GREEN + "The NPC "
				+ StringUtils.wrap(firstArg) + " was born!");
		if (EconomyHandler.useEconomy()) {
			double paid = EconomyHandler.pay(Operation.BASIC_CREATION, player);
			if (paid > 0) {
				player.sendMessage(MessageUtils.getPaidMessage(
						Operation.BASIC_CREATION, paid, firstArg, "", false));
			}
		}
		NPCManager.selectedNPCs.put(player.getName(), UID);
		player.sendMessage(ChatColor.GREEN + "You selected NPC "
				+ StringUtils.wrap(firstArg) + ", ID " + StringUtils.wrap(UID)
				+ ".");
	}

	@Command(
			aliases = "npc",
			usage = "move",
			desc = "move an NPC",
			modifiers = "move",
			min = 1,
			max = 1)
	@CommandPermissions("modify.basic")
	public static void moveNPC(CommandContext args, Player player, HumanNPC npc) {
		player.sendMessage(StringUtils.wrap(npc.getStrippedName())
				+ " is enroute to your location!");
		npc.teleport(player.getLocation());
		npc.getNPCData().setLocation(player.getLocation());
	}

	@Command(
			aliases = "npc",
			usage = "moveTo [x y z]",
			desc = "move an NPC to a location",
			modifiers = "moveTo",
			min = 4,
			max = 4)
	@CommandPermissions("modify.basic")
	public static void moveNPCToLocation(CommandContext args, Player player,
			HumanNPC npc) {
		int index = args.argsLength() - 1;
		double x = 0, y = 0, z = 0;
		float yaw = npc.getLocation().getYaw(), pitch = npc.getLocation()
				.getPitch();
		String world = "";
		switch (args.argsLength() - 1) {
		case 6:
			pitch = Float.parseFloat(args.getString(index));
			--index;
		case 5:
			yaw = Float.parseFloat(args.getString(index));
			--index;
		case 4:
			world = args.getString(index);
			--index;
		case 3:
			z = Double.parseDouble(args.getString(index));
			--index;
		case 2:
			y = Double.parseDouble(args.getString(index));
			--index;
		case 1:
			x = Double.parseDouble(args.getString(index));
		}
		if (Bukkit.getServer().getWorld(world) == null) {
			player.sendMessage("Invalid world.");
			return;
		}
		npc.teleport(new Location(Bukkit.getServer().getWorld(world), x, y, z,
				yaw, pitch));
	}

	@Command(
			aliases = "npc",
			usage = "copy",
			desc = "copy an NPC",
			modifiers = "copy",
			min = 1,
			max = 1)
	@CommandPermissions("create.basic")
	public static void copyNPC(CommandContext args, Player player, HumanNPC npc) {
		int newUID = NPCManager.register(npc.getName(), player.getLocation(),
				player.getName());
		HumanNPC newNPC = NPCManager.get(newUID);
		newNPC.teleport(player.getLocation());
		newNPC.getNPCData().setLocation(player.getLocation());
		PropertyManager.copyNPCs(npc.getUID(), newUID);
	}

	@Command(
			aliases = "npc",
			usage = "/npc remove all",
			desc = "remove all NPCs",
			modifiers = "remove",
			min = 2,
			max = 2)
	@CommandPermissions("admin")
	public static void removeAllNPCs(CommandContext args, Player player,
			HumanNPC npc) {
		NPCManager.removeAll();
		NPCManager.selectedNPCs.remove(player.getName());
		player.sendMessage(ChatColor.GRAY + "The NPC(s) disappeared.");
	}

	@Command(
			aliases = "npc",
			usage = "remove",
			desc = "remove an NPC",
			modifiers = "remove",
			min = 1,
			max = 1)
	@CommandPermissions("modify.basic")
	public static void removeNPC(CommandContext args, Player player,
			HumanNPC npc) {
		NPCManager.remove(npc.getUID());
		NPCManager.selectedNPCs.remove(player.getName());
		player.sendMessage(ChatColor.GRAY + npc.getName() + " disappeared.");
	}

	@Command(
			aliases = "npc",
			usage = "rename [name]",
			desc = "rename an NPC",
			modifiers = "rename",
			min = 2,
			max = 2)
	@CommandPermissions("modify.basic")
	public static void renameNPC(CommandContext args, Player player,
			HumanNPC npc) {
		String name = args.getString(1);
		if (name.length() > 16) {
			player.sendMessage(ChatColor.RED
					+ "Max name length is 16 - NPC name length will be truncated.");
			name = name.substring(0, 16);
		}
		NPCManager.rename(npc.getUID(), name, npc.getOwner());
		player.sendMessage(ChatColor.GREEN + StringUtils.wrap(npc.getName())
				+ "'s name was set to " + StringUtils.wrap(name) + ".");
	}

	@Command(
			aliases = "npc",
			usage = "color [color-code]",
			desc = "set the name color of an NPC",
			modifiers = "color",
			min = 2,
			max = 2)
	@CommandPermissions("modify.basic")
	public static void setNPCColour(CommandContext args, Player player,
			HumanNPC npc) {
		if (!args.getString(1).substring(0, 1).equals("&")) {
			player.sendMessage(ChatColor.RED + "Use an & to specify color.");
		} else if (args.getString(1).length() != 2) {
			player.sendMessage(ChatColor.GRAY
					+ "Use the format &(code). Example - &f = white.");
		} else {
			int colour = 0xf;
			try {
				colour = Integer.parseInt(args.getString(1).substring(1, 2));
			} catch (NumberFormatException ex) {
				try {
					colour = Integer.parseInt(
							args.getString(1).substring(1, 2), 16);
				} catch (NumberFormatException e) {
					player.sendMessage(ChatColor.RED + "Invalid colour code.");
					return;
				}
			}
			npc.getNPCData().setColour(colour);
			NPCManager.setColour(npc.getUID(), npc.getOwner());
			player.sendMessage(StringUtils.wrapFull("{" + npc.getName()
					+ "}'s name color is now "
					+ args.getString(1).replace("&", "�") + "this}."));
		}
	}

	@Command(
			aliases = "npc",
			usage = "set [text]",
			desc = "set the text of an NPC",
			modifiers = "set",
			min = 2)
	@CommandPermissions("modify.basic")
	public static void setNPCText(CommandContext args, Player player,
			HumanNPC npc) {
		String text = args.getJoinedStrings(1);
		ArrayDeque<String> texts = new ArrayDeque<String>();
		texts.add(text);
		NPCManager.setText(npc.getUID(), texts);
		player.sendMessage(StringUtils.wrapFull("{" + npc.getName()
				+ "}'s text was set to {" + text + "}."));
	}

	@Command(
			aliases = "npc",
			usage = "add [text]",
			desc = "add text to an NPC",
			modifiers = "add",
			min = 2)
	@CommandPermissions("modify.basic")
	public static void addNPCText(CommandContext args, Player player,
			HumanNPC npc) {
		String text = args.getJoinedStrings(1);
		NPCManager.addText(npc.getUID(), text);
		player.sendMessage(StringUtils.wrap(text) + " was added to "
				+ StringUtils.wrap(npc.getStrippedName() + "'s") + " text.");
	}

	@Command(
			aliases = "npc",
			usage = "reset",
			desc = "reset the text of an NPC",
			modifiers = "reset",
			min = 1,
			max = 1)
	@CommandPermissions("modify.basic")
	public static void resetNPCText(CommandContext args, Player player,
			HumanNPC npc) {
		NPCManager.resetText(npc.getUID());
		player.sendMessage(StringUtils.wrap(npc.getStrippedName() + "'s")
				+ " text was reset!");
	}

	@Command(
			aliases = "npc",
			usage = "item [item]",
			desc = "set the item in an NPC's hand",
			modifiers = "item",
			min = 2,
			max = 2)
	@CommandPermissions("modify.basic")
	public static void setNPCItemInHand(CommandContext args, Player player,
			HumanNPC npc) {
		NPCDataManager.setItemInHand(player, npc, args.getString(1));
	}

	@Command(
			aliases = "npc",
			usage = "armor [armor] [item]",
			desc = "set the armor of an NPC",
			modifiers = "armor",
			min = 3,
			max = 3)
	@CommandPermissions("modify.basic")
	public static void setNPCArmor(CommandContext args, Player player,
			HumanNPC npc) {
		Material mat = StringUtils.parseMaterial(args.getString(3));
		if (mat == null) {
			player.sendMessage(ChatColor.RED + "Invalid item.");
			return;
		}
		if (!player.getInventory().contains(mat)) {
			player.sendMessage(ChatColor.RED
					+ "You need to have the item in your inventory to add it to the NPC.");
			return;
		}
		if (mat.getId() < 298 || mat.getId() > 317) {
			player.sendMessage(ChatColor.GRAY
					+ "That can't be used as an armor material.");
			return;
		}
		int slot = player.getInventory().first(mat);
		ItemStack item = NPCDataManager.decreaseItemStack(player.getInventory()
				.getItem(slot));
		player.getInventory().setItem(slot, item);
		ArrayList<Integer> items = npc.getNPCData().getItems();
		int oldhelmet = items.get(1);

		if (args.getString(2).contains("helm")) {
			items.set(1, mat.getId());
		} else if (args.getString(2).equalsIgnoreCase("torso")) {
			items.set(2, mat.getId());
		} else if (args.getString(2).contains("leg")) {
			items.set(3, mat.getId());
		} else if (args.getString(2).contains("boot")) {
			items.set(4, mat.getId());
		}
		npc.getNPCData().setItems(items);
		NPCDataManager.addItems(npc, items);

		if ((oldhelmet != 0 && items.get(1) == 0)) {
			// Despawn the old NPC, register our new one.
			NPCManager.removeForRespawn(npc.getUID());
			NPCManager.register(npc.getUID(), npc.getOwner());
		}
		player.sendMessage(StringUtils.wrap(npc.getName())
				+ "'s armor was set to " + StringUtils.wrap(mat.name()) + ".");
	}

	@CommandRequirements(requiredType = "basic")
	@Command(
			aliases = "npc",
			usage = "tp",
			desc = "teleport to an NPC",
			modifiers = "tp",
			min = 1,
			max = 1)
	@CommandPermissions("use.basic")
	public static void teleportToNPC(CommandContext args, Player player,
			HumanNPC npc) {
		player.teleport(npc.getNPCData().getLocation());
		player.sendMessage(ChatColor.GREEN + "Teleported you to "
				+ StringUtils.wrap(npc.getStrippedName()) + ". Enjoy!");
	}

	@Command(
			aliases = "npc",
			usage = "talkwhenclose [true|false]",
			desc = "set an NPC's talk-when-close setting",
			modifiers = "talkwhenclose",
			min = 2,
			max = 2)
	@CommandPermissions("modify.basic")
	public static void changeNPCTalkWhenClose(CommandContext args,
			Player player, HumanNPC npc) {
		boolean talk = false;
		if (args.getString(1).equals("true")) {
			talk = true;
		}
		npc.getNPCData().setTalkClose(talk);
		if (talk) {
			player.sendMessage(StringUtils.wrap(npc.getStrippedName())
					+ " will now talk to nearby players.");
		} else if (!talk) {
			player.sendMessage(StringUtils.wrap(npc.getStrippedName())
					+ " will stop talking to nearby players.");
		}
	}

	@Command(
			aliases = "npc",
			usage = "lookatplayers [true|false]",
			desc = "set an NPC's look-when-close setting",
			modifiers = "lookatplayers",
			min = 2,
			max = 2)
	@CommandPermissions("modify.basic")
	public static void changeNPCLookWhenClose(CommandContext args,
			Player player, HumanNPC npc) {
		boolean look = false;
		if (args.getString(1).equals("true")) {
			look = true;
		}
		npc.getNPCData().setLookClose(look);
		if (look) {
			player.sendMessage(StringUtils.wrap(npc.getStrippedName())
					+ " will now look at players.");
		} else if (!look) {
			player.sendMessage(StringUtils.wrap(npc.getStrippedName())
					+ " will stop looking at players.");
		}
	}

	@CommandRequirements(requireSelected = true)
	@Command(
			aliases = "npc",
			usage = "id",
			desc = "display an NPC's ID",
			modifiers = "id",
			min = 1,
			max = 1)
	@CommandPermissions("use.basic")
	public static void displayNPCID(CommandContext args, Player player,
			HumanNPC npc) {
		player.sendMessage(ChatColor.GREEN + "The ID of this NPC is "
				+ StringUtils.wrap("" + npc.getUID()) + ".");
	}

	@Command(
			aliases = "npc",
			usage = "select [id]",
			desc = "select an NPC by its ID",
			modifiers = "select",
			min = 2,
			max = 2)
	@CommandPermissions("use.basic")
	public static void selectNPC(CommandContext args, Player player,
			HumanNPC npc) {
		npc = NPCManager.get(Integer.valueOf(args.getString(1)));
		if (npc == null) {
			player.sendMessage(ChatColor.RED + "No NPC with the ID "
					+ args.getString(1) + ".");
		} else {
			NPCManager.selectedNPCs.put(player.getName(), npc.getUID());
			player.sendMessage(ChatColor.GREEN + "Selected NPC with ID "
					+ StringUtils.wrap("" + npc.getUID()) + ", name "
					+ StringUtils.wrap(npc.getStrippedName()) + ".");
		}
	}

	@CommandRequirements(requireSelected = true)
	@Command(
			aliases = "npc",
			usage = "owner",
			desc = "get the owner of an NPC",
			modifiers = "owner",
			min = 1,
			max = 1)
	@CommandPermissions("use.basic")
	public static void getNPCOwner(CommandContext args, Player player,
			HumanNPC npc) {
		player.sendMessage(ChatColor.GREEN + "The owner of this NPC is "
				+ StringUtils.wrap(npc.getOwner()) + ".");
	}

	@Command(
			aliases = "npc",
			usage = "setowner [name]",
			desc = "set the owner of an NPC",
			modifiers = "setowner",
			min = 2,
			max = 2)
	@CommandPermissions("modify.basic")
	public static void setNPCOwner(CommandContext args, Player player,
			HumanNPC npc) {
		player.sendMessage(ChatColor.GREEN + "The owner of "
				+ StringUtils.wrap(npc.getStrippedName()) + " is now "
				+ StringUtils.wrap(args.getString(1)) + ".");
		npc.getNPCData().setOwner(args.getString(1));
	}

	@Command(
			aliases = "npc",
			usage = "/npc [path/waypoints] (reset)",
			desc = "toggle waypoint editing",
			modifiers = { "path", "waypoints" },
			min = 1,
			max = 2)
	@CommandPermissions("modify.basic")
	public static void editWaypoints(CommandContext args, Player player,
			HumanNPC npc) {
		if (args.length() == 2) {
			Integer editing = NPCManager.pathEditors.get(player.getName());
			int UID = npc.getUID();
			if (editing == null) {
				player.sendMessage(ChatColor.AQUA
						+ "=========[ Waypoint Editing Controls ]=========");
				player.sendMessage(StringUtils.wrap("Left")
						+ " click adds a waypoint, while "
						+ StringUtils.wrap("right") + " click acts as an undo.");
				player.sendMessage(StringUtils.wrap("Repeat")
						+ " this command to finish.");
				editing = UID;
			} else if (editing == UID) {
				player.sendMessage(StringUtils.wrap("Finished")
						+ " editing waypoints.");
				editing = null;
			} else if (editing != UID) {
				player.sendMessage(ChatColor.GRAY + "Now editing "
						+ StringUtils.wrap(npc.getStrippedName())
						+ "'s waypoints.");
				editing = UID;
			}
			NPCManager.pathEditors.put(player.getName(), editing);
		} else if (args.length() >= 3 && args.getString(1).equals("reset")) {
			npc.resetWaypoints();
			player.sendMessage(ChatColor.GREEN + "Waypoints "
					+ StringUtils.wrap("reset") + ".");
		}
	}

	@CommandRequirements()
	@Command(
			aliases = "npc",
			usage = "list (name) (page)",
			desc = "view a list of NPCs for a player",
			modifiers = "list",
			min = 1,
			max = 3)
	@CommandPermissions("use.basic")
	public static void displayNPCList(CommandContext args, Player player,
			HumanNPC npc) {
		switch (args.argsLength()) {
		case 1:
			MessageUtils.displayNPCList(player, player, npc, "1");
			break;
		case 2:
			if (StringUtils.isNumber(args.getString(1))) {
				MessageUtils.displayNPCList(player, player, npc,
						args.getString(1));
			} else {
				if (ServerUtils.matchPlayer(args.getString(1)) != null) {
					MessageUtils.displayNPCList(player,
							ServerUtils.matchPlayer(args.getString(1)), npc,
							"1");
				} else {
					player.sendMessage(ChatColor.RED
							+ "Could not match player.");
				}
			}
			break;
		case 3:
			if (ServerUtils.matchPlayer(args.getString(1)) != null) {
				MessageUtils.displayNPCList(player,
						ServerUtils.matchPlayer(args.getString(1)), npc,
						args.getString(2));
			} else {
				player.sendMessage(ChatColor.RED + "Could not match player.");
			}
			break;
		}
	}
}