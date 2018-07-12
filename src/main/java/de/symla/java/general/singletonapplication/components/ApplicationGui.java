package de.symla.java.general.singletonapplication.components;

import de.symla.java.general.singletonapplication.Daemon;

public abstract class ApplicationGui implements Runnable {

    private Daemon relatedDaemon;

    public abstract void showGui();

    public abstract void hideGui();

    public abstract boolean isGuiVisible();

    public abstract void run();

    protected Daemon getRelatedDaemon() {
        return this.relatedDaemon;
    }

}
