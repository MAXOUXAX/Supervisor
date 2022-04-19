package me.maxouxax.supervisor.supervised;

import me.maxouxax.supervisor.Supervisor;
import me.maxouxax.supervisor.exceptions.InvalidSupervisedException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class SupervisedLoader {

    final Supervisor supervisor;
    private final List<SupervisedClassLoader> loaders = new CopyOnWriteArrayList<>();

    public SupervisedLoader(@NotNull Supervisor instance) {
        supervisor = instance;
    }

    @NotNull
    public Supervised loadSupervised(@NotNull final File file) throws InvalidSupervisedException {
        if (!file.exists()) {
            throw new InvalidSupervisedException(new FileNotFoundException(file.getPath() + " does not exist"));
        }

        final SupervisedDescriptionFile supervisedDescriptionFile;
        supervisedDescriptionFile = getSupervisedDescription(file);

        final File parentFile = file.getParentFile();
        final File dataFolder = new File(parentFile, supervisedDescriptionFile.getName());

        if (dataFolder.exists() && !dataFolder.isDirectory()) {
            throw new InvalidSupervisedException("A file named " + dataFolder + " exists and is not a directory (for Supervised " + supervisedDescriptionFile.getName() + ")");
        }

        final SupervisedClassLoader loader;
        try {
            loader = new SupervisedClassLoader(this, getClass().getClassLoader(), supervisedDescriptionFile, dataFolder, file);
        } catch (InvalidSupervisedException e) {
            throw e;
        } catch (Throwable ex) {
            throw new InvalidSupervisedException(ex);
        }

        loaders.add(loader);

        return loader.supervised;
    }

    public SupervisedDescriptionFile getSupervisedDescription(File file) throws InvalidSupervisedException {
        JarFile jar = null;
        InputStream stream = null;

        try {
            jar = new JarFile(file);
            JarEntry entry = jar.getJarEntry("supervised.yml");

            if (entry == null) {
                throw new InvalidSupervisedException(new FileNotFoundException("Jar does not contain supervised.yml"));
            }

            stream = jar.getInputStream(entry);

            return SupervisedDescriptionFile.fromFile(stream);
        } catch (IOException e) {
            throw new InvalidSupervisedException(e);
        } finally {
            if (jar != null) {
                try {
                    jar.close();
                } catch (IOException ignored) {
                }
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public void enableSupervised(@NotNull final Supervised supervised) {
        if (!supervised.isEnabled()) {
            supervisor.getLogger().info("Enabling " + supervised.getDescription().getName());

            SupervisedClassLoader supervisedClassLoader = (SupervisedClassLoader) supervised.getClassLoader();

            if (!loaders.contains(supervisedClassLoader)) {
                loaders.add(supervisedClassLoader);
                supervisor.getLogger().warn("Enabled supervised with unregistered SupervisedClassLoader " + supervised.getDescription().getName());
            }

            try {
                supervised.setEnabled(true);
            } catch (Throwable ex) {
                supervisor.getLogger().error("Error occurred while enabling " + supervised.getDescription().getName(), ex);
            }
        }
    }

    public void disableSupervised(@NotNull Supervised supervised) {
        if (supervised.isEnabled()) {
            String message = String.format("Disabling %s", supervised.getDescription().getName());
            supervisor.getLogger().info(message);

            ClassLoader classLoader = supervised.getClassLoader();

            try {
                supervised.setEnabled(false);
            } catch (Throwable ex) {
                supervisor.getLogger().error("Error occurred while disabling " + supervised.getDescription().getName(), ex);
            }

            if (classLoader instanceof SupervisedClassLoader loader) {
                loaders.remove(loader);

                try {
                    loader.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
