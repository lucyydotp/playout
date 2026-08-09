package me.lucyydotp.playout.controller.env

/** Environment variables for the controller. */
public object EnvironmentVariables : VariablesObject("PLAYOUT") {

    /** The port to run the embedded server on. Only used in standalone mode. */
    public val standalonePort: UShort by "SERVER_PORT" default 8080u
}
