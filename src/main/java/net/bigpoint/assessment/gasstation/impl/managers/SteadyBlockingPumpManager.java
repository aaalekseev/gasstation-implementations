package net.bigpoint.assessment.gasstation.impl.managers;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

import java.util.Comparator;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by aaalekseev on 06-Aug-16.
 */
public class SteadyBlockingPumpManager implements PumpManager {
    /**
     * Thread-safe queue for elimination of simultaneous pumping and selecting pump with the highest remaining amount.
     */
    private final Queue<PumpWorker> pumpWorkersQueue = new PriorityBlockingQueue<>(1, new GasPumpWorkerComparator());

    public void pumpGas(double amountInLiters) throws NotEnoughGasException {
        // Synchronize selection of the pumpWorker with the largest amount. Release pumpWorker right after scheduling
        Future pumpingFuture;
        synchronized (this) {
            PumpWorker pumpWorker = null;
            try {
                pumpWorker = pumpWorkersQueue.poll();
                // We selected pumpWorker with the highest remaining amount. If it is not enough - NotEnoughGasException
                if (pumpWorker == null || pumpWorker.getRemainingGas() < amountInLiters)
                    throw new NotEnoughGasException();

                pumpingFuture = pumpWorker.schedulePumpGas(amountInLiters);
            } finally {
                if(pumpWorker != null)
                    pumpWorkersQueue.add(pumpWorker);
            }
        }

        // Wait till pumping finish in the request thread
        try {
            pumpingFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add gas pump to the proper structures
     * @param gasPump
     */
    public void addGasPump(GasPump gasPump) {
        pumpWorkersQueue.add(new PumpWorker(gasPump));
    }

    /**
     * Pump workers comparator for the selection of the largest remaining amount pump, excepting scheduled
     */
    private static class GasPumpWorkerComparator implements Comparator<PumpWorker> {
        public int compare(PumpWorker o1, PumpWorker o2) {
            return Double.compare(o2.getRemainingGas(), o1.getRemainingGas());
        }
    }
}
