package net.bigpoint.assessment.gasstation.impl.managers;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by aaalekseev on 06-Aug-16.
 * Represents an individual worker with a single pump
 */
public class PumpWorker {
    /**
     * Current pump
     */
    private final GasPump gasPump;
    /**
     * Singleton for the pumping from the current pump
     */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    /**
     * Remaining amount in the current pump AFTER all the scheduled pumpings
     */
    private volatile double remainingGasAmount;

    PumpWorker(GasPump pump) {
        this.gasPump = pump;
        this.remainingGasAmount = pump.getRemainingAmount();
    }

    double getRemainingGas() {
        return remainingGasAmount;
    }

    /**
     * Method for scheduling of gas purchase. It is NOT thread-safe and should be synchronized by the caller
     * @param amountInLiters
     * @return
     */
    Future schedulePumpGas(double amountInLiters) throws NotEnoughGasException {
        if (remainingGasAmount < amountInLiters)
            throw new NotEnoughGasException();

        remainingGasAmount -= amountInLiters;
        return executor.submit(() -> gasPump.pumpGas(amountInLiters));
    }
}
