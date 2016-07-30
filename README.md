### Pre Analysis and Architecture

Task definition contains several significant points, which should be taken into consideration at the implementation stage:
  - GasPump class is`t thread-safe. Individual worker should be provide foreach instance.
  - GasPump::pumpGas method is very slow. This situation should not affect GasStation sale process - pump process should take place in background, after the sale has happen.
  - There are different gas types. Each gas type should be supplied independently and not affect sales of the other gas types.
  - Different strategies of the gas pump selection may take place in the real life. And the required strategy could significantly influence internal structure of a gas station. In theory it is possible to create universal gas station, which taking selection-strategy as an input parameter, but it would not be the best decision in this case. The most obvious strategies are:
    * Steady-mining strategy: the gas station should select the pump with the highest remaining amount. It allows to align gas balances on the station pumps. Steady strategy would be implemented in ***net.bigpoint.assessment.gasstation.impl.SteadyGasStation***
    * Max-sales strategy: the gas station should select the pump with the lowest, but sufficient for the current request remaining amount. Such strategy allows to process as much as possible requests **in the moment** (here we consider instantaneous response to the request).
    * Real-time bidding strategy: it is a difficult strategy, which does't assume instantaneous sale, but some bidding pool is prepared and the most profitable for the gas station request is processed. Such a strategy requires deeper insight for the ad hoc implementation. It is also not suitable for the current synchronous GasStation::buyGas method, but theoretically is very interesting.
  - GasStation:getGasPumps method should return a copy of the station gas pumps, because of *"Modifying the resulting collection should not affect this gas station""*
  - There are a lot of volatile parameters of a gas station (Revenue, NumberOfSales, Gas prices and etc.). Thread-safety should be guaranteed for them in the effective manner.

