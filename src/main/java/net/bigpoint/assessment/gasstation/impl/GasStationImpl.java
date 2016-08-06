package net.bigpoint.assessment.gasstation.impl;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasStation;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

import java.util.Collection;

/**
 * Created by aaalekseev on 06-Aug-16.
 */
public class GasStationImpl implements GasStation {
    public void addGasPump(GasPump pump) {

    }

    public Collection<GasPump> getGasPumps() {
        return null;
    }

    public double buyGas(GasType type, double amountInLiters, double maxPricePerLiter) throws NotEnoughGasException, GasTooExpensiveException {
        return 0;
    }

    public double getRevenue() {
        return 0;
    }

    public int getNumberOfSales() {
        return 0;
    }

    public int getNumberOfCancellationsNoGas() {
        return 0;
    }

    public int getNumberOfCancellationsTooExpensive() {
        return 0;
    }

    public double getPrice(GasType type) {
        return 0;
    }

    public void setPrice(GasType type, double price) {

    }
}
