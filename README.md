

# Storyline Of Mineyarg

**Version:** 1.1.1
**Author:** MrYArg1cH
**Minecraft:** 1.20.1
**Forge:** 47.2.0+

---

## Description

**Storyline Of Mineyarg** is a Minecraft mod that introduces a **real-time zombie apocalypse system**.
The mod features **mob evolution**, an **expanding eternal snowfall**, and a wide range of **customizable settings**.

---

## Main Features

### 🧟 Apocalypse System

* **Real Time:** Events occur based on *real-world time*, not in-game time
* **Mob Evolution:** Hostile mobs grow stronger as time passes
* **Expanding Snowfall:** A cold zone that spreads across the world, similar to the shrinking storm in *Fortnite*
* **Configurable Parameters:** Full control over all aspects of the apocalypse

### 📋 Commands (Operator Only)

```
/apocalypse start          - Starts the apocalypse
/apocalypse stop           - Stops the apocalypse and resets progress
/apocalypse pause          - Pauses the apocalypse
/apocalypse resume         - Resumes the paused apocalypse
/apocalypse settings       - Opens the settings GUI
/apocalypse setspawnsnow   - Sets the starting coordinates for snowfall
/apocalypse test           - Launches accelerated test mode
```

---

## ⚙️ Settings

### Time Parameters

* **Days before mob spawn increase** (1–100 days, default: 3)
* **Days before snowfall starts** (2–100 days, default: 2)
* **Days before mob evolution begins** (1–100 days, default: 7)

### Coefficients

* **Mob spawn rate multiplier** (1–100%, default: 50%)
* **Mob evolution rate multiplier** (1–100%, default: 75%)
* **Snowfall speed multiplier** (1–100%, default: 25%)

### Additional Settings

* **Progress logging** (enabled/disabled)
* **Snowfall start coordinates** (set via command)

---

## 🔄 Apocalypse Stages

### 1. Mob Spawn Increase

* The number of hostile mobs rises significantly
* Triggered after a configurable number of days

### 2. Snowfall Begins

* Eternal snow starts spreading from a defined origin
* The snow expands like a closing zone
* Water gradually freezes into ice

### 3. Mob Evolution

The process occurs in stages:

**Level 0 (after 1 week):**

* Mobs stop burning in sunlight

**Level 1 (after 3–4 more days):**

* Zombies gain fishing rods and ender pearls
* Improved armor and weapons
* Skeletons receive enchanted bows

**Level 2+ (every 3–4 days):**

* Movement speed buffs
* Increased jump height
* Enhanced attack damage

---

## 🛠️ Installation

### Requirements

* Minecraft 1.20.1
* Minecraft Forge 47.2.0 or newer
* Kotlin for Forge 4.4.0+

### Instructions

1. Install Minecraft Forge 1.20.1
2. Download the **Storyline Of Mineyarg** mod
3. Place the `.jar` file into the `mods` folder
4. Launch the game

---

## 🔧 Building from Source

### Requirements

* Java 17+
* Git

### Instructions

```bash
git clone <repository-url>
cd storylineofmineyarg
./gradlew build
```

The built mod will appear in the `build/libs/` directory.

---

## 📁 Configuration Files

The mod generates the following files in `config/storylineofmineyarg/`:

* `storylineofmineyarg-config.properties` — main configuration
* `apocalypse-data.properties` — apocalypse progress data
* `logs/` — directory for log files (server only)

---

## 🎮 Usage

### Starting the Apocalypse

1. Set the snowfall coordinates using `/apocalypse setspawnsnow`
2. Configure parameters using `/apocalypse settings`
3. Start the apocalypse with `/apocalypse start`

### Testing

* Use `/apocalypse test` to quickly simulate all events
* In test mode, all time-based events happen much faster

### Control Commands

* `/apocalypse pause` — pause the apocalypse (progress saved)
* `/apocalypse resume` — resume the apocalypse
* `/apocalypse stop` — stop completely and reset all progress

---

## 🐛 Known Issues

* The settings GUI may not display correctly on some servers
* Performance may drop during test mode

---

## 📞 Support

If you experience issues:

1. Check `logs/latest.log`
2. Make sure you’re using the correct Forge version
3. Verify compatibility with other mods

---

## 🔄 Version History

### v1.1.1

* Fixed a bunch of bugs

### v1.1.0

* Bug fixes and minor stability improvements

### v1.0.0

* Initial release
* Core apocalypse system
* Command framework
* Settings GUI
* Progress logging
* Test mode
