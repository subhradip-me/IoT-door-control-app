package com.fasla.doorcontrol.features.bluetooth.domain.usecase;

import com.fasla.doorcontrol.features.bluetooth.domain.repository.BluetoothRepository;

/**
 * SendDataUseCase — sends a text command to the connected ESP32.
 *
 * Use CommandProtocol constants for the command string:
 *   sendDataUseCase.execute(CommandProtocol.CMD_OPEN);
 *   sendDataUseCase.execute(CommandProtocol.CMD_CLOSE);
 */
public class SendDataUseCase {

    private final BluetoothRepository repository;

    public SendDataUseCase(BluetoothRepository repository) {
        this.repository = repository;
    }

    /**
     * @param command Newline-terminated command string (use CommandProtocol constants)
     */
    public void execute(String command) {
        repository.sendCommand(command);
    }
}
