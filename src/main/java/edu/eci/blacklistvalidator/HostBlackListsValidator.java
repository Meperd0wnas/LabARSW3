package edu.eci.blacklistvalidator;

import edu.eci.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean; // MODIFICACIÓN: agregado
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HostBlackListsValidator {

    private static final int BLACK_LIST_ALARM_COUNT = 5;
    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());

    /**
     * Versión original (sobrecarga) que mantiene compatibilidad.
     */
    public List<Integer> checkHost(String ipaddress) {
        // puedes elegir un N por defecto razonable (p. ej. 10)
        return checkHost(ipaddress, 10);
    }

    /**
     * Versión paralelizada: divide la búsqueda entre N hilos.
     *
     * @param ipaddress IP a chequear
     * @param N número de hilos
     * @return índices de listas negras donde se encontró la IP
     */
    public List<Integer> checkHost(String ipaddress, int N) {

        LinkedList<Integer> blackListOcurrences = new LinkedList<>();

        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();
        int totalServers = skds.getRegisteredServersCount();

        if (totalServers <= 0) {
            LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{0, totalServers});
            skds.reportAsTrustworthy(ipaddress);
            return blackListOcurrences;
        }

        // Validar N
        if (N <= 0) N = 1;
        if (N > totalServers) N = totalServers;

        // Particionamiento equitativo
        int baseRange = totalServers / N;
        int remainder = totalServers % N;

        AtomicInteger occurrencesGlobal = new AtomicInteger(0);

        //  MODIFICACIÓN: bandera de parada
        AtomicBoolean stopFlag = new AtomicBoolean(false); 
        
        HostBlackListThread[] threads = new HostBlackListThread[N];

        int start = 0;
        for (int i = 0; i < N; i++) {
            int extra = (i < remainder) ? 1 : 0;
            int end = start + baseRange + extra - 1;
            if (end >= totalServers) end = totalServers - 1;

            // 🔹 MODIFICACIÓN: pasar stopFlag al constructor
            threads[i] = new HostBlackListThread(start, end, ipaddress, skds,
                    occurrencesGlobal, BLACK_LIST_ALARM_COUNT, stopFlag);

            start = end + 1;
        }

        // Lanzar hilos
        for (HostBlackListThread t : threads) {
            t.start();
        }

        // Esperar terminación
        for (HostBlackListThread t : threads) {
            try {
                t.join();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOG.log(Level.WARNING, "Se interrumpió la espera de los hilos", ex);
            }
        }

        // Recolectar resultados y contar servidores chequeados
        int checkedListsCount = 0;
        int totalOcurrences = 0;

        for (HostBlackListThread t : threads) {
            checkedListsCount += t.getCheckedCount();
            totalOcurrences += t.getOcurrencesCount();
            blackListOcurrences.addAll(t.getBlackListOcurrences());
        }

        // LOG verídico
        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{checkedListsCount, totalServers});

        // Reporte según política
        if (totalOcurrences >= BLACK_LIST_ALARM_COUNT) {
            skds.reportAsNotTrustworthy(ipaddress);
        } else {
            skds.reportAsTrustworthy(ipaddress);
        }

        return blackListOcurrences;
    }
}
