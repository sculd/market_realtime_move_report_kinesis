package com.marketapi.binance.response;

import com.google.common.base.MoreObjects;

import java.util.List;
import java.util.stream.Collectors;

public class QueryCrossMarginAccountDetails {
    public boolean borrowEnabled;
    public String marginLevel;
    public String totalAssetOfBtc;
    public String totalLiabilityOfBtc;
    public String totalNetAssetOfBtc;
    public boolean tradeEnabled;
    public boolean transferEnabled;

    public class UserAsset {
        public String asset;
        public String borrowed;
        public String free;
        public String interest;
        public String locked;
        public String netAsset;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(QueryCrossMarginAccountDetails.class)
                    .add("asset", asset)
                    .add("borrowed", borrowed)
                    .add("free", free)
                    .add("interest", interest)
                    .add("locked", locked)
                    .add("netAsset", netAsset)
                    .toString();
        }
    }
    public List<UserAsset> userAssets;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(QueryCrossMarginAccountDetails.class)
                .add("borrowEnabled", borrowEnabled)
                .add("marginLevel", marginLevel)
                .add("totalAssetOfBtc", totalAssetOfBtc)
                .add("totalLiabilityOfBtc", totalLiabilityOfBtc)
                .add("totalNetAssetOfBtc", totalNetAssetOfBtc)
                .add("tradeEnabled", tradeEnabled)
                .add("transferEnabled", transferEnabled)
                .add("userAssets", String.join(", ", userAssets.stream().map(a -> a.toString()).collect(Collectors.toList())))
                .toString();
    }

    public double getBorrowedAmount(String asset) {
        for (UserAsset userAsset : userAssets) {
            if (asset.equals(userAsset.asset)) {
                return Double.valueOf(userAsset.borrowed);
            }
        }
        return 0.0;
    }

    public double getFreeAmount(String asset) {
        for (UserAsset userAsset : userAssets) {
            if (asset.equals(userAsset.asset)) {
                return Double.valueOf(userAsset.free);
            }
        }
        return 0.0;
    }
}
