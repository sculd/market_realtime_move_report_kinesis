package com.trading.performance;

import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

public class ParameterScanCommon {
    @Builder
    public static class ScanGridDoubleParam {
        public double startDouble;
        public double endDouble; // end is inclusive
        public double stepDouble;

        public List<Double> getValues() {
            List<Double> ret = new ArrayList<>();
            double value = startDouble;
            while (value <= endDouble) {
                ret.add(value);
                value += stepDouble;
            }
            return ret;
        }
    }

    @Builder
    public static class ScanGridIntParam {
        public int startInt;
        public int endInt; // end is inclusive
        public int stepInt;

        public List<Integer> getValues() {
            List<Integer> ret = new ArrayList<>();
            int value = startInt;
            while (value <= endInt) {
                ret.add(value);
                value += stepInt;
            }
            return ret;
        }
    }
}
