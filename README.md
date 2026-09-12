# ⚔️ ISHS RPG

A Minecraft RPG server plugin exclusively for ISHS (Incheon Science High School) students.
Player identity is verified through **RiroSchool account authentication** (real name, student ID, and cohort), and the plugin brings together a **7-club faction system**, a custom stat/combat system, custom items, and quests on a single server.

- **API**: PaperMC 1.21
- **Java**: 21+
- **License**: [MIT](LICENSE)

---

## 🌟 Features

### RiroSchool Authentication
- Running `/auth` and then typing a RiroSchool ID/password in chat logs the player straight into RiroSchool to verify their real name, student ID, and cohort.
- Some cosmetic effects (e.g. the glowing outline) are withheld until authentication succeeds, then applied automatically.
- Admins can run `/entrygate [open|close]` to keep the server online while blocking new logins — players who are already connected are unaffected.

### Clubs (Classes)
| Club | Theme | Skill (Shift + F) |
|---|---|---|
| 📘 LOGIC | Math · Magic | Gravity Warp |
| 🔨 WIN | Physics · Destruction | Earthquake Slam |
| 🧨 TNT | Chemistry · Explosives | Chain Explosion |
| 🌾 LOTTOL | Biology · Recovery | Wave of Life |
| 🔮 QUASAR | Astronomy · Ranged | Black Hole |
| 💻 RAIBIT | Computer Science · Utility | Overclock / Glitch Teleport |
| ⚙️ PLUTONIUM | Engineering · Defense | Nano Barrier |

### Stats & Combat
- A custom HP/MP system built around 5 stats — Strength, Dexterity, Intelligence, HP, and Wisdom — freely allocated with `/stat`.
- Starting stats and starting stat points for new characters can be tuned in `config.yml` under `starting-stats`, no code changes required.
- In survival mode, players can fight (melee) but block breaking/placing is disabled.

### Custom Items
A self-contained item system that applies an enchant-glint effect to vanilla materials. Items are granted with `/giveitem [player] [itemId]` or handed out automatically as BetonQuest quest rewards. See the full list in [docs/ITEM_LIST.md](docs/ITEM_LIST.md).

### NPCs & Quests
NPCs are managed with **Citizens**, and dialogue/quests with **BetonQuest**. Dialogue is wired up automatically as long as a Citizens NPC's ID matches the expected mapping, so new NPCs can be placed without touching any code. See the ID mapping table in [docs/NPC_LIST.md](docs/NPC_LIST.md).

### Achievements
`/achievements` opens a 3x3 grid GUI showing which achievements have been completed and overall progress.

---

## 🎮 Commands

### Player
| Command | Description |
|---|---|
| `/help` (`/?`) | Command list and tips |
| `/auth`, `/authinfo` | Start RiroSchool authentication / check your own auth info |
| `/stat` | Stat allocation GUI |
| `/quest` (`/q`) | List of quests in progress |
| `/achievements` | Achievement progress GUI |
| `/lobby` (`/l`) | Teleport to the lobby |

### Admin
| Command | Description |
|---|---|
| `/authlist` | Authentication status of currently connected players (`ishsrpg.auth.admin`) |
| `/giveitem [player] [itemId]` | Grant a custom item |
| `/entrygate [open\|close]` | Allow/block new logins while keeping the server online |
| `/npc ...` | NPC creation/management uses the Citizens plugin's own commands |

> The in-game commands are Korean by default (e.g. `/도움말`, `/인증`, `/퀘스트`, `/도전과제`, `/아이템지급`, `/입장허용`); the English names above are for reference. See [plugin.yml](src/main/resources/plugin.yml) for the exact command names and aliases.

---

## 🧩 Companion Plugins

| Role | Plugin |
|---|---|
| NPCs | Citizens |
| Quests / dialogue | BetonQuest |
| Mobs | MythicMobs |
| Placeholders | PlaceholderAPI |

Custom placeholders such as `%ishsrpg_club%` expose club/level info to other plugins.

## ⚙️ Deploying to a Server

1. Drop the plugins above plus this plugin's build output (`build/libs/*.jar`) into the server's `plugins/` folder.
2. Copy the BetonQuest configuration under [`configs/`](configs) into the server's `plugins/BetonQuest/QuestPackages/` path as-is. See [configs/README.md](configs/README.md) for details.
3. On first boot, `config.yml` is generated automatically — starting stats, entry-gate state, and more can be adjusted there.
4. Non-code changes (quest dialogue, NPC placement, mob stats, etc.) only require editing the relevant `.yml` files; a rebuild is only needed when the Java source changes.

> ⚠️ `/reload` will **not** pick up code changes. Paper/Bukkit's `/reload` does not re-read a new jar from disk, so a full server restart is required after rebuilding.

## 🛠️ Building (for developers)

```bash
git clone https://github.com/dohye0508/ishsMinecraftRPG.git
cd ishsMinecraftRPG
./gradlew build
```

The built jar is placed in `build/libs/`.

## 📁 Documentation

- [docs/NPC_LIST.md](docs/NPC_LIST.md) — NPC ID mapping guide
- [docs/ITEM_LIST.md](docs/ITEM_LIST.md) — Custom item list and how to grant them
- [docs/TUTORIAL_GUIDE.md](docs/TUTORIAL_GUIDE.md) — New-student tutorial guide
- [docs/WORLDVIEW.md](docs/WORLDVIEW.md) — Setting/lore
- [configs/README.md](configs/README.md) — How to apply the pre-built config files
