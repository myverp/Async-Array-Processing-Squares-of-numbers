import java.util.*;
import java.util.concurrent.*;
import java.util.stream.DoubleStream;

public class AsyncArrayProcessing {
    public static void main(String[] args) {
        // Read the range from the user
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the minimum value of the range: ");
        double minRange = scanner.nextDouble();
        System.out.print("Enter the maximum value of the range: ");
        double maxRange = scanner.nextDouble();

        // Generate an array of random numbers
        Random random = new Random();
        int arraySize = 40 + random.nextInt(21); // Array size between 40 and 60
        double[] numbers = DoubleStream.generate(() -> minRange + (maxRange - minRange) * random.nextDouble())
                .limit(arraySize)
                .toArray();

        // Split the array into parts
        int partSize = arraySize / 4;
        List<double[]> parts = new ArrayList<>();
        for (int i = 0; i < arraySize; i += partSize) {
            parts.add(Arrays.copyOfRange(numbers, i, Math.min(i + partSize, arraySize)));
        }

        // Create ExecutorService for asynchronous task execution
        ExecutorService executorService = Executors.newFixedThreadPool(parts.size());
        List<Future<Set<Double>>> futures = new ArrayList<>();

        // Process array parts asynchronously
        for (double[] part : parts) {
            Callable<Set<Double>> task = () -> {
                Set<Double> result = new CopyOnWriteArraySet<>();
                for (double num : part) {
                    result.add(Math.pow(num, 2)); // Calculate the square of each number
                }
                return result;
            };
            futures.add(executorService.submit(task));
        }

        // Collect results and measure execution time
        long startTime = System.currentTimeMillis();
        try {
            for (Future<Set<Double>> future : futures) {
                if (!future.isCancelled()) { // Check if the task was not cancelled
                    while (!future.isDone()) { // Wait until the task is complete
                        System.out.println("Waiting for thread completion...");
                    }
                    Set<Double> result = future.get(); // Retrieve the result
                    System.out.println("Processed array part result: " + result);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown(); // Shut down the executor service
        }

        // Print the program's execution time
        long endTime = System.currentTimeMillis();
        System.out.println("Execution time: " + (endTime - startTime) + " ms");
    }
}
