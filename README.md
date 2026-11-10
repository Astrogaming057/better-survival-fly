# Forced Creative Flight

Minecraft **1.21.1** mod for Fabric, NeoForge, and Forge. It gives authorized players vanilla Creative flight while they stay in Survival. Health, hunger, inventory, armor, damage, death, experience, and block and entity interaction are unchanged. The mod never switches the player's game mode.

The server decides who may fly. The client sends toggle and speed requests, then applies the state the server confirms.

This is for a game the server operator runs. It keeps that operator's flight grant in place when another installed mod clears `allowFlying` or `flying`. It does not bypass an external server's anti-cheat, and it does not change Creative or Spectator players.

## What flight feels like

While the server has authorized a Survival or Adventure player:

- Double-tap jump starts and stops airborne flight, the same way Creative does.
- Hold jump to ascend, hold sneak to descend, and use WASD relative to the camera.
- The player can hover. Vanilla flight acceleration, sprint boost, and collision stay in charge.
- Hold Alt and scroll to change flight speed. Scroll up is faster, scroll down is slower. A plain scroll still moves the hotbar.
- Speed is remembered for that player.

`I` and `/flight` are the permission toggle, not the Creative double-tap. Turning flight on authorizes the player and lifts them off. Turning it off removes the grant and drops them. Double-tap still works after that for anyone who remains authorized. Revoking independent control does not remove an authorization an operator already gave.

Fall damage still applies if a player stops flying in the air.

## Permission model

Three separate values are stored per UUID:

| Value | Meaning |
| --- | --- |
| Authorization | The player may use Creative-style flight (`allowFlying`). |
| Flying | They are currently airborne. |
| Independent control (`allowcontrol`) | They may turn their own authorization on or off. |

Operators (permission level 2 by default) can change every value for any online player and can always toggle themselves. A player with `allowcontrol` can toggle only themselves, with `/flight`, `/flight self`, or `I`. Anyone else cannot turn flight on, and a client packet cannot grant it.

## Commands

All of these are checked on the server.

| Command | Who | Effect |
| --- | --- | --- |
| `/flight` | Operator, or a player with `allowcontrol` | Toggle that player's own flight. |
| `/flight self` | Same as `/flight` | Same as `/flight`. |
| `/flight <player>` | Operator | Toggle that player's authorization. Enabling also starts flight. |
| `/flight <player> on` | Operator | Authorize the player and start flight. |
| `/flight <player> off` | Operator | Remove authorization and stop flight. `allowcontrol` stays as it was. |
| `/flight <player> allowcontrol` | Operator | Let the player toggle their own flight. Saved across restarts. |
| `/flight <player> revokecontrol` | Operator | Remove that permission. An existing flight grant stays. |
| `/flight <player> status` | Operator | Show authorization, airborne state, control, and speed. |
| `/flight maxspeed` | Operator | Show the server-wide maximum, in the same `x` units as the speed display. |
| `/flight maxspeed <number>` | Operator | Set that maximum, save it, and apply it to everyone online. `7.5` means `7.50x`. |
| `/flight reload` | Operator | Reread `config.json` and `permissions.json`, then reapply them to online players. |

Targets must be online. Permissions themselves are stored by UUID, so a later username change still matches.

## Build

Requirements: JDK 21, and an internet connection the first time Gradle downloads Minecraft.

```powershell
cd forced-flight
$env:JAVA_HOME = "C:\path\to\jdk-21"
.\gradlew.bat :common:test :fabric:remapJar :neoforge:remapJar :forge:remapJar
```

`test` covers the permission rules, speed clamps, enforcement decisions, and the JSON store. The installable jars are the ones without `sources` or `dev-shadow` in the name:

| Loader | Jar |
| --- | --- |
| Fabric | `fabric/build/libs/forcedflight-fabric-1.0.0.jar` |
| NeoForge | `neoforge/build/libs/forcedflight-neoforge-1.0.0.jar` |
| Forge | `forge/build/libs/forcedflight-forge-1.0.0.jar` |

## Install

Use the jar that matches the loader. The client and the server must use the same one. A Fabric jar does not load on NeoForge or Forge, and the three jars cannot be mixed.

1. NeoForge 1.21.1: put `forcedflight-neoforge-1.0.0.jar` in the server `mods` folder and in the NeoForge client's `mods` folder.
2. Forge 1.21.1: put `forcedflight-forge-1.0.0.jar` in both Forge `mods` folders.
3. Fabric 1.21.1: install Fabric Loader and Fabric API, then put `forcedflight-fabric-1.0.0.jar` in both Fabric `mods` folders.

The client alone cannot grant flight. Restart the server after copying the jar. On join, chat explains the controls: `I` or `/flight` toggles permission, double-tap jump takes off, and Alt plus scroll changes speed. `/flight` is absent until that server has the matching jar.

On first launch the server writes:

- `config/forcedflight/config.json`
- `config/forcedflight/permissions.json`

A documented copy of the config defaults is `config.example.json` in this project. Edit the live file, then run `/flight reload` or restart.

The HUD and the default key live in the client config. Speed limits, the operator level, enforcement, and bootstrap UUIDs are read on the server. In multiplayer the client follows the speed range the server sends.

## Configuration

`enabled` turns the whole system off. Commands refuse to grant flight while it is false.

