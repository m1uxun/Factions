package com.massivecraft.factions.zcore;

import java.lang.reflect.Modifier;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.massivecraft.factions.zcore.persist.EM;
import com.massivecraft.factions.zcore.persist.SaveTask;
import com.massivecraft.mcore.util.Txt;
import com.massivecraft.mcore.xlib.gson.Gson;
import com.massivecraft.mcore.xlib.gson.GsonBuilder;


public abstract class MPlugin extends JavaPlugin
{	
	// Persist related
	public Gson gson;	
	private Integer saveTask = null;
	private boolean autoSave = true;
	protected boolean loadSuccessful = false;
	public boolean getAutoSave() {return this.autoSave;}
	public void setAutoSave(boolean val) {this.autoSave = val;}
	
	// Listeners
	public MPluginSecretPlayerListener mPluginSecretPlayerListener; 

	// -------------------------------------------- //
	// ENABLE
	// -------------------------------------------- //
	private long timeEnableStart;
	public boolean preEnable()
	{
		log("=== ENABLE START ===");
		timeEnableStart = System.currentTimeMillis();
		
		// Ensure basefolder exists!
		this.getDataFolder().mkdirs();

		this.gson = this.getGsonBuilder().create();

		// Create and register listeners
		this.mPluginSecretPlayerListener = new MPluginSecretPlayerListener(this);
		
		// Register recurring tasks
		long saveTicks = 20 * 60 * 30; // Approximately every 30 min
		if (saveTask == null)
		{
			saveTask = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new SaveTask(this), saveTicks, saveTicks);
		}

		loadSuccessful = true;
		return true;
	}
	
	public void postEnable()
	{
		log("=== ENABLE DONE (Took "+(System.currentTimeMillis()-timeEnableStart)+"ms) ===");
	}
	
	public void onDisable()
	{
		if (saveTask != null)
		{
			this.getServer().getScheduler().cancelTask(saveTask);
			saveTask = null;
		}
		// only save data if plugin actually loaded successfully
		if (loadSuccessful)
			EM.saveAllToDisc();
		log("Disabled");
	}
	
	public void suicide()
	{
		log("Now I suicide!");
		this.getServer().getPluginManager().disablePlugin(this);
	}

	// -------------------------------------------- //
	// Some inits...
	// You are supposed to override these in the plugin if you aren't satisfied with the defaults
	// The goal is that you always will be satisfied though.
	// -------------------------------------------- //

	public GsonBuilder getGsonBuilder()
	{
		return new GsonBuilder()
		.setPrettyPrinting()
		.disableHtmlEscaping()
		.serializeNulls()
		.excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.VOLATILE);
	}
	
	// -------------------------------------------- //
	// LANG AND TAGS
	// -------------------------------------------- //
	/*
	// These are not supposed to be used directly.
	// They are loaded and used through the TextUtil instance for the plugin.
	public Map<String, String> rawTags = new LinkedHashMap<String, String>();
	
	public void addRawTags()
	{
		this.rawTags.put("l", "<green>"); // logo
		this.rawTags.put("a", "<gold>"); // art
		this.rawTags.put("n", "<silver>"); // notice
		this.rawTags.put("i", "<yellow>"); // info
		this.rawTags.put("g", "<lime>"); // good
		this.rawTags.put("b", "<rose>"); // bad
		this.rawTags.put("h", "<pink>"); // highligh
		this.rawTags.put("c", "<aqua>"); // command
		this.rawTags.put("p", "<teal>"); // parameter
	}
	
	public void initTXT()
	{
		this.addRawTags();
		
		Type type = new TypeToken<Map<String, String>>(){}.getType();
		
		Map<String, String> tagsFromFile = this.persist.load(type, "tags");
		if (tagsFromFile != null) this.rawTags.putAll(tagsFromFile);
		this.persist.save(this.rawTags, "tags");
		
		for (Entry<String, String> rawTag : this.rawTags.entrySet())
		{
			this.txt.tags.put(rawTag.getKey(), TextUtil.parseColor(rawTag.getValue()));
		}
	}*/
	
	// -------------------------------------------- //
	// HOOKS
	// -------------------------------------------- //
	public void preAutoSave()
	{
		
	}
	
	public void postAutoSave()
	{
		
	}
	
	// -------------------------------------------- //
	// LOGGING
	// -------------------------------------------- //
	public void log(Object msg)
	{
		log(Level.INFO, msg);
	}

	public void log(String str, Object... args)
	{
		log(Level.INFO, Txt.parse(str, args));
	}

	public void log(Level level, String str, Object... args)
	{
		log(level, Txt.parse(str, args));
	}

	public void log(Level level, Object msg)
	{
		Bukkit.getLogger().log(level, "["+this.getDescription().getFullName()+"] "+msg);
	}
}
