import java.util.*;
import java.util.concurrent.*;
// make use of matrix to store the temperatures at each hour
// use semamphores to lock the arraylist and enforce mutal exclusion on critical section
// wait for all sensors to finish by using an array of booleans
// we only need to keep track of temperatures in that one hour so we can override in the next hour
public class atmosphericTemp {
    Semaphore mutex = new Semaphore(1, true);
    final static int NUM_THREADS = 8;
    int MINS_PER_HOUR = 60;
    final Random r = new Random();
    // you can manually set the num of hours
    int numHours = 72;
    volatile int[] temps; // each thread records a temperature every minute
    volatile ArrayList<Boolean> sensorAvailable;

    public static void main(String[] args){
        atmosphericTemp at = new atmosphericTemp();
        at.temps = new int[NUM_THREADS * 60];
        at.sensorAvailable = new ArrayList<>();
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter number of hours: ");
        at.numHours = sc.nextInt();
        sc.close();
        // initialize the arraylist of arraylists
        for(int i = 0; i < NUM_THREADS; i++){
            at.sensorAvailable.add(false);
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
    public boolean sensorsReady(ArrayList<Boolean> sensorAvailable, int id){
        for(int i = 0; i < NUM_THREADS; i++){
            if(!sensorAvailable.get(i) && id != i){
                return false;
            }
        }
        return true;
    }
    // get 5 highest temps
    public void getHighestTemps(int[] temps){
        // add all array values to a pq
        PriorityQueue<Integer> pq = new PriorityQueue<>(Collections.reverseOrder());
        for(int i = 0; i < temps.length; i++){
            pq.add(temps[i]);
        }
        System.out.println("Highest temperatures: ");
        for(int i = 0; i < 5; i++){
            System.out.print(pq.poll() + "F ");
        }
        System.out.println();
    }

    // each index represents a minute and thread value so example 0 is thread 0 minute 0 and 59 is thread 0 minute 59
    public void getLargestDiff(int[] temperatures){
        int largestDiff = Integer.MIN_VALUE;
        int interval = 0;
        // loop in chunks like a sliding window problem but for each thread each minute
        for(int threadId = 0; threadId < NUM_THREADS; threadId++){
            int curMin = threadId * 60;
            for(int i = curMin; i < MINS_PER_HOUR - 10 + 1; i += 10){
                int max = max(i, i + 10, temperatures);
                int min = min(i, i + 10, temperatures);
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
    public int max(int start, int end, int[] temperatures){
        int max = Integer.MIN_VALUE;
        for(int i = start; i < end; i++){
            if(temperatures[i] > max){
                max = temperatures[i];
            }
        }
        return max;
    }
    // utility function to get lowest value in a range
    public int min(int start, int end, int[] temperatures){
        int min = Integer.MAX_VALUE;
        for(int i = start; i < end; i++){
            if(temperatures[i] < min){
                min = temperatures[i];
            }
        }
        return min;
    }

    // get 5 lowest temps
    public void getLowestTemps(int[] temps){
        // add all array values to a pq
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        for(int i = 0; i < temps.length; i++){
            pq.add(temps[i]);
        }
        System.out.println("Lowest temperatures: ");
        for(int i = 0; i < 5; i++){
            System.out.print(pq.poll() + "F ");
        }
        System.out.println();
    }

    public void hourlyReport(int hour, int[] temperatures){
        try{
            System.out.println("Hour " + (hour + 1) + " report:");
            //System.out.println(Arrays.toString(temperatures));
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
    volatile atmosphericTemp at;
    int id;
    Random r = new Random();
    public sensorThread(atmosphericTemp at, int id){
        this.at = at;
        this.id = id;
    }
    public void run(){
        recordTemperature(this.id, at.temps, at.sensorAvailable);
    }
    public int generateRandomTemp(){
        int temp = r.nextInt(171) - 100;
        return temp;
    }

    public void recordTemperature(int id, int[] temps, ArrayList<Boolean> sensorAvailable){
        // to simulate each hour and minute
        for(int i = 0; i < at.numHours; i++){
            for(int j = 0; j < at.MINS_PER_HOUR; j++){
                sensorAvailable.set(this.id, false);
                temps[j + (id * 60)] = generateRandomTemp();
                sensorAvailable.set(this.id, true);
                // check to see if all sensors are done for current minute before continuing if not we sleep the thread
                while(!at.sensorsReady(sensorAvailable, id)){
                    //System.out.println("thread " + id + " is sleeping on minute " + j);
                    try{
                        Thread.sleep(10); // put thread to sleep for 10 ms
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                
            }
            // once we are done with the hour we print the report
            // we only dedicate one thread to print the report
            if(this.id == 0){
                try{
                    at.mutex.acquire();
                    at.hourlyReport(i, at.temps);
                    at.mutex.release();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            
        }
    }
    
}
