package com.marketapi.binance.response;

import com.google.common.base.MoreObjects;
import java.util.List;

public class AccountSnapshot {
    public int status;
    public String msg;

    public class Balance {
        public String asset;
        public String free;
        public String locked;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(Balance.class)
                    .add("asset", asset)
                    .add("free", free)
                    .add("locked", locked)
                    .toString();
        }
    }

    public class SnapshotData {
        public String totalAssetOfBtc;
        public List<Balance> balances;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(SnapshotData.class)
                    .add("totalAssetOfBtc", totalAssetOfBtc)
                    .add("balances", balances)
                    .toString();
        }
    }

    public class SnapshotVo {
        public String type;
        public long updateTime;
        public SnapshotData data;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(SnapshotVo.class)
                    .add("type", type)
                    .add("updateTime", updateTime)
                    .add("data", data)
                    .toString();
        }
    }
    public List<SnapshotVo> snapshotVos;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(AccountSnapshot.class)
                .add("status", status)
                .add("msg", msg)
                .add("snapshotVos", snapshotVos)
                .toString();
    }
}
