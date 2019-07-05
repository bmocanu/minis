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

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Utils {

    public static ExecutorService executorService = Executors.newFixedThreadPool(200);

    public static ExecutorService secondExecutorService = Executors.newFixedThreadPool(100);

    public static Random random = new Random();

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static int random(int max) {
        return random.nextInt(max);
    }

}
