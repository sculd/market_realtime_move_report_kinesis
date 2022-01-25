package com.trading.state;

import com.trading.state.transition.StateTransition;

public class Seek {
    public Common.ChangeType changeType;

    public double referencePrice;

    public double seekPrice;

    public double seekChange;

    public void init(Common.ChangeType changeType, double referencePrice, double seekChange) {
        this.changeType = changeType;
        this.referencePrice = referencePrice;
        this.setSeekChange(seekChange);
    }

    public void setSeekChange(double seekChange) {
        this.seekChange = seekChange;
        seekPrice = referencePrice * (1.0 + seekChange);
    }

    public void updateReferencePrice(double referencePrice) {
        this.referencePrice = referencePrice;
        setSeekChange(seekChange);
    }

    public boolean getIfTriggered(double price) {
        switch (changeType) {
            case JUMP:
                if (price >= seekPrice) {
                    return true;
                }
                break;
            case DROP:
                if (price <= seekPrice) {
                    return true;
                }
                break;
        }
        return false;
    }
}
