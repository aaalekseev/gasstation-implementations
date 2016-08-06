package net.bigpoint.assessment.gasstation.impl.managers;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by aaalekseev on 06-Aug-16.
 */
public class SteadyPumpManager implements PumpManager {
    /**
     * Thread-safe queue for elimination of simultaneous pumping and selecting pump with the highest remaining amount
     */
    private final Queue<GasPump> pumpQueue = new PriorityBlockingQueue(1, new GasPumpComparator());

    /**
     * Takes the pump with largest remaining amount and pump it. If no pumps available at this moment - NotEnoughGasException is thrown
     * @param amountInLiters
     * @throws NotEnoughGasException
     */
    public void pumpGas(double amountInLiters) throws NotEnoughGasException {
        GasPump gasPump = null;
        try {
            gasPump = pumpQueue.poll();
            if (gasPump == null || gasPump.getRemainingAmount() < amountInLiters)
                throw new NotEnoughGasException();

            gasPump.pumpGas(amountInLiters);
        } finally {
            if (gasPump != null)
                pumpQueue.add(gasPump);
        }
    }

    /**
     * Add gas pump to the proper structures
     * @param gasPump
     */
    public void addGasPump(GasPump gasPump) {
        pumpQueue.add(gasPump);
    }

    /**
     * Gas pump comparator for the selection of the largest remaining amount pump
     */
    private static class GasPumpComparator implements Comparator<GasPump> {
        public int compare(GasPump o1, GasPump o2) {
            return Double.compare(o2.getRemainingAmount(), o1.getRemainingAmount());
        }
    }
}
