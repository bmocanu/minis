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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Profiler is the main class of this tiny code profiler. Allows timing various executions,
 * with aggregated executions and nicely printed reports.
 */
@SuppressWarnings("unused")
public class Profiler {

    // ----------------------------------------------------------------------------------------------------
    // Configuration of the profiler
    // ----------------------------------------------------------------------------------------------------

    public void setHttpControlOnPort(int port) {
        if (this.httpControl != null) {
            httpControl.prepareToStop();
        }

        httpControl = new HttpControl();
        httpControl.init(port, this);
        httpControl.start();
    }

    public void setReportPrinter(ReportPrinter printer) {
        this.reportPrinter = printer;
    }

    // ----------------------------------------------------------------------------------------------------
    // Action methods
    // ----------------------------------------------------------------------------------------------------

    /**
     * Starts a new timing for a piece of code.
     *
     * @param point the name of the code point. Can be any string you want, with any format. It just needs to
     *              be unique to that code point and should stay the same if multiple threads are calling that
     *              point.
     * @return a trace to be used either as {@link AutoCloseable} or as a parameter to {@link #end(Trace)}. Can
     *     also be ignored, because {#end(ExecTrace)} usually knows how to pick it up.
     */
    public Trace start(String point) {
        return start(point, parentTraceStore.get());
    }

    /**
     * Starts a new timing for a piece of code, with the name of the code point composed as the name of the
     * currently executing trace (if any) suffixed with the given string.
     *
     * @param pointSuffix the suffix to add to the currently running trace, to form the name of the new trace
     * @return a trace to be used either as {@link AutoCloseable} or as a parameter to {@link #end(Trace)}. Can
     *     also be ignored, because {#end(ExecTrace)} usually knows how to pick it up.
     */
    public Trace startWithInheritedName(String pointSuffix) {
        Trace parentTrace = parentTraceStore.get();
        if (parentTrace != null) {
            return start(parentTrace.getPoint() + (pointSuffix.startsWith(".") ? "" : ".") + pointSuffix);
        } else {
            return start("INHERITED_TRACE_NOT_AVAILABLE." + pointSuffix);
        }
    }

    /**
     * Creates a {@link Callable} decorator, so that when the given Callable is executed, it is automatically
     * wrapped into an execution trace (and hence, automatically timed).
     *
     * @param point  the name of the point corresponding to the given Callable
     * @param target the Callable to call and time
     * @param <T>    the type parameter of the Callable's returned value
     * @return another Callable, of the same type as the given one, ready to be executed
     */
    public <T> Callable<T> timeCallable(final String point, final Callable<T> target) {
        final Trace parentTrace = parentTraceStore.get();
        return () -> {
            Trace trace = Profiler.lets.start(point, parentTrace);
            T result = target.call();
            Profiler.lets.end(trace);
            return result;
        };
    }

    /**
     * Ends the currently running trace.
     */
    public void end() {
        end(parentTraceStore.get());
    }

    /**
     * Ends the given trace.
     *
     * @param trace the trace to end.
     */
    public void end(Trace trace) {
        if (trace == null) {
            return;
        }
        long currentTimestamp = System.nanoTime();
        ExecRecord record = records.get(trace.getPoint());
        if (record != null) {
            record.recordExecution(currentTimestamp - trace.getStartTimestamp());
        }
        parentTraceStore.set(trace.getParent());
    }

    public void reset() {
        // this might crash or the processing in some thread might crash
        for (ExecRecord record : records.values()) {
            record.reset();
        }
    }

    // ----------------------------------------------------------------------------------------------------

    /**
     * A dictionary of {@link ExecRecord} mapped to the code points that generated them. Each item holds
     * the number of times the code point was invoked and the sum of each execution time in nanoseconds.
     */
    private Map<String, ExecRecord> records;

    /**
     * A dictionary of {@link ExecLink} mapped to the code points that generated them. Each link is
     * part of a call tree, having a parent and one or more children.
     */
    private Map<String, ExecLink> links;

    /**
     * The thread that triggers the printing of the report after X seconds since the last timing.
     */
    private ReportMonitor reportMonitor;

    /**
     * The component responsible for printing a report line.
     */
    private ReportPrinter reportPrinter;

