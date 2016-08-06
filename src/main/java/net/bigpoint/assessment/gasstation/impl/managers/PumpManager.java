package net.bigpoint.assessment.gasstation.impl.managers;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

/**
 * Created by aaalekseev on 06-Aug-16.
 * Unified interface for the different pump manager strategies
 */
public interface PumpManager {
    void pumpGas(double amountInLiters) throws NotEnoughGasException;

    void addGasPump(GasPump gasPump);
}
