package com.fasla.doorcontrol.core.bluetooth;

/**
 * CommandProtocol — defines all commands sent from Android → ESP32
 * and all responses received from ESP32 → Android.
 *
 * Protocol: newline-terminated UTF-8 text over Classic Bluetooth SPP.
 * ESP32 side reads with: SerialBT.readStringUntil('\n')
 */
public final class CommandProtocol {

    private CommandProtocol() { /* no instances */ }

    // ── Commands (Android → ESP32) ────────────────────────────────────────

    /** Unlock / open the door */
    public static final String CMD_OPEN   = "OPEN\n";

    /** Lock / close the door */
    public static final String CMD_CLOSE  = "CLOSE\n";

    /** Request current door state */
    public static final String CMD_STATUS = "STATUS\n";

    /** Keep-alive ping — expects PONG back */
    public static final String CMD_PING   = "PING\n";

    // ── Responses (ESP32 → Android) ───────────────────────────────────────

    /** Command was executed successfully */
    public static final String RESP_OK          = "OK";

    /** Door is currently open / unlocked */
    public static final String RESP_DOOR_OPEN   = "DOOR_OPEN";

    /** Door is currently closed / locked */
    public static final String RESP_DOOR_CLOSED = "DOOR_CLOSED";

    /** Response to PING */
    public static final String RESP_PONG        = "PONG";

    /** Command failed on device side */
    public static final String RESP_ERROR       = "ERROR";
}
