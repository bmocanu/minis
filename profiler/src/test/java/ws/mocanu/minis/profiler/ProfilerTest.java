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

public class ProfilerTest {

    public static void main(String[] args) throws InterruptedException {
        Profiler.lets.setHttpControlOnPort(10000);

        List<Callable<String>> controllerCallables = new LinkedList<>();
        for (int index = 0; index < 50; index++) {
            controllerCallables.add(() -> {
                RequestController requestController = new RequestController();
                return requestController.handleRequest();
            });
        }

        List<Future<String>> controllerFutures = Utils.secondExecutorService.invokeAll(controllerCallables);
        for (Future<String> future : controllerFutures) {
            try {
                System.out.println("Received controller response: " + future.get());
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        Utils.executorService.shutdown();
        Utils.secondExecutorService.shutdown();
    }

}
