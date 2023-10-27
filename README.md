# BPMNE4IoT-to-BPMN2.0

A Converter that converts a BPMNE4IoT model into a standard BPMN 2.0 model.
Based on the models created in the BPMNE4IoT [Modeller](https://github.com/elmurd0r/bpmne4iot).

Note: Currently the IoT Boundary Event is not supported in the BPMNE4IoT modeller, hence it is not supported in the current version of the converter either (as of 20 September 2023).
The same applies to the IoT intermediate throw/catch events that have sensor/actuator/catch artifacts connected to them (events without associations are supported).

This prototype has been kept simple and does not support models that possess the following:
1. Tasks that have associations to more than 1 IoT actuator/sensor/catch artifacts (a task can have one actuator, one sensor and one catch artifact at the same time)
2. IoT actuator/sensor/catch artifacts that have more than 2 associations
3. sub-processes

## How to run

To run the prototype execute the "BPMNE4IoT-Converter.jar" file (Java Version: 16).

## License

MIT
