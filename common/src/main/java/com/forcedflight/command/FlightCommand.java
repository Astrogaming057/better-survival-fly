package com.forcedflight.command;

import java.util.Locale;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.forcedflight.ForcedFlight;
import com.forcedflight.ForcedFlightBootstrap;
import com.forcedflight.config.ModConfig;
import com.forcedflight.flight.FlightManager;
import com.forcedflight.permission.FlightPermissionStore;
import com.forcedflight.permission.FlightPolicy;
import com.forcedflight.permission.FlightSpeed;
import com.forcedflight.permission.PlayerFlightRecord;

public final class FlightCommand {
	private FlightCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal("flight")
						.requires(source -> true)
						.executes(FlightCommand::toggleSelf)
						.then(Commands.literal("self").executes(FlightCommand::toggleSelf))
						.then(Commands.literal("reload").requires(FlightCommand::isAdmin).executes(FlightCommand::reload))
						.then(Commands.literal("joinmessage").requires(FlightCommand::isAdmin)
								.executes(context -> setJoinMessage(context, !ModConfig.get().joinMessage))
								.then(Commands.literal("on").executes(context -> setJoinMessage(context, true)))
								.then(Commands.literal("off").executes(context -> setJoinMessage(context, false))))
						.then(Commands.literal("maxspeed").requires(FlightCommand::isAdmin)
								.executes(FlightCommand::showMaxSpeed)
								.then(Commands.argument("multiplier", FloatArgumentType.floatArg(0.1f, 20.0f))
										.executes(FlightCommand::setMaxSpeed)))
						.then(Commands.argument("player", EntityArgument.player())
								.requires(FlightCommand::isAdmin)
								.executes(FlightCommand::toggleOther)
								.then(Commands.literal("on").executes(FlightCommand::enableOther))
								.then(Commands.literal("off").executes(FlightCommand::disableOther))
								.then(Commands.literal("allowcontrol").executes(FlightCommand::allowControl))
								.then(Commands.literal("revokecontrol").executes(FlightCommand::revokeControl))
								.then(Commands.literal("status").executes(FlightCommand::status)))
		);
	}

	private static boolean isAdmin(CommandSourceStack source) {
		return source.hasPermission(ModConfig.get().operatorLevel);
	}

	private static int toggleSelf(CommandContext<CommandSourceStack> context) {
		if (!ensureEnabled(context)) {
			return 0;
		}
		ServerPlayer player = context.getSource().getPlayer();
		if (player == null) {
			context.getSource().sendFailure(Component.literal("Only a player can toggle their own flight. Use /flight <player>."));
			return 0;
		}
		PlayerFlightRecord existing = FlightPermissionStore.get().get(player.getUUID()).orElse(null);
		if (!FlightPolicy.canToggleSelf(FlightManager.isOperator(player), existing != null && existing.allowControl())) {
			context.getSource().sendFailure(Component.literal("You do not have permission to toggle flight."));
			return 0;
		}
		PlayerFlightRecord record = FlightManager.recordForEdit(player);
		boolean enabled = FlightPolicy.toggleAuthorization(record);
		FlightManager.pushFlight(player, true);
		feedback(context, enabled ? "Flight enabled." : "Flight disabled.");
		return Command.SINGLE_SUCCESS;
	}

	private static int toggleOther(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		if (!ensureEnabled(context)) {
			return 0;
		}
		ServerPlayer target = EntityArgument.getPlayer(context, "player");
		PlayerFlightRecord record = FlightManager.recordForEdit(target);
		boolean enabled = FlightPolicy.toggleAuthorization(record);
		FlightManager.pushFlight(target, true);
		feedback(context, (enabled ? "Flight enabled for " : "Flight disabled for ") + target.getName().getString() + ".");
		return Command.SINGLE_SUCCESS;
	}

	private static int enableOther(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		if (!ensureEnabled(context)) {
			return 0;
		}
		ServerPlayer target = EntityArgument.getPlayer(context, "player");
		PlayerFlightRecord record = FlightManager.recordForEdit(target);
		FlightPolicy.enableFlight(record);
		FlightManager.pushFlight(target, true);
		feedback(context, "Flight enabled for " + target.getName().getString() + ".");
		return Command.SINGLE_SUCCESS;
	}

	private static int disableOther(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		if (!ensureEnabled(context)) {
			return 0;
		}
		ServerPlayer target = EntityArgument.getPlayer(context, "player");
		PlayerFlightRecord record = FlightManager.recordForEdit(target);
		FlightPolicy.disableFlight(record);
		FlightManager.pushFlight(target, true);
		feedback(context, "Flight disabled for " + target.getName().getString() + ". Independent control was left unchanged.");
		return Command.SINGLE_SUCCESS;
	}

	private static int allowControl(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		if (!ensureEnabled(context)) {
			return 0;
		}
		ServerPlayer target = EntityArgument.getPlayer(context, "player");
		PlayerFlightRecord record = FlightManager.recordForEdit(target);
		FlightPolicy.grantControl(record);
		FlightManager.syncControl(target);
		feedback(context, target.getName().getString() + " can now toggle flight.");
		return Command.SINGLE_SUCCESS;
	}

	private static int revokeControl(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		if (!ensureEnabled(context)) {
			return 0;
		}
		ServerPlayer target = EntityArgument.getPlayer(context, "player");
		PlayerFlightRecord record = FlightManager.recordForEdit(target);
		FlightPolicy.revokeControl(record);
		FlightManager.syncControl(target);
		feedback(context, "Revoked independent flight control from " + target.getName().getString() + ". Existing flight authorization was left unchanged.");
		return Command.SINGLE_SUCCESS;
	}

	private static int status(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		ServerPlayer target = EntityArgument.getPlayer(context, "player");
		PlayerFlightRecord record = FlightPermissionStore.get().get(target.getUUID()).orElse(null);
		boolean authorized = record != null && record.authorized();
		boolean flying = record != null && record.flying();
		boolean control = record != null && record.allowControl();
		float speed = record == null ? ModConfig.get().defaultSpeed : record.flySpeed();
		String message = String.format(
				Locale.ROOT,
				"%s: authorization=%s, flying=%s, control=%s, speed=%.2fx (%.3f)",
				target.getName().getString(),
				authorized ? "on" : "off",
				flying ? "yes" : "no",
				control ? "allowcontrol" : "none",
				FlightSpeed.multiplier(speed),
				speed
		);
		feedback(context, message);
		return Command.SINGLE_SUCCESS;
	}

	private static int setJoinMessage(CommandContext<CommandSourceStack> context, boolean enabled) {
		boolean saved = ModConfig.saveJoinMessage(enabled);
		feedback(context, saved
				? "Join message enabled. Players will see the flight controls when they log in."
				: "Join message disabled. Players will not see the flight controls when they log in.");
		return Command.SINGLE_SUCCESS;
	}

	private static int showMaxSpeed(CommandContext<CommandSourceStack> context) {
		feedback(context, maxSpeedMessage("Max flight speed is %.2fx."));
		return Command.SINGLE_SUCCESS;
	}

	private static int setMaxSpeed(CommandContext<CommandSourceStack> context) {
		float multiplier = FloatArgumentType.getFloat(context, "multiplier");
		float saved = ModConfig.saveMaxSpeed(multiplier * ForcedFlight.VANILLA_FLY_SPEED);
		FlightManager.applySpeedLimit();
		ForcedFlight.LOGGER.info("Set max flight speed to {}x", FlightSpeed.multiplier(saved));
		feedback(context, String.format(Locale.ROOT, "Max flight speed set to %.2fx.", FlightSpeed.multiplier(saved)));
		return Command.SINGLE_SUCCESS;
	}

	private static String maxSpeedMessage(String pattern) {
		return String.format(Locale.ROOT, pattern, FlightSpeed.multiplier(ModConfig.get().maxSpeed));
	}

	private static int reload(CommandContext<CommandSourceStack> context) {
		ForcedFlightBootstrap.reload();
		for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
			FlightManager.onJoin(player);
		}
		feedback(context, "Reloaded forced-flight config and permissions.");
		ForcedFlight.LOGGER.info("Reloaded flight config and permissions");
		return Command.SINGLE_SUCCESS;
	}

	private static boolean ensureEnabled(CommandContext<CommandSourceStack> context) {
		if (ModConfig.get().enabled) {
			return true;
		}
		context.getSource().sendFailure(Component.literal("Forced flight is disabled in the config."));
		return false;
	}

	private static void feedback(CommandContext<CommandSourceStack> context, String message) {
		context.getSource().sendSuccess(() -> Component.literal(message), true);
	}
}
