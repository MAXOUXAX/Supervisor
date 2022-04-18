package me.maxouxax.supervisor.supervised;

import me.maxouxax.supervisor.Supervisor;
import me.maxouxax.supervisor.exceptions.InvalidSupervisedException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

public class SupervisedManager {

    private final File SUPERVISED_DIRECTORY = new File("supervised/");
    private final ArrayList<Supervised> supervised = new ArrayList<>();
    private final Supervisor supervisor;

    public SupervisedManager() {
        this.supervisor = Supervisor.getInstance();
        loadAllSupervised(SUPERVISED_DIRECTORY);
    }

    public ArrayList<Supervised> getSupervised() {
        return this.supervised;
    }

    public void loadAllSupervised(@NotNull File directory) {
        if (directory.exists()) {
            for (File file : directory.listFiles()) {
                if(file.isDirectory() || !file.getName().endsWith(".jar")) continue;
                try {
                    Supervised loadedSupervised = loadSupervised(file);
                    supervisor.getCommandManager().registerSupervised(loadedSupervised);
                    if (loadedSupervised != null) {
                        supervised.add(loadedSupervised);
                        loadedSupervised.onLoad();
                    } else {
                        supervisor.getLogger().error("Could not load '" + file.getPath() + "' in folder '" + directory.getPath() + "'");
                    }
                } catch (InvalidSupervisedException ex) {
                    supervisor.getLogger().error("Could not load '" + file.getPath() + "' in folder '" + directory.getPath() + "'", ex);
                }
            }
        } else {
            directory.mkdirs();
        }
    }

    public Supervised loadSupervised(@NotNull File file) throws InvalidSupervisedException {
        SupervisedLoader loader = new SupervisedLoader(supervisor);
        Supervised result = loader.loadSupervised(file);

        supervised.add(result);

        return result;
    }

    public void enableAllSupervised() {
        supervised.forEach(supervised -> {
            if ((!supervised.isEnabled())) {
                enableSupervised(supervised);
            }
        });
    }

    public void enableSupervised(@NotNull final Supervised supervised) {
        if (!supervised.isEnabled()) {
            try {
                supervised.getSupervisedLoader().enablePlugin(supervised);
            } catch (Throwable ex) {
                supervisor.getLogger().error("Error occurred (in the plugin loader) while enabling " + supervised.getDescription().getName() + " (Is it up to date?)", ex);
            }
        }
    }

    public void disableAllSupervised() {
        supervised.forEach(this::disableSupervised);
    }

    public void disableSupervised(@NotNull final Supervised supervised) {
        if (supervised.isEnabled()) {
            try {
                supervised.getSupervisedLoader().disablePlugin(supervised);
            } catch (Throwable ex) {
                supervisor.getLogger().error("Error occurred (in the plugin loader) while disabling " + supervised.getDescription().getName() + " (Is it up to date?)", ex);
            }
        }
    }

}
