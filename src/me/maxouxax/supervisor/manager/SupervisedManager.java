package me.maxouxax.supervisor.manager;

import me.maxouxax.supervisor.api.Supervised;

import java.util.ArrayList;

public class SupervisedManager {

    private ArrayList<Supervised> supervisedBots = new ArrayList<>();

    public SupervisedManager() {
    }

    public ArrayList<Supervised> getSupervisedBots() {
        return this.supervisedBots;
    }

}
