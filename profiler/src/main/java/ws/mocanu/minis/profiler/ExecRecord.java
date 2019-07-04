/*
Copyright 2019 Bogdan Mocanu (https://bogdan.mocanu.ws)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package ws.mocanu.minis.profiler;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A record of a how many times and for how long a particular code point was executed.
 */
class ExecRecord {

    /**
     * The number of times a code point was executed.
     */
    private AtomicInteger nrOfRuns = new AtomicInteger(0);

    /**
     * The sum of the duration (in nanoseconds) of each execution of a code point.
     */
    private AtomicLong totalRunTime = new AtomicLong(0);

    // ----------------------------------------------------------------------------------------------------

    void incrementNrOfRuns() {
        nrOfRuns.incrementAndGet();
    }

    void addToTotalRunTime(long time) {
        totalRunTime.addAndGet(time);
    }

    // ----------------------------------------------------------------------------------------------------

    int getNrOfRuns() {
        return nrOfRuns.get();
    }

    long getTotalRunTime() {
        return totalRunTime.get();
    }

}
