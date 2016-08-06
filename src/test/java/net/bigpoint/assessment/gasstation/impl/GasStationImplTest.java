package net.bigpoint.assessment.gasstation.impl;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasStation;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;
import net.bigpoint.assessment.gasstation.impl.managers.PumpManagerEnum;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized.Parameter;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Created by aaalekseev on 06-Aug-16.
 */
@RunWith(Parameterized.class)
public class GasStationImplTest {
    private final double DELTA = 0.0001;
    private final double DIESEL_PRICE = 1.5;
    private final double TEST_PUMP_GAS_AMOUNT = 10;

    private GasStation gasStation;
    private int initialPumpCount;

    @Parameter
    public PumpManagerEnum pumpManagerStrategy;
    @Parameters()
    public static Iterable<PumpManagerEnum> data() {
        return Arrays.asList(PumpManagerEnum.SteadyPumpManager, PumpManagerEnum.SteadyBlockingPumpManager);
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        gasStation = new GasStationImpl(pumpManagerStrategy);

        // Add pumps
        gasStation.addGasPump(new GasPump(GasType.DIESEL, TEST_PUMP_GAS_AMOUNT));
        gasStation.addGasPump(new GasPump(GasType.DIESEL, TEST_PUMP_GAS_AMOUNT));
        gasStation.addGasPump(new GasPump(GasType.DIESEL, TEST_PUMP_GAS_AMOUNT));
        initialPumpCount = 3;

        // Set prices
        gasStation.setPrice(GasType.DIESEL, DIESEL_PRICE);
    }

    @Test
    public void testBuyGas() throws Exception {
        final double totalPrice = gasStation.buyGas(GasType.DIESEL, TEST_PUMP_GAS_AMOUNT, DIESEL_PRICE);

        assertEquals(TEST_PUMP_GAS_AMOUNT * DIESEL_PRICE, totalPrice, DELTA);
    }

    @Test
    public void testGetRevenue() throws NotEnoughGasException, GasTooExpensiveException {
        final double price = gasStation.buyGas(GasType.DIESEL, TEST_PUMP_GAS_AMOUNT, DIESEL_PRICE);
        assertEquals(price, gasStation.getRevenue(), DELTA);
    }

    @Test
    public void testGetNumberOfSales() throws NotEnoughGasException, GasTooExpensiveException {
        Assert.assertEquals(0, gasStation.getNumberOfSales());
        gasStation.buyGas(GasType.DIESEL, TEST_PUMP_GAS_AMOUNT, DIESEL_PRICE);
        Assert.assertEquals(1, gasStation.getNumberOfSales());
    }

    @Test
    public void testBuyFailTooExpensive() throws NotEnoughGasException, GasTooExpensiveException {
        expectedException.expect(GasTooExpensiveException.class);
        gasStation.buyGas(GasType.DIESEL, TEST_PUMP_GAS_AMOUNT, DIESEL_PRICE - 1);
        expectedException = ExpectedException.none();
    }

    @Test
    public void testBuyFailNotEnough() throws NotEnoughGasException, GasTooExpensiveException {
        expectedException.expect(NotEnoughGasException.class);
        gasStation.buyGas(GasType.DIESEL, 1000 * TEST_PUMP_GAS_AMOUNT, DIESEL_PRICE);
        expectedException = ExpectedException.none();
    }

    @Test
    public void testBuyFailNoSuchPump() throws NotEnoughGasException, GasTooExpensiveException {
        expectedException.expect(IllegalArgumentException.class);
        gasStation.buyGas(GasType.REGULAR, TEST_PUMP_GAS_AMOUNT, DIESEL_PRICE);
        expectedException = ExpectedException.none();
    }

    @Test
    public void testSetPrice() {
        gasStation.setPrice(GasType.DIESEL, 2 * DIESEL_PRICE);
        assertEquals(2 * DIESEL_PRICE, gasStation.getPrice(GasType.DIESEL), DELTA);
    }

    @Test
    public void testAddGetGasPumps() {
        Collection<GasPump> pumps = gasStation.getGasPumps();
        assertEquals(initialPumpCount, pumps.size());

        gasStation.addGasPump(new GasPump(GasType.DIESEL, TEST_PUMP_GAS_AMOUNT));
        pumps = gasStation.getGasPumps();
        assertEquals(initialPumpCount + 1, pumps.size());

        for (GasPump pump : pumps) {
            Assert.assertEquals(GasType.DIESEL, pump.getGasType());
            assertEquals(TEST_PUMP_GAS_AMOUNT, pump.getRemainingAmount(), DELTA);
        }
    }

    @Test
    public void testGetGasPumpsUnmodifiable() {
        Collection<GasPump> pumps = gasStation.getGasPumps();
        assertEquals(initialPumpCount, pumps.size());

        pumps.add(new GasPump(GasType.DIESEL, TEST_PUMP_GAS_AMOUNT));
        pumps = gasStation.getGasPumps();
        assertEquals(initialPumpCount, pumps.size());

        // Try pump gas from the returned collection
        for (GasPump pump : pumps)
            pump.pumpGas(1);

        // Source pumps should not be changed
        pumps = gasStation.getGasPumps();
        for (GasPump pump : pumps) {
            Assert.assertEquals(GasType.DIESEL, pump.getGasType());
            assertEquals(TEST_PUMP_GAS_AMOUNT, pump.getRemainingAmount(), DELTA);
        }
    }
}