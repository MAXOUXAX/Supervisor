package me.maxouxax.supervisor.supervised;

import me.maxouxax.supervisor.exceptions.InvalidSupervisedException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;

public final class SupervisedClassLoader extends URLClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    final Supervised supervised;
    private final SupervisedLoader loader;
    private final SupervisedDescriptionFile description;
    private final File dataFolder;
    private Supervised supervisedInit;
    private IllegalStateException supervisedState;

    SupervisedClassLoader(final SupervisedLoader loader, final ClassLoader parent, final SupervisedDescriptionFile description, File dataFolder, File file) throws InvalidSupervisedException, IOException {
        super(new URL[]{file.toURI().toURL()}, parent);

        this.loader = loader;
        this.description = description;
        this.dataFolder = dataFolder;

        try {
            Class<?> jarClass;
            try {
                jarClass = Class.forName(description.getMain(), true, this);
            } catch (ClassNotFoundException ex) {
                throw new InvalidSupervisedException("Cannot find main class `" + description.getMain() + "'", ex);
            }

            Class<? extends Supervised> supervisedClass;
            try {
                supervisedClass = jarClass.asSubclass(Supervised.class);
            } catch (ClassCastException ex) {
                throw new InvalidSupervisedException("main class `" + description.getMain() + "' does not extend Supervised", ex);
            }

            supervised = supervisedClass.getDeclaredConstructor().newInstance();
        } catch (IllegalAccessException ex) {
            throw new InvalidSupervisedException("No public constructor", ex);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException ex) {
            throw new InvalidSupervisedException("Abnormal supervised type", ex);
        }
    }

    synchronized void initialize(@NotNull Supervised supervised) {
        if (this.supervised != null || this.supervisedInit != null) {
            throw new IllegalArgumentException("Supervised already initialized!", supervisedState);
        }

        supervisedState = new IllegalStateException("Initial initialization");
        this.supervisedInit = supervised;

        supervised.init(loader, loader.supervisor, description, dataFolder, this);
    }

}