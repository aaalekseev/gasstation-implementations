package net.bigpoint.assessment.gasstation.impl;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasStation;
import net.bigpoint.assessment.gasstation.GasType;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Created by aaalekseev on 06-Aug-16.
 */
public class GasStationImplStressTest {
    private final double DELTA = 0.0001;

    @Test
    public void smokeTest() throws InterruptedException {
        final GasStation gasStation = new GasStationImpl();

        final double regularPrice = 1.1d;
        final double dieselPrice = 2.2d;
        final double superPrice = 3.3d;
        gasStation.setPrice(GasType.REGULAR, regularPrice);
        gasStation.setPrice(GasType.DIESEL, dieselPrice);
        gasStation.setPrice(GasType.SUPER, superPrice);

        final int pumpGasStartAmount = 10;
        final int pumpsOfEachType = 2;
        final int buyAmount = 1;
        final int buyIterations = pumpGasStartAmount * pumpsOfEachType / buyAmount;

        for (int i = 0; i < pumpsOfEachType; i++) {
            gasStation.addGasPump(new GasPump(GasType.REGULAR, pumpGasStartAmount));
            gasStation.addGasPump(new GasPump(GasType.DIESEL, pumpGasStartAmount));
            gasStation.addGasPump(new GasPump(GasType.SUPER, pumpGasStartAmount));
        }

        final ExecutorService buyerExecutor = Executors.newFixedThreadPool(50);

        final int numberOfTooExpensive = 10;
        for (int i = 0; i < numberOfTooExpensive; i++) {
            buyerExecutor.submit(() -> gasStation.buyGas(GasType.REGULAR, buyAmount, regularPrice - 0.1d));
            buyerExecutor.submit(() -> gasStation.buyGas(GasType.DIESEL, buyAmount, dieselPrice - 0.1d));
            buyerExecutor.submit(() -> gasStation.buyGas(GasType.SUPER, buyAmount, superPrice - 0.1d));
        }
        final int numberOfCancellation = 50;
        for (int i = 0; i < buyIterations + numberOfCancellation; i++) {
            buyerExecutor.submit(() -> gasStation.buyGas(GasType.REGULAR, buyAmount, regularPrice));
            buyerExecutor.submit(() -> gasStation.buyGas(GasType.DIESEL, buyAmount, dieselPrice));
            buyerExecutor.submit(() -> gasStation.buyGas(GasType.SUPER, buyAmount, superPrice));
            // Wait till previous purchase will finish
            Thread.sleep(110 * buyAmount);
        }
        buyerExecutor.shutdown();
        buyerExecutor.awaitTermination(10, TimeUnit.MINUTES);

        assertEquals(3 * buyIterations, gasStation.getNumberOfSales());
        assertEquals(buyAmount * buyIterations * (regularPrice + dieselPrice + superPrice), gasStation.getRevenue(), DELTA);
        assertEquals(3 * numberOfCancellation, gasStation.getNumberOfCancellationsNoGas());
        assertEquals(3 * numberOfTooExpensive, gasStation.getNumberOfCancellationsTooExpensive());
    }
}
