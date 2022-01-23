package com.trading.state;

import com.trading.state.transition.StateTransition;

public class Seek {
    public Common.ChangeType changeType;

    public double referencePrice;

    public double seekPrice;

    public void init(Common.ChangeType changeType, double referencePrice, double seekChange) {
        this.changeType = changeType;
        this.referencePrice = referencePrice;
        this.setSeekChange(seekChange);
    }

    public double getSeekChange() {
        return (seekPrice - referencePrice) / referencePrice - 1.0;
    }

    public void setSeekChange(double seekChange) {
        seekPrice = referencePrice * (1.0 + seekChange);
    }

    public void updateReferencePrice(double referencePrice) {
        double seekChange = getSeekChange();
        this.referencePrice = referencePrice;
        setSeekChange(seekChange);
    }

    public boolean getIfTriggered(double price) {
        switch (changeType) {
            case JUMP:
                if (seekPrice >= price) {
                    return true;
                }
                break;
            case DROP:
                if (seekPrice <= price) {
                    return true;
                }
                break;
        }
        return false;
    }
}
