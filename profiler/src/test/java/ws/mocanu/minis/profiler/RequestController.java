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

public class RequestController {

    private SomeService someService = new SomeService();

    public String handleRequest() throws Exception {
        Profiler.lets.start("RequestController.handleRequest");

        try (Trace ignored = Profiler.lets.start("SomeService.method1")) {
            someService.method1();
        }

        for (int index = 0; index < 5; index++) {
            try (Trace ignored = Profiler.lets.start("SomeService.method2")) {
                someService.method2();
            }
        }

        try (Trace ignored = Profiler.lets.start("SomeService.method3")) {
            someService.method3();
        }

        Profiler.lets.end();
        return "Controller-Response";
    }

}
