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

import java.util.concurrent.atomic.LongAdder;

/**
 * A record of a how many times and for how long a particular code point was executed.
 */
public class ExecRecord {

    /**
     * The number of times a code point was executed.
     */
    private LongAdder nrOfRuns = new LongAdder();

    /**
     * The sum of the duration (in nanoseconds) of each execution of a code point.
     */
    private LongAdder totalRunTime = new LongAdder();

    // ----------------------------------------------------------------------------------------------------

    public void recordExecution(long executionTime) {
        nrOfRuns.increment();
        totalRunTime.add(executionTime);
    }

    public void reset() {
        nrOfRuns.reset();
        totalRunTime.reset();
    }

    // ----------------------------------------------------------------------------------------------------

    public long getNrOfRuns() {
        return nrOfRuns.longValue();
    }

    public long getTotalRunTime() {
        return totalRunTime.longValue();
    }

}
