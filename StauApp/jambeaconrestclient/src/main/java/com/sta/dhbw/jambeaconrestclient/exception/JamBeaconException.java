package com.sta.dhbw.jambeaconrestclient.exception;

public class JamBeaconException extends Exception
{
    private static final long SERIAL_VERSION_UID = 20150508L;

    public JamBeaconException()
    {
        super();
    }

    public JamBeaconException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public JamBeaconException(String message)
    {
        super(message);
    }

    public JamBeaconException(Throwable cause)
    {
        super(cause);
    }
}
