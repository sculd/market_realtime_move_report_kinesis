package com.trading.binancestate;

import com.marketsignal.timeseries.analysis.Analyses;
import com.trading.state.EnterInProgress;
import com.trading.state.Common;;
import com.trading.state.ExitPlan;
import com.trading.state.Position;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class BinanceEnterInProgress extends EnterInProgress {
    public EnterInProgress.EnterInProgressStatus getProgressStatus(Common.PriceSnapshot entryPriceSnapshot, Analyses analysesUponEnter) {
        Position position = Position.builder()
                .market(market)
                .symbol(symbol)
                .positionSideType(positionSideType)
                .entryPriceSnapshot(Common.PriceSnapshot.builder()
                        .price(targetPrice)
                        .epochSeconds(entryPriceSnapshot.epochSeconds)
                        .build())
                .volume(targetVolume)
                .analysesUponEnter(analysesUponEnter)
                .build();
        ExitPlan exitPlan = ExitPlan.builder()
                .market(market)
                .symbol(symbol)
                .exitPlanInitParameter(exitPlanInitParameter)
                .position(position)
                .build();
        exitPlan.init(position);
        return EnterInProgress.EnterInProgressStatus.builder()
                .exitPlan(exitPlan)
                .position(position)
                .status(EnterInProgress.EnterInProgressStatus.Status.ORDER_COMPLETE).build();
    }
}
