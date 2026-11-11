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
						.then(Commands.literal("falldamage").requires(FlightCommand::isAdmin)
								.executes(context -> setFallDamage(context, !ModConfig.get().fallDamage))
								.then(Commands.literal("on").executes(context -> setFallDamage(context, true)))
								.then(Commands.literal("off").executes(context -> setFallDamage(context, false))))
						.then(Commands.literal("flypenalty").requires(FlightCommand::isAdmin)
								.executes(context -> setFlyPenalty(context, !ModConfig.get().flyPenalty))
								.then(Commands.literal("on").executes(context -> setFlyPenalty(context, true)))
								.then(Commands.literal("off").executes(context -> setFlyPenalty(context, false))))
						.then(Commands.literal("joinmessage").requires(FlightCommand::isAdmin)
								.executes(context -> setJoinMessage(context, !ModConfig.get().joinMessage))
								.then(Commands.literal("on").executes(context -> setJoinMessage(context, true)))
								.then(Commands.literal("off").executes(context -> setJoinMessage(context, false))))
						.then(Commands.literal("keepflying")
								.executes(context -> setOwnKeepFlying(context, null))
								.then(Commands.literal("on").executes(context -> setOwnKeepFlying(context, true)))
								.then(Commands.literal("off").executes(context -> setOwnKeepFlying(context, false))))
						.then(Commands.literal("speed")
								.executes(FlightCommand::showOwnSpeed)
								.then(Commands.argument("multiplier", FloatArgumentType.floatArg(0.1f, 20.0f))
										.executes(FlightCommand::setOwnSpeed)))
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
								.then(Commands.literal("keepflying")
										.executes(context -> setOtherKeepFlying(context, null))
										.then(Commands.literal("on").executes(context -> setOtherKeepFlying(context, true)))
										.then(Commands.literal("off").executes(context -> setOtherKeepFlying(context, false))))
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
		if (FlightManager.usesVanillaFlight(player)) {
			context.getSource().sendFailure(Component.literal(
					"Forced Creative Flight is for survival only. Creative and spectator keep vanilla flight."));
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
		PlayerFlightRecord existing = FlightPermissionStore.get().get(target.getUUID()).orElse(null);
		if (FlightManager.usesVanillaFlight(target) && (existing == null || !existing.authorized())) {
			context.getSource().sendFailure(Component.literal(
					target.getName().getString() + " is in creative or spectator. This mod only enables flight in survival."));
			return 0;
		}
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
		if (FlightManager.usesVanillaFlight(target)) {
			context.getSource().sendFailure(Component.literal(
					target.getName().getString() + " is in creative or spectator. This mod only enables flight in survival."));
			return 0;
		}
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
		boolean keepFlying = record != null && record.keepFlying();
		float speed = record == null ? ModConfig.get().defaultSpeed : record.flySpeed();
		String message = String.format(
				Locale.ROOT,
				"%s: authorization=%s, flying=%s, control=%s, keepflying=%s, speed=%.2fx (%.3f)",
				target.getName().getString(),
				authorized ? "on" : "off",
				flying ? "yes" : "no",
				control ? "allowcontrol" : "none",
				keepFlying ? "on" : "off",
				FlightSpeed.multiplier(speed),
				speed
		);
		feedback(context, message);
		return Command.SINGLE_SUCCESS;
	}

	private static int setFallDamage(CommandContext<CommandSourceStack> context, boolean enabled) {
		boolean saved = ModConfig.saveFallDamage(enabled);
		feedback(context, saved
				? "Fall damage enabled. Granted players still get hurt if they fall while flight is toggled off."
				: "Fall damage disabled while flight is granted and toggled off. Falling in that state no longer hurts.");
		return Command.SINGLE_SUCCESS;
	}

	private static int setFlyPenalty(CommandContext<CommandSourceStack> context, boolean enabled) {
		boolean saved = ModConfig.saveFlyPenalty(enabled);
		FlightManager.broadcastState();
		feedback(context, saved
				? "Fly mining penalty enabled. Breaking blocks while flying is slower."
				: "Fly mining penalty disabled. Breaking blocks while flying uses normal speed. Jumping is unchanged.");
		return Command.SINGLE_SUCCESS;
	}

	private static int setJoinMessage(CommandContext<CommandSourceStack> context, boolean enabled) {
		boolean saved = ModConfig.saveJoinMessage(enabled);
		feedback(context, saved
				? "Join message enabled. Players will see the flight controls when they log in."
				: "Join message disabled. Players will not see the flight controls when they log in.");
		return Command.SINGLE_SUCCESS;
	}

	private static int setOwnKeepFlying(CommandContext<CommandSourceStack> context, Boolean enabled) {
		if (!ensureEnabled(context)) {
			return 0;
		}
		ServerPlayer player = context.getSource().getPlayer();
		if (player == null) {
			context.getSource().sendFailure(Component.literal("Only a player can change their own keep-flying setting. Use /flight <player> keepflying."));
			return 0;
		}
		PlayerFlightRecord record = FlightManager.recordForEdit(player);
		boolean next = enabled == null ? !record.keepFlying() : enabled;
		boolean saved = FlightManager.setKeepFlying(player, next);
		feedback(context, saved
				? "Keep flying enabled for you. Landing no longer cancels your flight. Double-tap jump to stop flying."
				: "Keep flying disabled for you. Landing cancels your flight like normal.");
		return Command.SINGLE_SUCCESS;
	}

	private static int setOtherKeepFlying(CommandContext<CommandSourceStack> context, Boolean enabled) throws CommandSyntaxException {
		if (!ensureEnabled(context)) {
			return 0;
		}
		ServerPlayer target = EntityArgument.getPlayer(context, "player");
		PlayerFlightRecord record = FlightManager.recordForEdit(target);
		boolean next = enabled == null ? !record.keepFlying() : enabled;
		boolean saved = FlightManager.setKeepFlying(target, next);
		feedback(context, saved
				? "Keep flying enabled for " + target.getName().getString() + "."
				: "Keep flying disabled for " + target.getName().getString() + ".");
		return Command.SINGLE_SUCCESS;
	}

	private static int showOwnSpeed(CommandContext<CommandSourceStack> context) {
		ServerPlayer player = context.getSource().getPlayer();
		if (player == null) {
			context.getSource().sendFailure(Component.literal("Only a player can check their own flight speed."));
			return 0;
		}
		if (FlightManager.usesVanillaFlight(player)) {
			context.getSource().sendFailure(Component.literal(
					"Creative and spectator use vanilla flight speed. This mod only changes speed in survival."));
			return 0;
		}
		PlayerFlightRecord record = FlightPermissionStore.get().get(player.getUUID()).orElse(null);
		float speed = record == null ? ModConfig.get().defaultSpeed : record.flySpeed();
		feedback(context, String.format(Locale.ROOT,
				"Your flight speed is %.2fx. The server cap is %.2fx. Set it with /flight speed <number>.",
				FlightSpeed.multiplier(speed),
				FlightSpeed.multiplier(ModConfig.get().maxSpeed)));
		return Command.SINGLE_SUCCESS;
	}

	private static int setOwnSpeed(CommandContext<CommandSourceStack> context) {
		if (!ensureEnabled(context)) {
			return 0;
		}
		ServerPlayer player = context.getSource().getPlayer();
		if (player == null) {
			context.getSource().sendFailure(Component.literal("Only a player can set their own flight speed."));
			return 0;
		}
		if (FlightManager.usesVanillaFlight(player)) {
			context.getSource().sendFailure(Component.literal(
					"Creative and spectator use vanilla flight speed. This mod only changes speed in survival."));
			return 0;
		}
		PlayerFlightRecord record = FlightPermissionStore.get().get(player.getUUID()).orElse(null);
		if (record == null || !record.authorized()) {
			context.getSource().sendFailure(Component.literal("Turn flight on before setting a speed."));
			return 0;
		}
		float multiplier = FloatArgumentType.getFloat(context, "multiplier");
		float saved = FlightManager.setOwnSpeed(player, multiplier);
		if (!Float.isFinite(saved)) {
			context.getSource().sendFailure(Component.literal("Could not change flight speed."));
			return 0;
		}
		feedback(context, String.format(Locale.ROOT, "Flight speed set to %.2fx.", FlightSpeed.multiplier(saved)));
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
