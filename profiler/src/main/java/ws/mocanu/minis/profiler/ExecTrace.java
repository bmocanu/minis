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

/**
 * An execution trace maps the details of a particular code execution. Such traces are stored in a
 * {@link ThreadLocal} to allow for trace aggregation and "stack" unwinding.
 */
public class ExecTrace implements AutoCloseable {

    private String point;
    private long startTimestamp;
    private ExecTrace parent;
    private ExecLink link;

    ExecTrace(String point, long startTimestamp, ExecTrace parent, ExecLink link) {
        this.point = point;
        this.startTimestamp = startTimestamp;
        this.parent = parent;
        this.link = link;
    }

    String getPoint() {
        return point;
    }

    long getStartTimestamp() {
        return startTimestamp;
    }

    ExecTrace getParent() {
        return parent;
    }

    ExecLink getLink() {
        return link;
    }

    @Override
    public void close() {
        Profiler.lets.end(this);
    }

}
