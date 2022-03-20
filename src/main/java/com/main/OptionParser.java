package com.main;

import java.util.ListIterator;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;

public class OptionParser extends GnuParser {
    private boolean ignoreUnrecognizedOption;

    public OptionParser(final boolean ignoreUnrecognizedOption) {
        this.ignoreUnrecognizedOption = ignoreUnrecognizedOption;
    }

    @Override
    protected void processOption(final String arg, final ListIterator iter) throws ParseException {
        boolean hasOption = getOptions().hasOption(arg);
        if (hasOption || !ignoreUnrecognizedOption) {
            super.processOption(arg, iter);
        }
    }
}
