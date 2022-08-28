package me.maxouxax.supervisor.database;

import me.maxouxax.supervisor.database.nosql.NoSQLDatabaseAccess;
import me.maxouxax.supervisor.database.sql.SQLDatabaseAccess;

public enum Databases {

    MARIADB("MARIADB", SQLDatabaseAccess.class),
    MONGODB("MONGODB", NoSQLDatabaseAccess.class),
    ;

    private String name;
    private Class<? extends IDatabaseAccess> classType;

    <T extends IDatabaseAccess> Databases(String name, Class<T> classType) {
        this.name = name;
        this.classType = classType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<? extends IDatabaseAccess> getClassType() {
        return classType;
    }

    public void setClassType(Class<? extends IDatabaseAccess> classType) {
        this.classType = classType;
    }

}
