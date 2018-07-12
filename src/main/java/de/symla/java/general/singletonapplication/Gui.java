package de.symla.java.general.singletonapplication;

import de.symla.java.general.singletonapplication.components.ApplicationGui;

import javax.swing.*;

public class Gui extends ApplicationGui {

    private JFrame frame;

    public Gui() {}

    @Override
    public void showGui() {
        validateStarted();
        if ( this.isGuiVisible() )
            throw new IllegalStateException("Already visible.");

        this.frame.setVisible(true);
        this.frame.toFront();
        this.frame.requestFocus();
    }

    @Override
    public void hideGui() {
        validateStarted();
        if ( !this.isGuiVisible() )
            throw new IllegalStateException("Already invisible.");

        this.frame.setVisible(false);
    }

    @Override
    public boolean isGuiVisible() {
        validateStarted();
        return this.frame.isVisible();
    }

    private void validateStarted() {
        if ( this.frame == null )
            throw new IllegalStateException("Has not been started within an own Thread yet.");
    }

    @Override
    public void run() {
        this.frame = new JFrame("de.symla.java.general.singletonapplication.Gui");
        this.frame.setSize(800, 500);
        this.frame.setLocationRelativeTo(null);

        this.frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.showGui();
    }
}
