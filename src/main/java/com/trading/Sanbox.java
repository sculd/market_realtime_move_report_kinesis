package com.trading;

import com.binance.connector.client.impl.SpotClientImpl;
import com.binance.connector.client.exceptions.BinanceClientException;
import com.binance.connector.client.exceptions.BinanceConnectorException;
import com.marketapi.binance.response.QueryCrossMarginAccountDetails;
import com.marketapi.binance.response.SystemStatus;
import com.marketapi.binance.response.AccountSnapshot;
import com.marketapi.binance.response.ExchangeInformation;
import com.marketsignal.timeseries.analysis.Analyses;
import com.trading.state.*;
import com.tradingbinance.state.BinanceEnter;
import com.tradingbinance.state.BinanceEnterInProgress;
import com.tradingbinance.state.BinanceExit;
import com.tradingbinance.state.BinanceExitInProgress;
import com.tradingbinance.state.BinanceUtil;
import com.google.gson.Gson;

import java.util.LinkedHashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sanbox {
    private static final Logger logger = LoggerFactory.getLogger(Sanbox.class);

    public static void main(String... args) {
        SpotClientImpl client = new SpotClientImpl(System.getenv("BINANCE_API_KEY"), System.getenv("BINANCE_API_SECRET"));
        LinkedHashMap<String,Object> parameters;
        String result;

        ExchangeInformation exchangeInformation = BinanceUtil.getExchangeInfo("ADAUSDT");
        logger.info("exchangeInformation: {}", exchangeInformation);

        // binance enter and exit
        String symbol = "ADAUSDT";
        String market = "binance";
        BinanceEnter binanceEnter = BinanceEnter.builder()
                .symbol(symbol)
                .targetVolume(20.2)
                .positionSideType(Common.PositionSideType.SHORT)
                .build();

        Enter.ExecuteResult enterExecuteResult =  binanceEnter.execute(
                Common.PriceSnapshot.builder().price(3.5).epochSeconds(java.time.Instant.now().getEpochSecond()).build(),
                new Analyses());
        logger.info("enterExecuteResult: {}", enterExecuteResult);

        BinanceEnterInProgress binanceEnterInProgress = BinanceEnterInProgress.builder()
                .market(market).symbol(symbol).orderID(enterExecuteResult.orderID)
                .exitPlanInitParameter(ExitPlan.ExitPlanInitParameter.builder()
                        .takeProfitPlanInitParameter(TakeProfitPlan.TakeProfitPlanInitParameter.builder().build())
                        .stopLossPlanInitParameter(StopLossPlan.StopLossPlanInitParameter.builder().build())
                        .timeoutPlanInitParameter(TimeoutPlan.TimeoutPlanInitParameter.builder().build())
                        .build())
                .build();
        EnterInProgress.EnterInProgressStatus enterInProgressStatus = null;
        while (true)
        {
            enterInProgressStatus = binanceEnterInProgress.getProgressStatus(Common.PositionSideType.SHORT, Common.PriceSnapshot.builder().build(), new Analyses());
            logger.info("enterInProgressStatus: {}", enterInProgressStatus);
            if (enterInProgressStatus.status == EnterInProgress.EnterInProgressStatus.Status.ORDER_COMPLETE) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        BinanceExit binanceExit = BinanceExit.builder()
                .position(enterInProgressStatus.position)
                .targetPrice(3.5)
                .analysesUponExit(new Analyses())
                .build();

        Exit.ExecuteResult exitExecuteResult = binanceExit.execute();
        logger.info("exitExecuteResult: {}", exitExecuteResult);

        BinanceExitInProgress binanceExitInProgress = BinanceExitInProgress.builder().market(market).symbol(symbol).orderID(exitExecuteResult.orderID).build();
        ExitInProgress.ExitInProgressStatus exitInProgressStatus = null;
        while (true)
        {
            exitInProgressStatus = binanceExitInProgress.getProgressStatus(enterInProgressStatus.position.positionSideType);
            logger.info("exitInProgressStatus: {}", exitInProgressStatus);
            if (exitInProgressStatus.status == ExitInProgress.ExitInProgressStatus.Status.ORDER_COMPLETE) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // testNewOrder
        parameters = new LinkedHashMap<String,Object>();
        parameters.put("symbol","BTCUSDT");
        parameters.put("side", "SELL");
        parameters.put("type", "LIMIT");
        parameters.put("timeInForce", "GTC");
        parameters.put("quantity", 0.01);
        parameters.put("price", 9500);
        result = client.createTrade().testNewOrder(parameters);
        System.out.println(result);

        // test buy invalid pair
        try {
            parameters.put("symbol","dummy");
            result = client.createTrade().testNewOrder(parameters);
            logger.info(result);
        }
        catch (BinanceConnectorException e) {
            logger.error("fullErrMessage: {}", e.getMessage(), e);
        }
        catch (BinanceClientException e) {
            logger.error("fullErrMessage: {} \nerrMessage: {} \nerrCode: {} \nHTTPStatusCode: {}",
                    e.getMessage(), e.getErrMsg(), e.getErrorCode(), e.getHttpStatusCode(), e);
        }

        // test account, system status
        Gson gson = new Gson(); // Or use new GsonBuilder().create();
        parameters = new LinkedHashMap<String,Object>();
        parameters.put("type", "SPOT");
        result = client.createWallet().accountSnapshot(parameters);
        System.out.println(result);
        AccountSnapshot accountSnapshot = gson.fromJson(result, AccountSnapshot.class);
        System.out.println(accountSnapshot);

        result = client.createWallet().systemStatus();
        System.out.println(result);
        SystemStatus systemStatus = gson.fromJson(result, SystemStatus.class);
        System.out.println(systemStatus);

        // test borrow margin
        parameters.put("asset","XRP");
        parameters.put("amount", 5);
        result = client.createMargin().borrow(parameters);
        logger.info(result);

        // test repay borrowed
        try {
            parameters.put("asset","XRP");
            parameters.put("amount", 11);
            result = client.createMargin().repay(parameters);
            logger.info(result);
        }
        catch (BinanceClientException e) {
            logger.error("fullErrMessage: {} \nerrMessage: {} \nerrCode: {} \nHTTPStatusCode: {}",
                    e.getMessage(), e.getErrMsg(), e.getErrorCode(), e.getHttpStatusCode(), e);
        }

        // test short
        try {
            parameters.put("symbol","BTCUSDT");
            parameters.put("side", "SELL");
            parameters.put("type", "LIMIT");
            parameters.put("timeInForce", "GTC");
            parameters.put("quantity", 0.01);
            parameters.put("price", 9500);
            result = client.createMargin().newOrder(parameters);
            logger.info(result);
        }
        catch (BinanceConnectorException e) {
            logger.error("fullErrMessage: {}", e.getMessage(), e);
        }
        catch (BinanceClientException e) {
            logger.error("fullErrMessage: {} \nerrMessage: {} \nerrCode: {} \nHTTPStatusCode: {}",
                    e.getMessage(), e.getErrMsg(), e.getErrorCode(), e.getHttpStatusCode(), e);
        }

        // test margin account detail
        parameters.clear();
        result = BinanceUtil.client.createMargin().account(parameters);
        QueryCrossMarginAccountDetails marginAccountDetail = gson.fromJson(result, QueryCrossMarginAccountDetails.class);
        logger.info(marginAccountDetail.toString());

        System.out.println("done");
    }
}
