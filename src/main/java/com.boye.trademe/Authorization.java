package com.boye.trademe;

/**
 * Created by boy on 11/12/15.
 */
public class Authorization {
    private static Authorization instance;

    private Authorization(){

    }

    private synchronized Authorization getInstance(){
        if(instance==null){
            instance = new Authorization();
        }
        return instance;
    }
}
