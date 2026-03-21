# CustomDeathMessages

Replace standard Minecraft death messages with fully customizable ones, with MiniMessage formatting, a League of Legends-style death combo system, and optional EssentialsX nickname support.

## Features

- **Per-cause messages** — configure different messages for every damage cause (fall, fire, lava, PvP, projectile, etc.)
- **Random selection** — provide a list of messages for any cause and one is picked at random each time
- **MiniMessage formatting** — full colour, gradient, rainbow, and style support in all messages
- **Smart mob articles** — mob killers are automatically prefixed with "a" or "an" (e.g. "a Zombie", "an Enderman")
- **Placeholders** — `<player>`, `<killer>`, and `<world>` available in every message
- **EssentialsX nicknames** — automatically uses Essentials display names when available
- **World-scoped broadcast** — optionally limit death messages to the world the player died in
- **Suppress messages** — set any cause to an empty list `[]` to hide those death messages entirely
- **Death combo system** — LoL-style kill streak tracker with configurable milestones, gradient labels, and a combo-breaker broadcast
- **Live reload** — `/cdm reload` reloads the config without restarting

## Requirements

| Requirement | Version |
|---|---|
| Paper (or fork) | 1.21.1+ |
| Java | 21+ |
| EssentialsX *(optional)* | 2.20.1+ |

## Installation

1. Download `custom-death-messages-<version>.jar` from the [Releases](../../releases) page.
2. Drop it into your server's `plugins/` folder.
3. Restart or reload the server.
4. Edit `plugins/CustomDeathMessages/config.yml` to your liking.
5. Run `/cdm reload` to apply changes without restarting.

## Configuration

### Settings

```yaml
settings:
  # Use Essentials nicknames in messages (requires EssentialsX)
  use-essentials-nicknames: true
  # true  = broadcast to the whole server
  # false = broadcast only to the world the player died in
  broadcast-globally: true
```

### Death Messages

```yaml
messages:
  # Fallback used when no specific cause entry exists
  default:
    - "<gray><player> died."
    - "<gray><player> met their end."

  # A list picks one message at random each time
  ENTITY_ATTACK:
    - "<red><player> <gray>was slain by <red><killer><gray>."
    - "<red><player> <gray>got destroyed by <red><killer><gray>."

  # Gradients and rainbow are fully supported
  FALL:
    - "<gradient:yellow:gold><player></gradient> <gray>fell to their death."

  # Suppress a cause entirely
  SUICIDE: []
```

#### Placeholders

| Placeholder | Description |
|---|---|
| `<player>` | The player who died (uses Essentials nickname if enabled) |
| `<killer>` | The entity or player that killed them. Players use their display name. Mobs use their type name prefixed with "a" or "an" (e.g. `a Zombie`, `an Enderman`). Custom-named mobs use their custom name with no article. |
| `<world>` | The name of the world the death occurred in |

#### Supported Damage Causes

All Bukkit [`DamageCause`](https://jd.papermc.io/paper/1.21.1/org/bukkit/event/entity/EntityDamageEvent.DamageCause.html) values are valid config keys. Common ones:

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

### Death Combo System

When a player is killed by the same killer multiple times within the combo window, a milestone is announced server-wide in place of the regular death message. "Same killer" means the same player (matched by UUID) or the same mob type (e.g. any Zombie counts). Dying to a different killer silently resets the streak.

When the combo window expires without another death, a combo-breaker message is broadcast server-wide and the victim receives a private congratulation.

```yaml
combo:
  enabled: true

  # Seconds after the last death before the combo window closes.
  # Resets with each new death to the same killer.
  window-seconds: 120

  # Milestone labels at each combo count threshold.
  # The highest defined label is reused for counts beyond it (9x, 10x, … = Godlike).
  # Labels support full MiniMessage formatting.
  milestones:
    2: "<gradient:yellow:gold><bold>Double Kill</bold></gradient>"
    3: "<gradient:gold:red><bold>Triple Kill</bold></gradient>"
    4: "<gradient:red:dark_red><bold>Quadra Kill</bold></gradient>"
    5: "<gradient:dark_red:dark_purple><bold>Penta Kill</bold></gradient>"
    6: "<gradient:dark_purple:light_purple><bold>Legendary</bold></gradient>"
    7: "<gradient:light_purple:white><bold>Unstoppable</bold></gradient>"
    8: "<rainbow><bold>Godlike</bold></rainbow>"

  # Replaces the normal death message when a milestone is reached (server-wide).
  milestone-broadcast: "<death_message> <dark_gray>—</dark_gray> <milestone> <gray>(<combo>x)</gray>"

  # Sent server-wide when the victim survives the combo window.
  breaker-broadcast: "<gray><aqua><player></aqua> survived a <bold><combo>x</bold> death streak!</gray>"

  # Sent privately to the victim when their streak ends.
  breaker-private: "<green>You survived a <bold><combo>x</bold> death streak! Well done.</green>"
```

#### Combo Placeholders

| Placeholder | Available in | Description |
|---|---|---|
| `<death_message>` | `milestone-broadcast` | The fully-rendered custom death message for this cause |
| `<milestone>` | `milestone-broadcast` | The milestone label (e.g. "Double Kill") |
| `<combo>` | `milestone-broadcast`, `breaker-broadcast`, `breaker-private` | The current/final streak count |
| `<player>` | `breaker-broadcast`, `breaker-private` | The player who survived the streak |

#### Default Combo Behaviour

| Scenario | Result |
|---|---|
| Same killer again within window | Death message replaced by milestone broadcast |
| Different killer within window | Streak resets silently, new streak begins |
| Survive window with streak ≥ 2 | Server-wide breaker broadcast + private congrats |
| Survive window with no streak | Silent — nothing sent |
| `combo.enabled: false` | Feature disabled entirely |

## Commands & Permissions

| Command | Alias | Description | Permission |
|---|---|---|---|
| `/customdeathmessages reload` | `/cdm reload` | Reload the config live | `customdeathmessages.admin` |

`customdeathmessages.admin` defaults to **OP**.

## Building from Source

```bash
git clone https://github.com/choudesu/custom-death-messages.git
cd custom-death-messages
mvn package
# Output: target/custom-death-messages-<version>.jar
```

Requires Java 21 and Maven 3.8+.