    /**
     * The timestamp when a timing was last performed, in nanoseconds since (some) starting point.
     * This is reset after each printed report.
     */
    private long lastRecordedTimestamp = Long.MAX_VALUE;

    /**
     * The root node of the call graph. Used for anchoring new links as timings are performed and for
     * knowing where to start the report printing from.
     */
    private ExecLink rootLink = new ExecLink("Root");

    /**
     * A thread local that stored the currently timed {@link Trace}. Helps with created aggregated
     * executions.
     */
    private ThreadLocal<Trace> parentTraceStore = new ThreadLocal<>();
    private HttpControl httpControl;

    // ----------------------------------------------------------------------------------------------------

    public Profiler() {
        this.records = new HashMap<>();
        this.links = new HashMap<>();

        this.reportMonitor = new ReportMonitor();
        this.reportMonitor.init();
        this.reportMonitor.start();
        this.reportPrinter = new StdoutReportPrinter();
    }

    // ----------------------------------------------------------------------------------------------------

    public void printReport() {
        printReport(reportPrinter);
    }

    public void printReport(ReportPrinter printer) {
        int maxLineLength = calculateMaxLineLength(rootLink, 0) + 4;
        printReportInternal(rootLink, 0, maxLineLength, printer);
    }

    public long getLastRecordedTimestamp() {
        return lastRecordedTimestamp;
    }

    public void resetLastRecordedTimestamp() {
        this.lastRecordedTimestamp = Long.MAX_VALUE;
    }

    // ----------------------------------------------------------------------------------------------------

    private Trace start(String point, Trace parentTrace) {
        ExecLink parentLink = rootLink;
        if (parentTrace != null) {
            parentLink = parentTrace.getLink();
        }

        ExecRecord record;
        ExecLink link;

        record = records.get(point);
        if (record == null) {
            synchronized (point) {
                record = records.get(point);
                if (record == null) {
                    record = new ExecRecord();
                    records.put(point, record);
                }
            }
        }

        link = links.get(point);
        if (link == null) {
            synchronized (point) {
                link = links.get(point);
                if (link == null) {
                    link = new ExecLink(point);
                    parentLink.addChild(link);
                    links.put(point, link);
                }
            }
        }

        long currentTimestamp = System.nanoTime();
        Trace thisTrace = new Trace(point, currentTimestamp, parentTrace, link);
        parentTraceStore.set(thisTrace);
        lastRecordedTimestamp = currentTimestamp;
        return thisTrace;
    }

    private void printReportInternal(ExecLink link, int depth, int maxLineLength, ReportPrinter printer) {
        if (depth > 0) {
            ExecRecord record = records.get(link.getPoint());
            int indent = calculateReportLineIndent(depth);
            if (record != null) {
                long nrOfRuns = record.getNrOfRuns();
                long averageRunTime = 0;
                if (nrOfRuns > 0) {
                    averageRunTime = record.getTotalRunTime() / nrOfRuns / 1000000; // nanos to millis
                }
                long averageOnLastTen = record.getSumForLastNExecutions() / 10 / 1000000; // nanos to millis
                printer.printReportLine("Profiler| %" + indent + "s %-" + (maxLineLength - indent) + "s| runs:%6d | avgRunTime: %8d ms | avgOnLast10: %8d ms",
                                        " ", link.getPoint(), nrOfRuns, averageRunTime, averageOnLastTen);
            } else {
                printer.printReportLine("Profiler| %" + indent + "s %-" + (maxLineLength - indent) + "s| null",
                                        " ", link.getPoint());
            }
        }

        for (ExecLink currentChild : link.getChildren()) {
            printReportInternal(currentChild, depth + 1, maxLineLength, printer);
        }
    }

    private int calculateMaxLineLength(ExecLink link, int depth) {
        int localMax = calculateReportLineIndent(depth) + link.getPoint().length();
        for (ExecLink currentChild : link.getChildren()) {
            localMax = Math.max(localMax, calculateMaxLineLength(currentChild, depth + 1));
        }
        return localMax;
    }

    private int calculateReportLineIndent(int depth) {
        return (depth - 1) * 4 + 1;
    }

    // ----------------------------------------------------------------------------------------------------

    /**
     * The default instance of this Profiler. Works like a Singleton, without forcing you to actually
     * stay only with a singleton Profiler.
     */
    public static final Profiler lets = new Profiler();

}
