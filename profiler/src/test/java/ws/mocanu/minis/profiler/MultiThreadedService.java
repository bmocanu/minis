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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static ws.mocanu.minis.profiler.Utils.executorService;

public class MultiThreadedService {

    public void method1() {
        Utils.sleep(350);

        Callable<String> callable = () -> {
            // Perform some computation
            Utils.sleep(2000);
            return "First-Result";
        };

        System.out.println("Calling the first callable");
        Future<String> future = executorService.submit(
            Profiler.lets.timeCallable("MultiThreadedService.callable-1", callable)
        );
        try {
            System.out.println("Waiting for the first callable");
            String result = future.get();
            System.out.println("First callable returned: " + result);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void method2() {
        List<Callable<String>> callables = new LinkedList<>();
        for (int index = 0; index < 10; index++) {
            callables.add(Profiler.lets.timeCallable("MultiThreadedService.method2.callable-" + index, () -> {
                // Perform some computation
                Utils.sleep(Utils.random(2000));
                return "Some-Result";
            }));
        }

        try {
            System.out.println("Calling all the callables");
            List<Future<String>> futures = executorService.invokeAll(callables);
            for (int index = 0; index < 10; index++) {
                System.out.println("Waiting for the results from callable " + index);
                futures.get(index).get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void method3() {
        Utils.sleep(150);
    }

}
