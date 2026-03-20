# CustomDeathMessages

Replace standard Minecraft death messages with fully customizable ones, with MiniMessage formatting and optional EssentialsX nickname support.

## Features

- **Per-cause messages** — configure different messages for every damage cause (fall, fire, lava, PvP, projectile, etc.)
- **Random selection** — provide a list of messages for any cause and one is picked at random each time
- **MiniMessage formatting** — full colour, gradient, rainbow, and style support
- **Placeholders** — `{player}`, `{killer}`, and `{world}` available in every message
- **EssentialsX nicknames** — automatically uses Essentials nicknames in messages when available
- **World-scoped broadcast** — optionally limit death messages to the world the player died in
- **Suppress messages** — set any cause to an empty list `[]` to hide those death messages entirely
- **Live reload** — `/cdm reload` reloads the config without restarting

## Requirements

| Requirement | Version |
|---|---|
| Paper (or fork) | 1.21.1+ |
| Java | 21+ |
| EssentialsX *(optional)* | 2.20.1+ |

## Installation

1. Download `custom-death-messages-1.0.0.jar` from the [Releases](../../releases) page.
2. Drop it into your server's `plugins/` folder.
3. Restart or reload the server.
4. Edit `plugins/CustomDeathMessages/config.yml` to your liking.
5. Run `/cdm reload` to apply changes without restarting.

## Configuration

```yaml
settings:
  # Use Essentials nicknames in messages (requires EssentialsX)
  use-essentials-nicknames: true
  # true  = broadcast to the whole server
  # false = broadcast only to the world the player died in
  broadcast-globally: true

messages:
  # Fallback used when no specific cause entry exists
  default:
    - "<gray>{player} died."
    - "<gray>{player} met their end."

  # A single string is also valid (no list needed)
  FALL:
    - "<yellow>{player} <gray>fell to their death."
    - "<yellow>{player} <gray>forgot that the ground is hard."
    - "<yellow>{player} <gray>experienced a rapid unplanned descent."

  # Suppress this cause entirely
  SUICIDE: []
```

### Placeholders

| Placeholder | Description |
|---|---|
| `{player}` | The player who died (uses Essentials nickname if enabled) |
| `{killer}` | The entity or player that killed them (mob name, player name, or `Unknown`) |
| `{world}` | The name of the world the death occurred in |

### Supported Damage Causes

All Bukkit [`DamageCause`](https://jd.papermc.io/paper/1.21.1/org/bukkit/event/entity/EntityDamageEvent.DamageCause.html) values are supported as config keys. Common ones:

| Key | When it triggers |
|---|---|
| `ENTITY_ATTACK` | Melee hit by a mob or player |
| `ENTITY_SWEEP_ATTACK` | Sword sweep attack |
| `PROJECTILE` | Arrow, trident, or other projectile |
| `MAGIC` | Splash/lingering potion or instant damage effect |
| `THORNS` | Thorns enchantment retaliation |
| `FALL` | Fall damage |
| `FLY_INTO_WALL` | Elytra collision |
| `DROWNING` | Running out of air underwater |
| `FIRE` / `FIRE_TICK` | Direct fire / burning over time |
| `LAVA` | Lava contact |
| `HOT_FLOOR` | Magma block contact |
| `VOID` | Falling out of the world |
| `LIGHTNING` | Lightning strike |
| `FREEZE` | Powder snow freezing |
| `BLOCK_EXPLOSION` | TNT or bed/anchor explosion |
| `ENTITY_EXPLOSION` | Creeper or respawn anchor explosion |
| `POISON` | Poison effect |
| `WITHER` | Wither effect |
| `STARVATION` | Hunger |
| `SUFFOCATION` | Stuck inside a block |
| `CRAMMING` | Too many entities in one space |
| `FALLING_BLOCK` | Falling anvil or sand |
| `DRAGON_BREATH` | Ender Dragon breath |
| `SONIC_BOOM` | Warden sonic boom |
| `KILL` | `/kill` command |
| `CUSTOM` | Plugin-defined damage |
| `default` | Any cause without a specific entry |

### MiniMessage Formatting

Messages support the full [MiniMessage](https://docs.advntr.dev/minimessage/format.html) tag set:

```yaml
ENTITY_ATTACK:
  - "<red>{player} <gray>was slain by <red>{killer}<gray>."
  - "<gradient:red:gold>{player}</gradient> <gray>was defeated by <bold>{killer}</bold>."
  - "<rainbow>{player}</rainbow> <gray>lost to {killer}."
```

## Commands & Permissions

| Command | Alias | Description | Permission |
|---|---|---|---|
| `/customdeathmessages reload` | `/cdm reload` | Reload the config | `customdeathmessages.admin` |

`customdeathmessages.admin` defaults to **OP**.

## Building from Source

```bash
git clone https://github.com/choudesu/custom-death-messages.git
cd custom-death-messages
mvn package
# Output: target/custom-death-messages-1.0.0.jar
```

Requires Java 21 and Maven 3.8+.
