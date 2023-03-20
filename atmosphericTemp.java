import java.util.*;
import java.util.concurrent.*;
// make use of matrix to store the temperatures at each hour
// use semamphores to lock the arraylist and enforce mutal exclusion on critical section
// wait for all sensors to finish by using an array of booleans
// we only need to keep track of temperatures in that one hour so we can override in the next hour
public class atmosphericTemp {
    final static int NUM_THREADS = 8;
    int[][] temps = new int[NUM_THREADS][60]; // each thread records a temperature every minute
    boolean sensorAvailable[] = new boolean[NUM_THREADS];
    Semaphore lock = new Semaphore(1);
    // you can manually set the num of hours
    int numHours = 24;
    public static void main(String[] args){
        atmosphericTemp at = new atmosphericTemp();
        // all sensors begin as ready
        for(int i = 0; i < NUM_THREADS; i++){
            at.sensorAvailable[i] = true;
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
    public void run(){
        atmosphericTemp at = new atmosphericTemp();
        // all sensors begin as ready
        for(int i = 0; i < NUM_THREADS; i++){
            at.sensorAvailable[i] = true;
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
    public boolean sensorsReady(int index){
        for(int i = 0; i < NUM_THREADS; i++){
            if(!sensorAvailable[i] && i != index){
                return false;
            }
        }
        return true;
    }
    
}

class sensorThread implements Runnable{
    atmosphericTemp at;
    int id;
    public sensorThread(atmosphericTemp at, int id){
        this.at = at;
        this.id = id;
    }
    public void run(){
        // to simulate each hour and minute
        for(int i = 0; i < at.numHours; i++){
            for(int j = 0; j < 60; j++){
                // since its recording temp we set it to not ready
                at.sensorAvailable[id] = false;
                // record current temp
                at.temps[id][j] = generateRandomTemp();
                at.sensorAvailable[id] = true;

                // check to see if all sensors are done for current minute before continuing if not we sleep the thread
                while(!at.sensorsReady(id)){
                    try{
                        Thread.sleep(15); // put thread to sleep for 15 milliseconds
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            // we will let one thread generate the hourly report
            if(id == 0){
                // first acquire lock
                try{
                    at.lock.acquire();
                } catch (Exception e){
                    e.printStackTrace();
                }
                System.out.println("Hour " + (i + 1) + " report: ");
                hourlyReport();
                //System.out.println();
                /*
                for(int k = 0; k < 8; k++){
                    for(int l = 0; l < 60; l++){
                        System.out.print(at.temps[k][l] + " ");
                    }
                    System.out.println();
                }
                System.out.println();
                */
                try{
                    at.lock.release();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    // this will be used to get the temperature from the sensor
    public int generateRandomTemp(){
        Random rand = new Random();
        int temp = rand.nextInt(171) - 100;
        return temp;
    }
    public void hourlyReport(){
        //System.out.println(Arrays.toString(at.temps));
        // get 10 min interval of largest diff
        getLargestDiff();
        // get 5 highest temps
        getHighestTemps();
        // get 5 lowest temps
        getLowestTemps();
    }
    // fix this function
    // each index represents a minute and thread value so example 0 is thread 0 minute 0 and 59 is thread 0 minute 59
    public void getLargestDiff(){
        int largestDiff = Integer.MIN_VALUE;
        int interval = 0;
        // loop in chunks like a sliding window problem but for each thread each minute
        for(int sensor = 0; sensor < 8; sensor++){
            for(int i = 0; i < 49; i++){
                int max = max(sensor, i, i + 10);
                int min = min(sensor, i, i + 10);
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
    public int max(int sensor, int start, int end){
        int max = Integer.MIN_VALUE;
        for(int i = start; i < end; i++){
            if(at.temps[sensor][i] > max){
                max = at.temps[sensor][i];
            }
        }
        return max;
    }
    // utility function to get lowest value in a range
    public int min(int sensor, int start, int end){
        int min = Integer.MAX_VALUE;
        for(int i = start; i < end; i++){
            if(at.temps[sensor][i] < min){
                min = at.temps[sensor][i];
            }
        }
        return min;
    }

    // get 5 highest temps
    public void getHighestTemps(){
        // add all array values to a pq
        PriorityQueue<Integer> pq = new PriorityQueue<>(Collections.reverseOrder());
        for(int i = 0; i < at.temps.length; i++){
            for(int j = 0; j < at.temps[i].length; j++){
                pq.add(at.temps[i][j]);
            }
        }
        System.out.println("Highest temperatures: ");
        for(int i = at.temps.length - 1; i > at.temps.length - 6; i--){
            System.out.print(pq.poll() + "F ");
        }
        System.out.println();
    }

    // get 5 lowest temps
    public void getLowestTemps(){
        // add all array values to a pq
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        for(int i = 0; i < at.temps.length; i++){
            for(int j = 0; j < at.temps[i].length; j++){
                pq.add(at.temps[i][j]);
            }
        }
        System.out.println("Lowest temperatures: ");
        for(int i = 0; i < 5; i++){
            System.out.print(pq.poll() + "F ");
        }
        System.out.println();
    }
    
}
