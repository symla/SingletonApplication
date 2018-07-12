package de.symla.java.general.singletonapplication.components;

import de.symla.java.general.singletonapplication.Daemon;

public abstract class ApplicationProcess implements Runnable {

    private Daemon relatedDaemon;

    public abstract void run();

    protected Daemon getRelatedDaemon() {
        return this.relatedDaemon;
    }

}
