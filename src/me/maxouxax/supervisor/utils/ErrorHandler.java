package me.maxouxax.supervisor.utils;

import me.maxouxax.supervisor.Supervisor;

import java.util.Arrays;

public class ErrorHandler {

    private final Supervisor supervisor;

    public ErrorHandler() {
        this.supervisor = Supervisor.getInstance();
    }

    public void handleException(Throwable exception){
        supervisor.getLogger().error("Une erreur est survenue !\n"+exception.getMessage());
        exception.printStackTrace();
        supervisor.getLogger().error(exception.getMessage()+"\n"+Arrays.toString(exception.getStackTrace()), false);
    }



}
