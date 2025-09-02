package edu.eci.blacklistvalidator;

import java.util.*;
import java.util.logging.*;

/**
 * ExperimentMain :
 * - Si se pasa "all" como primer argumento, ejecuta la serie completa:
 *   1, p, 2p, 50, 100 (p = Runtime.availableProcessors()).
 * - Si se pasa un número entero como primer arg, usa ese número de hilos.
 * - Si no se pasa ningún argumento, solicita por consola el número de hilos.
 * - Opcional: segundo argumento puede ser la IP a probar.
 *
 */
public class ExperimentMain {

    // IP por defecto (dispersa) para el experimento real
    private static final String DEFAULT_IP = "202.24.34.55";

    // Runs: 1 warmup + 5 medibles
    private static final int RUNS = 6;

    public static void main(String[] args) {

        Logger.getLogger(HostBlackListsValidator.class.getName()).setLevel(Level.OFF);

        String ip = DEFAULT_IP;
        List<Integer> configsToRun = new ArrayList<>();

        // Interpretar argumentos
        if (args.length >= 1) {
            String first = args[0].trim();
            // Si el primer arg es "all", ejecutamos la serie completa
            if ("all".equalsIgnoreCase(first)) {
                int p = Runtime.getRuntime().availableProcessors();
                configsToRun = Arrays.asList(1, p, Math.max(1, 2 * p), 50, 100);
            } else {
                // intentamos parsear como entero
                try {
                    int threads = Integer.parseInt(first);
                    if (threads <= 0) {
                        System.err.println("El numero de hilos debe ser mayor que 0.");
                        System.exit(1);
                    }
                    configsToRun.add(threads);
                } catch (NumberFormatException nfe) {
                    System.err.println("Argumento invalido: se esperaba 'all' o un entero. Ejemplo: 8");
                    System.exit(1);
                }
            }
            // Si hay segundo argumento, usarlo como IP
            if (args.length >= 2) {
                ip = args[1].trim();
            }
        } else {
            // Si no hay argumentos, pedimos el número de hilos por consola
            Scanner sc = new Scanner(System.in);
            System.out.println("No se pasaron argumentos.");
            System.out.print("Introduce número de hilos (o escribe 'all' para ejecutar todas las configuraciones): ");
            String input = sc.nextLine().trim();
            if ("all".equalsIgnoreCase(input)) {
                int p = Runtime.getRuntime().availableProcessors();
                configsToRun = Arrays.asList(1, p, Math.max(1, 2 * p), 50, 100);
            } else {
                try {
                    int threads = Integer.parseInt(input);
                    if (threads <= 0) {
                        System.err.println("El numero de hilos debe ser mayor que 0.");
                        System.exit(1);
                    }
                    configsToRun.add(threads);
                } catch (NumberFormatException nfe) {
                    System.err.println("Entrada invalida. Saliendo.");
                    System.exit(1);
                }
            }
            System.out.print("Introduce la IP a testear (enter para usar " + DEFAULT_IP + "): ");
            String ipIn = sc.nextLine().trim();
            if (!ipIn.isEmpty()) ip = ipIn;
            sc.close();
        }

        // Mostrar info inicial
        System.out.println("# cores=" + Runtime.getRuntime().availableProcessors());
        System.out.println("IP=" + ip);
        System.out.println("threads,run,ms,occurrences");

        HostBlackListsValidator validator = new HostBlackListsValidator();

        // Ejecutar cada configuración por separado
        for (int threads : configsToRun) {
            System.out.println("==> Ejecutando configuración threads=" + threads);
            System.out.flush();

            for (int r = 1; r <= RUNS; r++) {
                System.out.println("Run " + r + " (threads=" + threads + ") - iniciando...");
                System.out.flush();

                long t0 = System.nanoTime();
                List<Integer> found = validator.checkHost(ip, threads);
                long t1 = System.nanoTime();
                long ms = (t1 - t0) / 1_000_000;

                System.out.printf(Locale.US, "%d,%d,%d,%d%n", threads, r, ms, found.size());
                System.out.flush();

                // pequeña pausa entre runs para visualizar en VisualVM
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            }

            System.out.println("==> Terminada configuración threads=" + threads);
            System.out.flush();

            // pausa un poco antes de la siguiente configuración
            try { Thread.sleep(400); } catch (InterruptedException ignored) {}
        }

        System.out.println("Experimento(s) finalizado(s).");
    }
}

