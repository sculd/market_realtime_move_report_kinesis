package com.marketsignal.orderbook.analysis;

import com.marketsignal.orderbook.Orderbook;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OrderFlowTest {
    final double equalDelta = 0.001;

    @Test
    public void testGetBidOrderFlowNoChange() {
        Orderbook former = new Orderbook("dummy_market", "dummy_symbol", 0);
        former.asks.quotes.add(Orderbook.Quote.builder().price(110).volume(10).build());
        former.bids.quotes.add(Orderbook.Quote.builder().price(100).volume(20).build());

        Orderbook later = new Orderbook("dummy_market", "dummy_symbol", 10);
        later.asks.quotes.add(Orderbook.Quote.builder().price(110).volume(10).build());
        later.bids.quotes.add(Orderbook.Quote.builder().price(100).volume(20).build());

        assertEquals(0, OrderFlow.getBidOrderFlow(former, later), equalDelta);
    }

    @Test
    public void testGetBidOrderFlowNoPriceMoveVolumeIncrease() {
        Orderbook former = new Orderbook("dummy_market", "dummy_symbol", 0);
        former.asks.quotes.add(Orderbook.Quote.builder().price(110).volume(10).build());
        former.bids.quotes.add(Orderbook.Quote.builder().price(100).volume(20).build());

        Orderbook later = new Orderbook("dummy_market", "dummy_symbol", 10);
        later.asks.quotes.add(Orderbook.Quote.builder().price(110).volume(10).build());
        later.bids.quotes.add(Orderbook.Quote.builder().price(100).volume(30).build());

        assertEquals(10, OrderFlow.getBidOrderFlow(former, later), equalDelta);
    }

    @Test
    public void testGetBidOrderFlowNoPriceMoveVolumeDecrease() {
        Orderbook former = new Orderbook("dummy_market", "dummy_symbol", 0);
        former.asks.quotes.add(Orderbook.Quote.builder().price(110).volume(10).build());
        former.bids.quotes.add(Orderbook.Quote.builder().price(100).volume(20).build());

        Orderbook later = new Orderbook("dummy_market", "dummy_symbol", 10);
        later.asks.quotes.add(Orderbook.Quote.builder().price(110).volume(10).build());
        later.bids.quotes.add(Orderbook.Quote.builder().price(100).volume(10).build());

        assertEquals(-10, OrderFlow.getBidOrderFlow(former, later), equalDelta);
    }

    @Test
    public void testGetBidOrderFlowPriceIncrease() {
        Orderbook former = new Orderbook("dummy_market", "dummy_symbol", 0);
        former.asks.quotes.add(Orderbook.Quote.builder().price(110).volume(10).build());
        former.bids.quotes.add(Orderbook.Quote.builder().price(100).volume(20).build());

        Orderbook later = new Orderbook("dummy_market", "dummy_symbol", 10);
        later.asks.quotes.add(Orderbook.Quote.builder().price(110).volume(10).build());
        later.bids.quotes.add(Orderbook.Quote.builder().price(105).volume(30).build());

        assertEquals(30, OrderFlow.getBidOrderFlow(former, later), equalDelta);
    }

    @Test
    public void testGetBidOrderFlowPriceDecrease() {
        Orderbook former = new Orderbook("dummy_market", "dummy_symbol", 0);
        former.asks.quotes.add(Orderbook.Quote.builder().price(110).volume(10).build());
        former.bids.quotes.add(Orderbook.Quote.builder().price(100).volume(20).build());

        Orderbook later = new Orderbook("dummy_market", "dummy_symbol", 10);
        later.asks.quotes.add(Orderbook.Quote.builder().price(110).volume(10).build());
        later.bids.quotes.add(Orderbook.Quote.builder().price(90).volume(30).build());

        assertEquals(-20, OrderFlow.getBidOrderFlow(former, later), equalDelta);
    }

    @Test
    public void testGetBidOrderFlowAbsentFormer() {
        Orderbook former = new Orderbook("dummy_market", "dummy_symbol", 0);
        former.asks.quotes.add(Orderbook.Quote.builder().price(110).volume(10).build());

        Orderbook later = new Orderbook("dummy_market", "dummy_symbol", 10);
        later.asks.quotes.add(Orderbook.Quote.builder().price(110).volume(10).build());
        later.bids.quotes.add(Orderbook.Quote.builder().price(90).volume(20).build());

        assertEquals(20, OrderFlow.getBidOrderFlow(former, later), equalDelta);
    }

    @Test
    public void testGetBidOrderFlowAbsentLater() {
        Orderbook former = new Orderbook("dummy_market", "dummy_symbol", 0);
        former.asks.quotes.add(Orderbook.Quote.builder().price(110).volume(10).build());
        former.bids.quotes.add(Orderbook.Quote.builder().price(100).volume(20).build());

        Orderbook later = new Orderbook("dummy_market", "dummy_symbol", 10);
        later.asks.quotes.add(Orderbook.Quote.builder().price(110).volume(10).build());

        assertEquals(-20, OrderFlow.getBidOrderFlow(former, later), equalDelta);
    }

    @Test
    public void testGetAskOrderFlowNoChange() {
        Orderbook former = new Orderbook("dummy_market", "dummy_symbol", 0);
        former.asks.quotes.add(Orderbook.Quote.builder().price(110).volume(10).build());
        former.bids.quotes.add(Orderbook.Quote.builder().price(100).volume(20).build());

        Orderbook later = new Orderbook("dummy_market", "dummy_symbol", 10);
        later.asks.quotes.add(Orderbook.Quote.builder().price(110).volume(10).build());
        later.bids.quotes.add(Orderbook.Quote.builder().price(100).volume(20).build());

        assertEquals(0, OrderFlow.getAskOrderFlow(former, later), equalDelta);
    }

    @Test
    public void testGetAskOrderFlowNoPriceMoveVolumeIncrease() {
        Orderbook former = new Orderbook("dummy_market", "dummy_symbol", 0);
        former.asks.quotes.add(Orderbook.Quote.builder().price(110).volume(10).build());
        former.bids.quotes.add(Orderbook.Quote.builder().price(100).volume(20).build());

        Orderbook later = new Orderbook("dummy_market", "dummy_symbol", 10);
        later.asks.quotes.add(Orderbook.Quote.builder().price(110).volume(20).build());
        later.bids.quotes.add(Orderbook.Quote.builder().price(100).volume(20).build());

        assertEquals(10, OrderFlow.getAskOrderFlow(former, later), equalDelta);
    }

    @Test
    public void testGetAskOrderFlowNoPriceMoveVolumeDecrease() {
        Orderbook former = new Orderbook("dummy_market", "dummy_symbol", 0);
        former.asks.quotes.add(Orderbook.Quote.builder().price(110).volume(10).build());
        former.bids.quotes.add(Orderbook.Quote.builder().price(100).volume(20).build());

        Orderbook later = new Orderbook("dummy_market", "dummy_symbol", 10);
        later.asks.quotes.add(Orderbook.Quote.builder().price(110).volume(5).build());
        later.bids.quotes.add(Orderbook.Quote.builder().price(100).volume(20).build());

        assertEquals(-5, OrderFlow.getAskOrderFlow(former, later), equalDelta);
    }

    @Test
    public void testGetAskOrderFlowPriceIncrease() {
        Orderbook former = new Orderbook("dummy_market", "dummy_symbol", 0);
        former.asks.quotes.add(Orderbook.Quote.builder().price(110).volume(10).build());
        former.bids.quotes.add(Orderbook.Quote.builder().price(100).volume(20).build());

        Orderbook later = new Orderbook("dummy_market", "dummy_symbol", 10);
        later.asks.quotes.add(Orderbook.Quote.builder().price(120).volume(20).build());
        later.bids.quotes.add(Orderbook.Quote.builder().price(100).volume(20).build());

        assertEquals(-10, OrderFlow.getAskOrderFlow(former, later), equalDelta);
    }

    @Test
    public void testGetAskOrderFlowPriceDecrease() {
        Orderbook former = new Orderbook("dummy_market", "dummy_symbol", 0);
        former.asks.quotes.add(Orderbook.Quote.builder().price(110).volume(10).build());
        former.bids.quotes.add(Orderbook.Quote.builder().price(100).volume(20).build());

        Orderbook later = new Orderbook("dummy_market", "dummy_symbol", 10);
        later.asks.quotes.add(Orderbook.Quote.builder().price(100).volume(20).build());
        later.bids.quotes.add(Orderbook.Quote.builder().price(100).volume(20).build());

        assertEquals(20, OrderFlow.getAskOrderFlow(former, later), equalDelta);
    }

    @Test
    public void testGetAskOrderFlowAbsentFormer() {
        Orderbook former = new Orderbook("dummy_market", "dummy_symbol", 0);
        former.bids.quotes.add(Orderbook.Quote.builder().price(100).volume(20).build());

        Orderbook later = new Orderbook("dummy_market", "dummy_symbol", 10);
        later.asks.quotes.add(Orderbook.Quote.builder().price(120).volume(10).build());
        later.bids.quotes.add(Orderbook.Quote.builder().price(90).volume(20).build());

        assertEquals(10, OrderFlow.getAskOrderFlow(former, later), equalDelta);
    }

    @Test
    public void testGetAskOrderFlowAbsentLater() {
        Orderbook former = new Orderbook("dummy_market", "dummy_symbol", 0);
        former.asks.quotes.add(Orderbook.Quote.builder().price(120).volume(10).build());
        former.bids.quotes.add(Orderbook.Quote.builder().price(100).volume(20).build());

        Orderbook later = new Orderbook("dummy_market", "dummy_symbol", 10);
        later.bids.quotes.add(Orderbook.Quote.builder().price(100).volume(20).build());

        assertEquals(-10, OrderFlow.getAskOrderFlow(former, later), equalDelta);
    }
}
