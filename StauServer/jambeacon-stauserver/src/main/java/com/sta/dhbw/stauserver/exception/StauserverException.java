package com.sta.dhbw.stauserver.exception;

public class StauserverException extends Exception
{
    public StauserverException()
    {
        super();
    }

    public StauserverException(String message)
    {
        super(message);
    }

    public StauserverException(Throwable cause)
    {
        super(cause);
    }

    public StauserverException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
