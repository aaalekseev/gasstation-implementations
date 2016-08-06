package net.bigpoint.assessment.gasstation.impl.managers;

/**
 * Created by aaalekseev on 06-Aug-16.
 * Encapsulates all the implemented strategies for a gas station.
 * Contains full classloader paths for the runtime instantiation
 */
public enum PumpManagerEnum {
    SteadyPumpManager("net.bigpoint.assessment.gasstation.impl.managers.SteadyPumpManager"),
    SteadyBlockingPumpManager("net.bigpoint.assessment.gasstation.impl.managers.SteadyBlockingPumpManager");

    private final String text;
    PumpManagerEnum(final String text) {
        this.text = text;
    }
    @Override
    public String toString() {
        return text;
    }
}
