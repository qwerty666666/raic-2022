package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    int totalGames = 8;
    String version = "ai-cup-22-jar-with-dependencies.jar";

    Lock lock = new ReentrantLock();

    double games = 0;
    List<Integer> kills = new ArrayList<>();
    List<Double> damage = new ArrayList<>();
    List<Double> score = new ArrayList<>();
    List<Integer> place = new ArrayList<>();
    List<Integer> ticks = new ArrayList<>();
    List<Long> durations = new ArrayList<>();


    public void f() throws ExecutionException, InterruptedException {

        var startTime = System.currentTimeMillis();

        var es = Executors.newFixedThreadPool(4);

        List<Future> futures = new ArrayList<>();
        for (int i = 0; i < totalGames; i++) {
            futures.add(es.submit(new Task(i)));
        }

        for (var f: futures) {
            f.get();
        }

        System.out.println("Games " + games);
        System.out.format("kills: avg: %.2f, worst: %.2f, best: %.2f %n",
                kills.stream().mapToDouble(e -> e).sum() / games,
                kills.stream().mapToDouble(e -> e).min().orElse(0),
                kills.stream().mapToDouble(e -> e).max().orElse(0)
        );
        System.out.format("damage: avg: %.2f, worst: %.2f, best: %.2f %n",
                damage.stream().mapToDouble(e -> e).sum() / games,
                damage.stream().mapToDouble(e -> e).min().orElse(0),
                damage.stream().mapToDouble(e -> e).max().orElse(0)
        );
        System.out.format("score: avg: %.2f, worst: %.2f, best: %.2f  %n",
                score.stream().mapToDouble(e -> e).sum() / games,
                score.stream().mapToDouble(e -> e).min().orElse(0),
                score.stream().mapToDouble(e -> e).max().orElse(0)
        );
        System.out.format("place: avg: %.2f, worst: %.2f, best: %.2f | win rate %.2f %n",
                place.stream().mapToDouble(e -> e).sum() / games,
                place.stream().mapToDouble(e -> e).min().orElse(0),
                place.stream().mapToDouble(e -> e).max().orElse(0),
                place.stream().filter(p -> p == 1).count() / games
        );
        System.out.format("ticks: avg: %.2f, worst: %.2f, best: %.2f %n",
                ticks.stream().mapToDouble(e -> e).sum() / games,
                ticks.stream().mapToDouble(e -> e).min().orElse(0),
                ticks.stream().mapToDouble(e -> e).max().orElse(0)
        );
        System.out.format("time: avg: %.2f, worst: %.2f, best: %.2f | avg time per tick: %.2f %n",
                durations.stream().mapToDouble(e -> e).sum() / games,
                durations.stream().mapToDouble(e -> e).min().orElse(0),
                durations.stream().mapToDouble(e -> e).max().orElse(0),
                (durations.stream().mapToDouble(e -> e).sum() / ticks.stream().mapToDouble(e -> e).sum())
        );
        System.out.println("total time " + ((System.currentTimeMillis() - startTime) / 1000.));

        es.shutdown();
    }

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        new Main().f();
    }

    class Task implements Runnable {
        public int id;

        public Task(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {
                var dir = "/mnt/f/raic2022/solution/batch-runner/";
                var filename = "res" + Thread.currentThread() + ".json";
                var configFile = dir + "config-" + id + ".json";

                var port = 30000 + id;
                var config = Files.readString(Path.of(dir + "config_test.json"))
                        .replace("31001", Integer.toString(port))
                        .replace("1.0.jar", version);
                Files.write(Path.of(configFile), config.getBytes(StandardCharsets.UTF_8));

                var pb = new ProcessBuilder(new String[]{"bash", "-l", "-c", (dir + "aicup22 --batch-mode --config " + configFile + " --save-replay /mnt/f/raic2022/solution/batch-runner/repl-" + id + " --save-results " + (dir + filename))});
                pb.redirectError(Path.of(dir + "err").toFile());
                pb.redirectOutput(Path.of(dir + "stdout").toFile());
                pb.directory(new File(dir));
                //Runtime.getRuntime().exe

                var startMs = System.currentTimeMillis();

                var lr = pb.start();

                lr.waitFor();

                var duration = System.currentTimeMillis() - startMs;

                Files.delete(Path.of(configFile));

                var res = new ObjectMapper().readValue(Path.of(dir + filename).toFile(), Map.class);
                int a = 0;

                Map results = (Map) ((List) (((Map) res.get("results")).get("players"))).get(0);

                lock.lock();

                games++;
                kills.add((int) (results.get("kills")));
                damage.add((double) (results.get("damage")));
                place.add((int) (results.get("place")));
                score.add((double) (results.get("score")));
                ticks.add((int) (((Map) res.get("results")).get("tick_duration")));
                durations.add(duration);

                System.out.println(games + " " + id + " " + results);

                lock.unlock();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}