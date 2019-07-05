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

import ws.mocanu.minis.profiler.ReportPrinter;

/**
 * An report printer that prints to STDOUT.
 */
public class StdoutReportPrinter implements ReportPrinter {

    public void printReportLine(String format, Object... args) {
        System.out.println(String.format(format, (Object[]) args));
    }

}
