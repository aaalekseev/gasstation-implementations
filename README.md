### Pre Analysis and Architecture

Task definition contains several significant points, which should be taken into consideration at the implementation stage:
  - GasPump class is`t thread-safe. Individual worker should be provided foreach instance.

  - GasPump::pumpGas method is very slow. This situation should not affect GasStation sale process - pump process should take place in background and not affect other sales.

  - There are different gas types. Each gas type should be supplied independently and not affect sales of the other gas types.

  - GasStation:getGasPumps method should return a copy of the station gas pumps, because of *"Modifying the resulting collection should not affect this gas station"*

  - There are a lot of volatile parameters of a gas station (Revenue, NumberOfSales, Gas prices and etc.). Thread-safety should be guaranteed for them in the effective manner.

  - **Important!** Different strategies of the gas pump selection may take place in the real life. And the required strategy could significantly influence an internal structure of a gas station. We know nothing about gas station purchases (frequency, volumes and etc.) and sale requirements, because of this we could not select one best strategy.
    All the pump selection logic is strongly related to the particular gas type. So, it would be correctly to encapsulate all the strategy logic in different pump managers (individual pump manager foreach strategy). The most obvious strategies are:
    * Steady-mining non-blocking strategy: the gas station should select the pump with the highest remaining amount. It allows to align gas balances on the station pumps. If no pump with essential gas amount is available at the moment, NotEnoughGasException would be thrown. Steady non-blocking strategy would be implemented in ***net.bigpoint.assessment.gasstation.impl.managers.SteadyPumpManager***
    * Steady-mining blocking strategy: the gas station should select the pump with the highest remaining amount. If no pump with essential gas amount available at the moment, but theoretically exists, it will schedule purchase and wait. Steady blocking strategy would be implemented in ***net.bigpoint.assessment.gasstation.impl.managers.SteadyBlockingPumpManager***
    * "Max-sales" strategy: the gas station should select the pump with the lowest, but sufficient for the current request remaining amount. Such strategy allows to process as much as possible requests **with instantaneous response and purchases obeying uniform distribution**.
    * Real-time bidding strategy: it is a difficult strategy, which does't assume instantaneous sale, but some bidding pool is prepared and the most profitable for the gas station request is processed. Such a strategy requires deeper insight in the sales process for the ad hoc implementation. It is also not suitable for the current synchronous GasStation::buyGas method and we would not concern it, but theoretically is very interesting direction.

