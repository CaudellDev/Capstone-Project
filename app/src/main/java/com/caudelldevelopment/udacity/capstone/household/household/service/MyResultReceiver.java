package com.caudelldevelopment.udacity.capstone.household.household.service;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class MyResultReceiver extends ResultReceiver {

    private Receiver mReceiver;

    public MyResultReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        mReceiver.onReceiveResult(resultCode, resultData);
    }

    public interface Receiver {
        void onReceiveResult(int resultCode, Bundle resultData);
    }
}