`operatorLevel` is the vanilla permission level treated as an operator. `2` matches a normal OP. The value is clamped to 1–4.

`defaultToggleKey` is the key registered the first time Minecraft creates the control. After that, change it in Options → Controls → Forced Creative Flight. The binding is `Toggle Flight`.

`flight.defaultSpeed` is vanilla Creative speed when it is `0.05`. That displays as `1.00x`. `minSpeed`, `maxSpeed`, and `speedStep` bound the scroll wheel. The default maximum is `0.375`, which displays as `7.50x`. Clients only scroll up to the maximum the server last sent. Operators change it with `/flight maxspeed`. Speeds are clamped again to the range 0.005 through 1.0 so a bad value cannot run away. A saved config that still has the old default maximum of `0.25` is raised to `0.375` the next time it loads.

`hud.enabled`, `hud.anchor` (`top_left`, `top_right`, `bottom_left`, `bottom_right`, `bottom_center`), `hud.offsetX`, and `hud.offsetY` place the indicator. `hud.durationTicks` is how long it stays after a toggle or a scroll. It stays solid, then fades out over the last second. It does not remain on screen just because the player is flying.

`enforcement.enabled` restores an authorized player's flight flags when something else clears them. `enforcement.mode` is `resync` or `silent`. `resync` also sends a correction to that client, at most once per `resyncCooldownTicks` (minimum 5). `silent` fixes the server fields and any ability packet built from those fields, without an extra resync packet. Creative and Spectator are skipped so vanilla game modes keep their own flight.

`bootstrap.authorized` and `bootstrap.allowControl` are UUID lists. They create a record only when that UUID has never been saved. A later `/flight off` or `revokecontrol` is not undone on restart. Delete the UUID from `permissions.json` if the bootstrap list should apply again.

## Permissions file

`permissions.json` is the source of truth:

```json
{
  "version": 1,
  "players": {
    "11111111-1111-1111-1111-111111111111": {
      "authorized": true,
      "flying": false,
      "allowControl": true,
      "flySpeed": 0.08
    }
  }
}
```

Writes go to a temp file and then replace the real file. A file that cannot be parsed is moved aside as `permissions.json.corrupt` and the server continues with an empty store plus any bootstrap UUIDs. Command changes are saved immediately. Flying and speed changes are saved within about a second, and always on disconnect and on server stop.

## How enforcement is hooked

`allowFlying` and `flying` are public fields, so another mod can assign them without calling a setter. The mod does not poll those fields as its only defense. It corrects them at the points Minecraft and other mods actually publish the change:

- `Player.travel`, immediately before movement, and only for online players this mod has authorized
- `ServerPlayer.onUpdateAbilities`, before the outgoing packet is built
- the `ClientboundPlayerAbilitiesPacket` constructor that copies a live `Abilities` object
- `ServerGamePacketListenerImpl.handlePlayerAbilities`, where a client double-tap is accepted or ignored
- game-mode changes, respawn copy, respawn, and dimension change

Unrelated flags (`invulnerable`, `creativeMode`, `allowModifyWorld`, walk speed) are not written. A client ability packet is treated as that player starting or stopping flight, because that is the same packet vanilla uses for double-tap. A server-side wipe that does not come through that packet is restored. If some other mod clears the flags every tick, the fields are corrected for movement every tick, and a network correction is sent only on the cooldown. The first correction for a player is written to the server log.

## Testing

Automated checks:

```powershell
.\gradlew.bat test
```

Those tests cover operator toggle on and off, granting and revoking `allowcontrol` without dropping an existing grant, players who cannot toggle, speed clamps, a wiped ability set being restored without touching the other flags, Creative being left alone, and the permission file surviving a reload for more than one UUID.

On a 1.21.1 client and server that both have the jar for that loader:

1. OP runs `/flight` and lifts off, then `/flight` again and drops. The game mode stays Survival, and health and hunger still work.
2. OP runs `/flight Steve` or `/flight Steve on`, then `/flight Steve off`.
3. OP runs `/flight Steve allowcontrol` and `/flight Steve revokecontrol`. After revoke, a grant that is still on stays on. `/flight Steve status` shows the three values.
4. Steve, with control, uses `/flight self` and `I`. Without control, `I` and `/flight` do nothing useful and Steve cannot target another player.
5. A Survival player who was never authorized cannot start flight by pressing jump or by editing client abilities.
6. While flight is authorized, hold Alt and scroll up and down. The HUD multiplier changes, movement matches it, and a scroll without Alt still moves the hotbar. The speed is still there after landing and taking off again.
7. With a second mod or a short test mixin that sets `allowFlying` and `flying` to false, an authorized player is flying again on the next movement tick. The server log notes the restore once.
8. Die and respawn, change dimension, disconnect, and restart the server. Authorization, control, and speed come back. Airborne state comes back when that was what was stored.
9. Two authorized players can fly at the same time.
10. Put an authorized player in Creative and back in Survival. Creative flight stays vanilla, and the Survival grant is still there afterward.

## Limitation

A mod on the client can send the same flying packet the player sends when they double-tap. The server accepts that as the player choosing to land or take off, as long as they are authorized. It does not accept it as a new grant. Stopping a remote anti-cheat is intentionally out of scope.
