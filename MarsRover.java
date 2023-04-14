import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

// Main class
public class MarsRover
{
    // Main method
    public static void main(String [] args)
    {
        // time scale determines how fast an hour passes in the simulation
        double timeScale = 10.0;
        // create a report instance
        TemperatureReport report = new TemperatureReport();
        // we'll be using 8 threads for this simulation
        Thread [] threads = new Thread[8];
        
        // create all 8 threads and start them
        for (int i = 0; i < 8; i++)
        {
            threads[i] = new Thread(new Sensor(report, timeScale, i));
            threads[i].start();
        }

        // Create the reporter thread
        Thread reporter = new Thread(() -> {
            while(true)
            {
                try
                {
                    // sleep until its time to give the hourly report
                    // 1 hour * 60 minutes * 60 seconds * 1000 ms / timeScale gets sleep time in ms
                    Thread.sleep(Math.max(1, (long)(1 * 60 * 60 * 1000 / timeScale)));
                    report.generateHourlyReport();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        // start the report
        reporter.start();

        // Join/close all threads
        for (Thread t : threads)
        {
            try
            {
                t.join();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        try
        {
            reporter.join();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        System.out.println("All done!");
    }
}

// Sensor class/thread
class Sensor implements Runnable 
{
    // create class fields for report, randomizer, and timescale + thread id
    private TemperatureReport report;
    private Random rand;
    private double timeScale;
    private int id;

    // Constructor
    public Sensor(TemperatureReport report, double timeScale, int id) 
    {
        this.report = report;
        this.rand = new Random();
        this.timeScale = timeScale;
        this.id = id;
    }

    // Generate a random temperature between -100 and 70
    private double generateTemperature() 
    {
        double min = -100.0;
        double max = 70.0;
        return min + (max - min) * rand.nextDouble();
    }

    @Override
    public void run() 
    {
        while (true)
        {
            try
            {
                // sleep until its time to take a reading
                // 1 hour * 60 mintues * 60 seconds / timeScale gets the sleep time in seconds
                Thread.sleep(Math.max(1, (long) (1 * 60 * 60 / timeScale)));
                // generate and add this temperature
                double temp = generateTemperature();
                report.addTemperature(temp);
                // print the reading
                System.out.println("Thread " + id + ": Temperature reading: " + temp);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}

// Temperature Report Class
class TemperatureReport 
{
    // Create a lock and data list
    private ReadWriteLock readWriteLock;
    private List<Double> temperatureData;
    
    // Cosntructor
    public TemperatureReport() 
    {
        this.readWriteLock = new ReentrantReadWriteLock();
        this.temperatureData = new ArrayList<>();
    }
    
    // Add a temperature to the list
    public void addTemperature(double temperature) 
    {
        // lock first then add
        readWriteLock.writeLock().lock();
        try
        {
            temperatureData.add(temperature);
        }
        finally
        {
            readWriteLock.writeLock().unlock();
        }
    }
    
    // get a list of the data
    public List<Double> getTemperatureData() 
    {
        readWriteLock.readLock().lock();
        try
        {
            return new ArrayList<>(temperatureData);
        }
        finally
        {
            readWriteLock.readLock().unlock();
        }
    }
    
    // print out the top 5 temps, bottom 5 temps, and the max difference
    public void generateHourlyReport() 
    {
        List<Double> data = getTemperatureData();
        List<Double> top5Hi = getTop5Highest(data);
        List<Double> top5Lo = getTop5Lowest(data);

        int maxTempDiff = getMaxTempDiff(data);
        System.out.println("Hourly Report: ");
        System.out.println("Top 5 highest temps: " + top5Hi);
        System.out.println("Top 5 lowest temps: " + top5Lo);
        System.out.println("Max temperature difference: " + maxTempDiff);
    }
    
    // HELPER FUNCTIONS
    private List<Double> getTop5Highest(List<Double> data) 
    {
        List<Double> sorted = new ArrayList<>(data);
        Collections.sort(sorted, Collections.reverseOrder());
        return sorted.subList(0, Math.min(5, sorted.size()));
    }
    
    private List<Double> getTop5Lowest(List<Double> data) 
    {
        List<Double> sorted = new ArrayList<>(data);
        Collections.sort(sorted, Collections.reverseOrder());
        return sorted.subList(0, Math.min(5, sorted.size()));
    }
    
    private int getMaxTempDiff(List<Double> data) 
    {
        int start = 0;
        double max = Double.MIN_VALUE;

        for (int i = 0; i <= data.size() - 10; i++)
        {
            double minT = Collections.min(data.subList(i, i + 10));
            double maxT = Collections.max(data.subList(i, i + 10));
            double diff = maxT - minT;

            if (diff > max)
            {
                max = diff;
                start = i;
            }
        }

        return start;
    }
}

