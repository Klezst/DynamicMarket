package com.idragonfire.data;

import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.configuration.file.YamlConfiguration;

/**
 * DragonMessageLibary to load message from a yaml file
 * @author IDragonfire
 */
public class DMsg {

    public enum MsgKey {
	// add new messages here # START
	BUY_TOMUCH("buy.tomuch", "{ERR}You can't buy that much at once!"), 
	BUY_NOSPACE("buy.nospace", "{ERR} $shop$ doesn't have enough space"), 
	SELL_NOSTOCK("buy.nostock", "{ERR} $shop$ doesn't have enough stock."),
	HELP_LINE1("help.example1", "line1"),
	HELP_LINE2("help.example2", "line2"),
	HELP_LINE3("help.example3", "line3");
	// # END
	private final String key;
	private final String msg;

	private MsgKey(String key, String msg) {
	    this.key = key;
	    this.msg = msg;
	}

	public String getKey() {
	    return this.key;
	}

	public String getDefaultMsg() {
	    return this.msg;
	}

    }

    public static final DMsg INSTANCE = new DMsg();
    public static final String REPLACER = "$";
    private String msgLibPath = "plugins/DynamicMarket/messages.yml";
    private YamlConfiguration msgLib;

    //TODO add split function for multiline support
    public String get(MsgKey key) {
	return get(key, null);
    }

    public String get(MsgKey key, String[][] replacement) {
	String s = this.msgLib.getString(key.getKey(), key.getDefaultMsg());
	if (replacement != null) {
	    for (int i = 0; i < replacement.length; i++) {
		s = s.replace(DMsg.REPLACER + replacement[i][0]
			+ DMsg.REPLACER, replacement[i][1]);
	    }
	}
	return s;
    }

    private DMsg() {
	init();
    }

    // TODO: register logger or Messager, ...
    private void init() {
	try {
	    File f = new File(this.msgLibPath);
	    if (!f.exists()) {
		System.out.println("create : " + f.getName());
		f.createNewFile();
		this.msgLib = YamlConfiguration.loadConfiguration(f);
		initLibary();
		this.msgLib.save(f);
	    } else {
		this.msgLib = YamlConfiguration.loadConfiguration(f);
		checkLibary(f);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    private void checkLibary(File f) {
	boolean validate = true;
	Class<?> MsgLibClass = this.getClass();
	Field[] fields = MsgLibClass.getDeclaredFields();
	String[] tmpKey = null;
	for (int i = 0; i < fields.length; i++) {
	    if (fields[i].getType() == String[].class) {
		try {
		    tmpKey = (String[]) fields[i].get(null);
		    if (this.msgLib.get(tmpKey[0]) == null) {
			validate = false;
			break;
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	}
	if (!validate) {
	    backupAndReinit(f);
	}
    }

    // TODO: register logger or Messager, ...
    private void backupAndReinit(File f) {
	SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
	File newFile = new File(this.msgLibPath + ".bak"
		+ format.format(new Date()));
	System.out.println(this.msgLibPath + " not validate, backup to "
		+ newFile.getName() + " and generate new one.");
	f.renameTo(newFile);
	init();
    }

    private void initLibary() {
	Class<?> MsgLibClass = this.getClass();
	Field[] fields = MsgLibClass.getDeclaredFields();
	String[] tmpKey = null;
	for (int i = 0; i < fields.length; i++) {
	    if (fields[i].getType() == String[].class) {
		try {
		    tmpKey = (String[]) fields[i].get(null);
		    this.msgLib.set(tmpKey[0], tmpKey[1]);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	}
    }

    public static void main(String[] args) {
    }
}
