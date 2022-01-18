package com.trading.state;

import com.trading.position.Exit;
import com.trading.position.Position;
import com.trading.performance.ClosedTrade;
import lombok.Builder;

@Builder
public class States {
    public String market;
    public String symbol;

    public Seek seek;
    public Action action;
    public Position position;
    public Exit exit;
    public ClosedTrade closedTrade;
}
