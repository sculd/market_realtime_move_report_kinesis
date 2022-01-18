package com.trading.state;

public class Common {

    public enum ActionType {
        ENTER,
        EXIT,
        IDLE;
    }

    public enum ChangeType {
        JUMP,
        DROP;
    }

    public enum PositionType {
        LONG,
        SHORT;
    }

    public enum SeekToggleType {
        SEEK,
        IDLE;
    }

    public enum PositionToggleType {
        IN_POSITION,
        IDLE;
    }

    public enum ExitToggleType {
        EXITED,
        IDLE;
    }
}
