package me.maxouxax.supervisor.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value=ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    String name();
    String description() default "Aucune description n'a été fournie";
    String help() default "Aucune aide n'a été fournie";
    String example() default "Aucun exemple n'a été fourni";
    boolean guildOnly() default true;

    int power() default 0;

}
