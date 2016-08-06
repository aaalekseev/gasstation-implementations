package net.bigpoint.assessment.gasstation.impl;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasStation;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;
import net.bigpoint.assessment.gasstation.impl.managers.PumpManager;
import net.bigpoint.assessment.gasstation.impl.managers.PumpManagerEnum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;

/**
 * Created by aaalekseev on 06-Aug-16.
 */
public class GasStationImpl implements GasStation {
    /**
     * Thread-safe map for Prices
     */
    private final Map<GasType, Double> gasPricesMap = new ConcurrentHashMap<GasType, Double>();
    /**
     * Thread-safe map for PumpManagers
     */
    private final Map<GasType, PumpManager> pumpManagersMap = new ConcurrentHashMap<GasType, PumpManager>();
    /**
     * Thread-safe List of all the pums (
     */
    private final Collection<GasPump> allGasPumps = Collections.synchronizedList(new ArrayList<>());
    /**
     * Atomic counters for volatile station attrivutes
     */
    private final DoubleAdder revenueSum = new DoubleAdder();
    private final AtomicInteger salesCounter = new AtomicInteger(0);
    private final AtomicInteger cancellationsNoGasCounter = new AtomicInteger(0);
    private final AtomicInteger cancellationsTooExpensiveCounter = new AtomicInteger(0);

    /**
     * PumpManager encapsulates strategy for the pump selection process.
     */
    private final Class<PumpManager> pumpManagerClass;

    /**
     * Constructors
     * @param pumpManagerStrategy - required for the gas station creation
     */
    public GasStationImpl(PumpManagerEnum pumpManagerStrategy) throws ClassNotFoundException {
        this.pumpManagerClass = (Class<PumpManager>)Class.forName(pumpManagerStrategy.toString());
    }
    public GasStationImpl(PumpManagerEnum pumpManagerStrategy, Map<GasType, Double> gasPrices, Collection<GasPump> initialPumps) throws ClassNotFoundException {
        this.pumpManagerClass = (Class<PumpManager>)Class.forName(pumpManagerStrategy.toString());
        gasPricesMap.putAll(gasPrices);
        initialPumps.forEach(this::addGasPump);
    }

    public void addGasPump(GasPump gasPump) {
        // Add a new Manager if absent
        try {
            pumpManagersMap.putIfAbsent(gasPump.getGasType(), pumpManagerClass.newInstance());
        } catch (IllegalAccessException|InstantiationException e) {
            e.printStackTrace(); // Ignored
        }
        // Add to allPumps Collection
        allGasPumps.add(gasPump);
        pumpManagersMap.get(gasPump.getGasType()).addGasPump(gasPump);
    }

    public Collection<GasPump> getGasPumps() {
        return allGasPumps.stream().map(x -> new GasPump(x.getGasType(), x.getRemainingAmount())).collect(Collectors.toList());
    }

    public double buyGas(GasType gasType, double amountInLiters, double maxPricePerLiter) throws NotEnoughGasException, GasTooExpensiveException {
        if (gasType == null || !gasPricesMap.containsKey(gasType) || !pumpManagersMap.containsKey(gasType))
            throw new IllegalArgumentException("This gas type is not provided by the Gas Station");

        final double currPricePerLiter = getPrice(gasType);
        // Check if gas is too expensive
        if (currPricePerLiter > maxPricePerLiter) {
            cancellationsTooExpensiveCounter.getAndIncrement();
            throw new GasTooExpensiveException();
        }

        // Try to pump gas
        try {
            final PumpManager pumpManager = pumpManagersMap.get(gasType);
            // Wait in the same thread
            pumpManager.pumpGas(amountInLiters);

            salesCounter.getAndIncrement();
            final double purchasePrice = currPricePerLiter * amountInLiters;
            revenueSum.add(purchasePrice);
            return purchasePrice;
        } catch (NotEnoughGasException e) {
            cancellationsNoGasCounter.getAndIncrement();
            throw e;
        }
    }

    public double getRevenue() {
        return revenueSum.sum();
    }

    public int getNumberOfSales() {
        return salesCounter.get();
    }

    public int getNumberOfCancellationsNoGas() {
        return cancellationsNoGasCounter.get();
    }

    public int getNumberOfCancellationsTooExpensive() {
        return cancellationsTooExpensiveCounter.get();
    }

    public double getPrice(GasType gasType) {
        if (gasType == null || !gasPricesMap.containsKey(gasType))
            throw new IllegalArgumentException("Inappropriate gas type");

        return gasPricesMap.get(gasType);
    }

    public void setPrice(GasType gasType, double price) {
        gasPricesMap.put(gasType, price);
    }
}
