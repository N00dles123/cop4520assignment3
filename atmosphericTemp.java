import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
// make use of matrix to store the temperatures at each hour
// use semamphores to lock the arraylist and enforce mutal exclusion on critical section
// wait for all sensors to finish by using an array of booleans
// we only need to keep track of temperatures in that one hour so we can override in the next hour
public class atmosphericTemp {
    final static int NUM_THREADS = 8;
    final Random r = new Random();
    // you can manually set the num of hours
    int numHours = 72;
    volatile ArrayList<ArrayList<Integer>> temps; // each thread records a temperature every minute
    volatile ArrayList<Boolean> sensorAvailable;

    public static void main(String[] args){
        atmosphericTemp at = new atmosphericTemp();
        at.temps = new ArrayList<ArrayList<Integer>>();
        at.sensorAvailable = new ArrayList<>();

        // initialize the arraylist of arraylists
        for(int i = 0; i < NUM_THREADS; i++){
            at.temps.add(new ArrayList<Integer>());
            at.sensorAvailable.add(false);
            for(int j = 0; j < 60; j++){
                at.temps.get(i).add(0);
            }
        }

        
        Thread[] sensors = new Thread[NUM_THREADS];
        long startTime = System.currentTimeMillis();
        for(int i = 0; i < NUM_THREADS; i++){
            sensors[i] = new Thread(new sensorThread(at, i));
            sensors[i].start();
        }

        for(int i = 0; i < NUM_THREADS; i++){
            try{
                sensors[i].join();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Time taken: " + (endTime - startTime) + " ms");
        
    }
    
    // checks to see if sensors are ready so we can proceed to "next minute"
    public boolean sensorsReady(int index, ArrayList<Boolean> sensorAvailable){
        for(int i = 0; i < NUM_THREADS; i++){
            if(!sensorAvailable.get(i) && index != i){
                return false;
            }
        }
        return true;
    }
    // get 5 highest temps
    public void getHighestTemps(ArrayList<ArrayList<Integer>> temps){
        // add all array values to a pq
        PriorityQueue<Integer> pq = new PriorityQueue<>(Collections.reverseOrder());
        for(int i = 0; i < temps.size(); i++){
            for(int j = 0; j < temps.get(i).size(); j++){
                pq.add(temps.get(i).get(j));
            }
        }
        System.out.println("Highest temperatures: ");
        for(int i = 0; i < 5; i++){
            System.out.print(pq.poll() + "F ");
        }
        System.out.println();
    }

    // each index represents a minute and thread value so example 0 is thread 0 minute 0 and 59 is thread 0 minute 59
    public void getLargestDiff(ArrayList<ArrayList<Integer>> temperatures){
        int largestDiff = Integer.MIN_VALUE;
        int interval = 0;
        // loop in chunks like a sliding window problem but for each thread each minute
        for(int sensor = 0; sensor < 8; sensor++){
            for(int i = 0; i < 49; i++){
                int max = max(sensor, i, i + 10, temperatures);
                int min = min(sensor, i, i + 10, temperatures);
                int diff = max - min;
                if(diff > largestDiff){
                    largestDiff = diff;
                    interval = i;
                }
            }
        }
        
        System.out.println("Largest temperature difference: " + largestDiff + "F " + " from minute " + interval + " to minute " + (interval + 10));
    }



    // utility function to get highest value in a range
    public int max(int sensor, int start, int end, ArrayList<ArrayList<Integer>> temperatures){
        int max = Integer.MIN_VALUE;
        for(int i = start; i < end; i++){
            if(temperatures.get(sensor).get(i) > max){
                max = temperatures.get(sensor).get(i);
            }
        }
        return max;
    }
    // utility function to get lowest value in a range
    public int min(int sensor, int start, int end, ArrayList<ArrayList<Integer>> temperatures){
        int min = Integer.MAX_VALUE;
        for(int i = start; i < end; i++){
            if(temperatures.get(sensor).get(i) < min){
                min = temperatures.get(sensor).get(i);
            }
        }
        return min;
    }

    
    public int generateRandomTemp(){
        int temp = r.nextInt(171) - 100;
        return temp;
    }
    // get 5 lowest temps
    public void getLowestTemps(ArrayList<ArrayList<Integer>> temps){
        // add all array values to a pq
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        for(int i = 0; i < temps.size(); i++){
            for(int j = 0; j < temps.get(i).size(); j++){
                pq.add(temps.get(i).get(j));
            }
        }
        System.out.println("Lowest temperatures: ");
        for(int i = 0; i < 5; i++){
            System.out.print(pq.poll() + "F ");
        }
        System.out.println();
    }

    public void hourlyReport(int hour, ArrayList<ArrayList<Integer>> temperatures){
        try{
            System.out.println("Hour " + (hour + 1) + " report:");
            // get 5 highest temps
            getHighestTemps(temperatures);
            // get 5 lowest temps
            getLowestTemps(temperatures);
            // get 10 min interval of largest diff
            getLargestDiff(temperatures);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    
}

class sensorThread implements Runnable{
    Semaphore lock = new Semaphore(1);
    volatile atmosphericTemp at;
    int id;
    public sensorThread(atmosphericTemp at, int id){
        this.at = at;
        this.id = id;
    }
    public void run(){
        recordTemperature(this.id, at.temps, at.sensorAvailable);
    }
    
    public void recordTemperature(int id, ArrayList<ArrayList<Integer>> temps, ArrayList<Boolean> sensorAvailable){
        // to simulate each hour and minute
        for(int i = 0; i < at.numHours; i++){
            for(int j = 0; j < 60; j++){
                // since its recording temp we set it to not ready
                at.sensorAvailable.set(this.id, false);
                at.temps.get(id).set(j, at.generateRandomTemp());
                at.sensorAvailable.set(this.id, true);
                // check to see if all sensors are done for current minute before continuing if not we sleep the thread
                while(!at.sensorsReady(id, at.sensorAvailable)){
                    //System.out.println("thread " + id + " is sleeping");
                    try{
                        Thread.sleep(10); // put thread to sleep for 15 ms
                    } catch (Exception e){
                    }
                }
                
            }
            
            // once we are done with the hour we print the report
            // we only dedicate one thread to print the report
            if(this.id == 7){
                try{
                    lock.acquire();
                    at.hourlyReport(i, at.temps);
                    lock.release();
                } catch (Exception e){

                }
            }
            
        }
    }
    
}
