package edu.eci.blacklistvalidator;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import edu.eci.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

/**
 * Hilo que revisa un rango de servidores [start..end] buscando la IP dada.
 */
public class HostBlackListThread extends Thread {

    private final int start;
    private final int end;
    private final String ip;
    private final HostBlacklistsDataSourceFacade skds;
    private final AtomicInteger occurrencesGlobal;
    private final int threshold;

    private final List<Integer> found = new ArrayList<>();
    private int checkedCount = 0;

    public HostBlackListThread(int start, int end, String ip, HostBlacklistsDataSourceFacade skds,
            AtomicInteger occurrencesGlobal, int threshold) {
        this.start = start;
        this.end = end;
        this.ip = ip;
        this.skds = skds;
        this.occurrencesGlobal = occurrencesGlobal;
        this.threshold = threshold;
    }

    @Override
    public void run() {
        for (int i = start; i <= end; i++) {
            // Parada temprana global
            if (occurrencesGlobal.get() >= threshold) {
                break;
            }
            checkedCount++;
            try {
                if (skds.isInBlackListServer(i, ip)) {
                    found.add(i);
                    occurrencesGlobal.incrementAndGet();
                }
            } catch (Exception ex) {
                // Facade es thread-safe; en caso de excepción la ignoramos para no matar el hilo
            }
        }
    }

    public int getOcurrencesCount() {
        return found.size();
    }

    public List<Integer> getBlackListOcurrences() {
        return new ArrayList<>(found);
    }

    public int getCheckedCount() {
        return checkedCount;
    }
}
