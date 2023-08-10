package com.fiercemanul.blackholestorage.channel;

public class NullChannel extends ServerChannel {

    public static final NullChannel INSTANCE = new NullChannel();

    private NullChannel() {
        super();
        super.setName("RemovedChannel");
    }

    @Override
    public boolean isRemoved() {
        return true;
    }
}
