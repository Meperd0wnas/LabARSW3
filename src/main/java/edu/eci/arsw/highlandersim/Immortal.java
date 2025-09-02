package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback = null;

    private int health;
    private int defaultDamageValue;
    private final List<Immortal> immortalsPopulation;
    private final String name;
    private final Random r = new Random(System.currentTimeMillis());

    // 🔹 Variable global de control de pausa
    private static AtomicBoolean paused = new AtomicBoolean(false);

    // 🔹 Flag para detener el hilo de manera segura
    private volatile boolean running = true;

    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue,
                    ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback = ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue = defaultDamageValue;
    }

    public void run() {
        while (running) {  // 🔹 Ahora el bucle termina cuando running = false

            // 🔹 Chequea si está en pausa
            synchronized (paused) {
                while (paused.get()) {
                    try {
                        paused.wait(); // Espera hasta que alguien haga "resume"
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            Immortal im;
            int myIndex = immortalsPopulation.indexOf(this);
            if (!immortalsPopulation.isEmpty()) {  // 🔹 Evitar errores si la lista se vacía
                int nextFighterIndex = r.nextInt(immortalsPopulation.size());

                // 🔹 evitar pelear consigo mismo
                if (nextFighterIndex == myIndex) {
                    nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
                }

                im = immortalsPopulation.get(nextFighterIndex);

                this.fight(im);
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 🔹 Informar que el hilo terminó
        updateCallback.processReport(this + " has stopped running.\n");
    }

    public void fight(Immortal i2) {
        // 🔹 Evitar deadlocks: siempre bloquear en un orden fijo como vimos en clase con hashCodes
        Immortal first, second;
        if (this.hashCode() < i2.hashCode()) {
            first = this;
            second = i2;
        } else {
            first = i2;
            second = this;
        }

        synchronized (first) {
            synchronized (second) {
                if (i2.getHealth() > 0 && this.getHealth() > 0) {
                    i2.changeHealth(i2.getHealth() - defaultDamageValue);
                    this.health += defaultDamageValue;
                    updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
                } else {
                    updateCallback.processReport(this + " says: " + i2 + " is already dead!\n");
                }

                if (this.health <= 0) {
                    immortalsPopulation.remove(this); // 🔹 Eliminarse a sí mismo
                    updateCallback.processReport(this + " has died and was removed.\n");
                }
            }
        }
    }

    public void changeHealth(int v) {
        health = v;
    }

    public int getHealth() {
        return health;
    }

    @Override
    public String toString() {
        return name + "[" + health + "]";
    }

    // 🔹 Métodos para pausar y reanudar desde afuera (ej. GUI o main)
    public static void pauseAll() {
        paused.set(true);
    }

    public static void resumeAll() {
        synchronized (paused) {
            paused.set(false);
            paused.notifyAll(); // 🔹 Despierta a todos los hilos
        }
    }

    // 🔹 Método seguro para detener un hilo individual
    public void stopRunning() {
        running = false;
    }
}

