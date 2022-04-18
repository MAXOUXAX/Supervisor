package me.maxouxax.supervisor.supervised;

import me.maxouxax.supervisor.Supervisor;
import me.maxouxax.supervisor.manager.GlobalListener;
import me.maxouxax.supervisor.serversconfig.ServerConfigsManager;
import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Represents the Supervised BOT
 */
public abstract class Supervised {

    private boolean isEnabled = false;
    public ServerConfigsManager serverConfigsManager;
    public JDA jda;
    public Supervisor supervisor;
    private SupervisedLoader supervisedLoader = null;
    private GlobalListener globalListener;
    private File file = null;
    private File dataFolder = null;
    private File configFile = null;
    private Config config = null;
    private ClassLoader classLoader;
    private SupervisedDescriptionFile description;

    public Supervised() {
        final ClassLoader classLoader = this.getClass().getClassLoader();
        if (!(classLoader instanceof SupervisedClassLoader)) {
            throw new IllegalStateException("JavaPlugin requires " + SupervisedClassLoader.class.getName());
        }
        ((SupervisedClassLoader) classLoader).initialize(this);
    }

    public final ClassLoader getClassLoader() {
        return classLoader;
    }

    public SupervisedDescriptionFile getDescription() {
        return description;
    }

    public SupervisedLoader getSupervisedLoader() {
        return supervisedLoader;
    }

    /**
     * This method will be called when loading the BOT in, right before starting it
     */
    public void onLoad() {
        this.supervisor = Supervisor.getInstance();
    }

    /**
     * This method will be called when the BOT is being started
     */
    public void onEnable() {
        this.serverConfigsManager.loadConfigs();
    }

    /**
     * This method will be called when the BOT is being stopped
     */
    public abstract void onDisable();

    /**
     * This method will be called when the BOT is being reloaded
     */
    public abstract void onReload();

    public boolean isEnabled() {
        return isEnabled;
    }

    public final void setEnabled(boolean enabled) {
        if (isEnabled != enabled) {
            isEnabled = enabled;

            if (isEnabled) {
                onEnable();
            } else {
                onDisable();
            }
        }
    }

    public void saveDefaultConfig(){
        if (!configFile.exists()) {
            saveResource("config.yml", false);
        }
    }

    public void saveResource(String resourcePath, boolean replace) {
        if (resourcePath == null || resourcePath.equals("")) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }
        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + getDescription().getName());
        }
        File outFile = new File(dataFolder, resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(dataFolder, resourcePath.substring(0, Math.max(lastIndex, 0)));
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        try {
            if (!outFile.exists() || replace) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            } else {
                in.close();
            }
        } catch (IOException ex) {
            throw new RuntimeException("I/O error on resource path " + resourcePath, ex);
        }
    }

    public InputStream getResource(@NotNull String fileName) {
        try {
            URL url = getClassLoader().getResource(fileName);

            if (url == null) {
                return null;
            }

            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * Loads the config file parsing it as the given class
     * @param configClass The class of the config
     */
    public <T extends Config> void loadConfig(Class<T> configClass) {

        if(this.configFile.exists()) {

            Yaml yaml = new Yaml(new CustomClassLoaderConstructor(this.classLoader));
            yaml.setBeanAccess(BeanAccess.FIELD);

            try {
                InputStream inputStream = new FileInputStream(this.configFile);
                this.config = yaml.loadAs(inputStream, configClass);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            this.configFile.getParentFile().mkdirs();
            try {
                this.configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ServerConfigsManager getServerConfigsManager() {
        return serverConfigsManager;
    }

    public JDA getJda() {
        return this.jda;
    }

    public File getDataFolder() {
        return this.dataFolder;
    }

    public Config getConfig() {
        return this.config;
    }

    public void bindListeners() {
        this.jda.addEventListener(this.globalListener);
    }

    final void init(@NotNull SupervisedLoader supervisedLoader, Supervisor supervisor, @NotNull SupervisedDescriptionFile description, @NotNull File dataFolder, @NotNull File file, @NotNull ClassLoader classLoader) {
        this.supervisedLoader = supervisedLoader;
        this.supervisor = supervisor;
        this.globalListener = new GlobalListener(this, supervisor);
        this.file = file;
        this.description = description;
        this.dataFolder = dataFolder;
        this.classLoader = classLoader;
        this.configFile = new File(dataFolder, "config.yml");
    }

}
