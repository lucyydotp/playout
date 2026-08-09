package me.lucyydotp.playout.controller.env

/** Environment variables for the controller. */
public object EnvironmentVariables : VariablesObject("PLAYOUT") {

    /** The port to run the embedded server on. Only used in standalone mode. */
    public val standalonePort: UShort by "SERVER_PORT" default 8080u

    /**
     * The port to host the AMCP media scanner API on. If not specified, the scanner API is not
     * available.
     */
    public val amcpScannerPort: UShort? by "AMCP_SCANNER_API_PORT" default 8000u

    /** The hostname to use when advertising the media scanner API in fetched configs. */
    public val amcpScannerAdvertisedHost: String? by "AMCP_SCANNER_API_ADVERTISED_HOST"

    /**
     * The port to use when advertising the media scanner API in fetched configs. Defaults to
     * [amcpScannerPort] if not specified.
     */
    public val amcpScannerAdvertisedPort: UShort? by
        "AMCP_SCANNER_API_ADVERTISED_PORT" default amcpScannerPort
}
