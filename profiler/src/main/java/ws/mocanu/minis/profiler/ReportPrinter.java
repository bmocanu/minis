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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A printer for each profiler report line. By default this class uses SLF4J as report destination, but this
 * class can be easily overridden to have the report lines printed elsewhere.
 */
public class ReportPrinter {

    private static final Logger LOG = LoggerFactory.getLogger(Profiler.class);

    public void printReportLine(String format, Object... args) {
        LOG.info(String.format(format, (Object[]) args));
    }

}
