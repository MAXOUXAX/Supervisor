package me.maxouxax.supervisor.database;

public interface IDatabaseAccess {

    String getType();
    void initPool();
    void closePool();

}
