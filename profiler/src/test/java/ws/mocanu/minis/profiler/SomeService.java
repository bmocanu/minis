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

public class SomeService {

    private MultiThreadedService multiThreadedService = new MultiThreadedService();

    public void method1() {
        Utils.sleep(350);
    }

    public void method2() throws Exception {
        Utils.sleep(550);
        try (Trace ignored = Profiler.lets.start("MultiThreadedService.method1")) {
            multiThreadedService.method1();
        }
        Utils.sleep(150);
        try (Trace ignored = Profiler.lets.start("MultiThreadedService.method2")) {
            multiThreadedService.method2();
        }
    }

    public void method3() {
        Utils.sleep(150);
        try (Trace ignored = Profiler.lets.start("MultiThreadedService.method3")) {
            multiThreadedService.method3();
        }
    }

}
