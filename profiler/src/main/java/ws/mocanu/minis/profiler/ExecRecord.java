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
    private final LongAdder nrOfRuns = new LongAdder();

    /**
     * The sum of the duration (in nanoseconds) of each execution of a code point.
     */
    private final LongAdder totalRunTime = new LongAdder();

    private final long[] lastExecutions = new long[10];

    private int lastExecutionCursor = 0;

    // ----------------------------------------------------------------------------------------------------

    public void recordExecution(long executionTime) {
        nrOfRuns.increment();
        totalRunTime.add(executionTime);

        synchronized (lastExecutions) {
            lastExecutionCursor++;
            if (lastExecutionCursor >= lastExecutions.length) {
                lastExecutionCursor = lastExecutionCursor % lastExecutions.length;
            }
            lastExecutions[lastExecutionCursor] = executionTime;
        }
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

    public long getSumForLastNExecutions() {
        long sum = 0;
        for (int index = 0; index < lastExecutions.length; index++) {
            sum += lastExecutions[index];
        }
        return sum;
    }

}
