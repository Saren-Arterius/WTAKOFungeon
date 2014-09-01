package net.wtako.WTAKOFungeon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.wtako.WTAKOFungeon.Commands.CommandWFun;
import net.wtako.WTAKOFungeon.EventHandlers.FungeonSignListener;
import net.wtako.WTAKOFungeon.EventHandlers.FungeonWizardListener;
import net.wtako.WTAKOFungeon.EventHandlers.PlayerGameListener;
import net.wtako.WTAKOFungeon.EventHandlers.PlayerItemDropListener;
import net.wtako.WTAKOFungeon.Methods.Database;
import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Schedulers.FungeonScheduler;
import net.wtako.WTAKOFungeon.Utils.Config;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main             instance;
    public static Economy           econ;
    public static String            artifactId;
    public static YamlConfiguration LANG;
    public static File              LANG_FILE;
    public static Logger            log = Logger.getLogger("WTAKOFungeon");

    @Override
    public void onEnable() {
        Main.instance = this;
        Main.artifactId = getProperty("artifactId");
        Config.saveAll();
        loadLang();
        setupEcon();
        getCommand(getProperty("mainCommand")).setExecutor(new CommandWFun());
        if (Database.getInstance() == null) {
            try {
                new Database();
                Fungeon.loadAllFungeons();
            } catch (final SQLException e) {
                e.printStackTrace();
            }
        }
        if (FungeonScheduler.getInstance() == null) {
            new FungeonScheduler();
        }
        getServer().getPluginManager().registerEvents(new FungeonWizardListener(), this);
        getServer().getPluginManager().registerEvents(new FungeonSignListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerItemDropListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerGameListener(), this);
    }

    @Override
    public void onDisable() {
        try {
            for (final Fungeon fungeon: Fungeon.getAllFungeons().values()) {
                fungeon.forceResetAll();
            }
            Fungeon.getAllFungeons().clear();
            Database.reset();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public void setupEcon() {
        final RegisteredServiceProvider<Economy> economyProvider = Main.getInstance().getServer().getServicesManager()
                .getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            Main.econ = economyProvider.getProvider();
        }
    }

    /**
     * Gets the messages.yml config.
     * 
     * @return The messages.yml config.
     */
    public YamlConfiguration getLang() {
        return Main.LANG;
    }

    /**
     * Get the messages.yml file.
     * 
     * @return The messages.yml file.
     */
    public File getLangFile() {
        return Main.LANG_FILE;
    }

    @SuppressWarnings("deprecation")
    public String getProperty(String key) {
        final YamlConfiguration spawnConfig = YamlConfiguration.loadConfiguration(getResource("plugin.yml"));
        return spawnConfig.getString(key);
    }

    @SuppressWarnings("deprecation")
    public void loadLang() {
        final File lang = new File(getDataFolder(), "messages.yml");
        if (!lang.exists()) {
            try {
                getDataFolder().mkdir();
                lang.createNewFile();
                final InputStream defConfigStream = getResource("messages.yml");
                if (defConfigStream != null) {
                    final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                    defConfig.save(lang);
                    Lang.setFile(defConfig);
                    return;
                }
            } catch (final IOException e) {
                e.printStackTrace(); // So they notice
                Main.log.severe("[" + Main.getInstance().getName() + "] Couldn't create language file.");
                Main.log.severe("[" + Main.getInstance().getName() + "] This is a fatal error. Now disabling");
                setEnabled(false); // Without it loaded, we can't send them
                // messages
            }
        }
        final YamlConfiguration conf = YamlConfiguration.loadConfiguration(lang);
        for (final Lang item: Lang.values()) {
            if (conf.getString(item.getPath()) == null) {
                conf.set(item.getPath(), item.getDefault());
            }
        }
        Lang.setFile(conf);
        Main.LANG = conf;
        Main.LANG_FILE = lang;
        try {
            conf.save(getLangFile());
        } catch (final IOException e) {
            Main.log.log(Level.WARNING, "[" + Main.getInstance().getName() + "] Failed to save messages.yml.");
            Main.log.log(Level.WARNING, "[" + Main.getInstance().getName() + "] Report this stack trace to "
                    + getProperty("author") + ".");
            e.printStackTrace();
        }
    }

    public static Main getInstance() {
        return Main.instance;
    }

}
