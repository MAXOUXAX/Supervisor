package me.maxouxax.supervisor.database;

import me.maxouxax.supervisor.Supervisor;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    public static List<IDatabaseAccess> databaseAccessList = new ArrayList<>();

    public static void initDatabaseConnection(File file) throws SQLException {
        DatabaseCredentials databaseCredentials;

        if(file.exists()) {

            try (final Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                Yaml yaml = new Yaml(new CustomClassLoaderConstructor(ClassLoader.getSystemClassLoader()));
                yaml.setBeanAccess(BeanAccess.FIELD);

                databaseCredentials = yaml.loadAs(reader, DatabaseCredentials.class);
                if (!databaseCredentials.isValid()) {
                    throw new SQLException("Database is not configured in " + file.getName());
                }
                IDatabaseAccess iDatabaseAccess = Databases.valueOf(databaseCredentials.getType()).getClassType().getConstructor(DatabaseCredentials.class).newInstance(databaseCredentials);
                iDatabaseAccess.initPool();
                databaseAccessList.add(iDatabaseAccess);
            } catch (IOException | NoSuchMethodException e) {
                Supervisor.getInstance().getErrorHandler().handleException(e);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static IDatabaseAccess getDatabaseAccess(String type) {
        return databaseAccessList.stream()
                .filter(iDatabaseAccess1 -> iDatabaseAccess1.getType().equalsIgnoreCase(type))
                .findFirst().orElse(null);
    }

    public static void closeDatabasesConnection() {
        for (IDatabaseAccess databaseAccess : databaseAccessList) {
            databaseAccess.closePool();
        }
    }

    public static List<IDatabaseAccess> getDatabaseAccessList() {
        return databaseAccessList;
    }

}