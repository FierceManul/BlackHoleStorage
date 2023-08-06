package com.fiercemanul.blackholestorage.channel;

public class NullChannel extends ServerChannel {

    public NullChannel() {
        super();
        super.setName("RemovedChannel");
    }

    @Override
    public boolean isRemoved() {
        return true;
    }
}
