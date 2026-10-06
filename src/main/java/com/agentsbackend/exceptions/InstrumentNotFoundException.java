package com.agentsbackend.exceptions;

import java.util.UUID;


public class InstrumentNotFoundException extends RuntimeException{
    
    public InstrumentNotFoundException(UUID instrumentId){
        super("Instrument was not found with Instrument Id: "+ instrumentId);
    }

}
