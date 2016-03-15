/*

This file is part of OTExtentions.

OTExtentions is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License (AGPL)
v3.0 as published by the Free Software Foundation.

OTExtentions is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU Affero General Public License (AGPL) v3.0 for more details.

You should have received a copy of the  GNU Affero General Public
License (AGPL) v3.0 along with OTExtentions. If not, see
<http://www.gnu.org/licenses/agpl-3.0.txt>.


=====================================

    Author: Alexandros Mittos
    Year:   2016

=====================================

*/

package com.ote;

import java.util.concurrent.TimeUnit;

public class Timer {

    long starts;

    private Timer() {
        reset();
    }

    public static Timer start() {
        return new Timer();
    }

    public Timer reset() {
        starts = System.nanoTime();
        return this;
    }

    // Elapsed time in default format
    public long elapsed_time() {
        long ends = System.nanoTime();
        return ends - starts;
    }

    // Elapsed Time in custom format (i.e TimeUnit.SECONDS, TimeUnit.NANOSECONDS)
    public long elapsed_time(TimeUnit unit) {
        return unit.convert(elapsed_time(), TimeUnit.NANOSECONDS);
    }

    public long nanoToMillis() {
        return (elapsed_time() / 1000) / 1000;
    }

    public long nanoToSeconds() {
        return ((elapsed_time() / 1000) / 1000) / 1000;
    }

    // Elapsed time in minutes and seconds
    public String toMinuteSeconds() {
        return String.format("%d min, %d sec", elapsed_time(TimeUnit.MINUTES), elapsed_time(TimeUnit.SECONDS) - elapsed_time(TimeUnit.MINUTES));
    }


}