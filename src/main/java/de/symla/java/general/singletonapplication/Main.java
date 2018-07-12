package de.symla.java.general.singletonapplication;

public class Main {

    public static void main(String[] args) {

        final Gui g = new Gui();

        Daemon.start(13080, g);

//        Thread.getAllStackTraces().keySet().forEach(thread -> {
//            System.out.println(thread.getName() +" - "+thread.getId()+" - "+thread.getState());
//        });

    }

}
