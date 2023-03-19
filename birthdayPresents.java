import java.util.*;
import java.util.concurrent.*;
// make a linked list type structure
public class birthdayPresents {
    final int numGuests = 500000;
    final int numServants = 4;
    LinkedList<Integer> presents;
    ConcurLinkedList list;
    HashSet<Integer> thankYouCards;
    Semaphore lock = new Semaphore(1);
    boolean printOutput;
    public birthdayPresents(){
        presents = randomizePresents(numGuests);
        list = new ConcurLinkedList();
        thankYouCards = new HashSet<Integer>();
        printOutput = false;
    }
    
    public static void main(String[] args){
        birthdayPresents bp = new birthdayPresents();
        if(args.length == 1 && args[0].toLowerCase().equals("true")){
           bp.printOutput = Boolean.parseBoolean(args[0]);
        }
        Thread[] servants = new Thread[bp.numServants];
        long startTime = System.currentTimeMillis();
        for(int i = 0; i < bp.numServants; i++){
            servants[i] = new Thread(new Servant(bp));
            servants[i].start();
        }

        for(int i = 0; i < bp.numServants; i++){
            try{
                servants[i].join();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Time taken: " + (endTime - startTime) + " ms");
    }

    // to use in tester class
    public long run(boolean printOutput){
        long startTime = System.currentTimeMillis();
        birthdayPresents bp = new birthdayPresents();
        if(printOutput){
            bp.printOutput = printOutput;
        }
        Thread[] servants = new Thread[bp.numServants];
        for(int i = 0; i < bp.numServants; i++){
            servants[i] = new Thread(new Servant(bp));
            servants[i].start();
        }

        for(int i = 0; i < bp.numServants; i++){
            try{
                servants[i].join();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Time taken: " + (endTime - startTime) + " ms");
        return endTime - startTime;
    }
    // simulates a random bag of presents
    public static LinkedList<Integer> randomizePresents(int numGuests){
        LinkedList<Integer> presents = new LinkedList<Integer>();
        for(int i = 0; i < numGuests; i++){
            presents.add(i);
        }
        Collections.shuffle(presents);
        return presents;
    }
}

// this will be used to manage servants each servant is a thread and gets assigned a random task on each run. 0 is adding a gift to chain, 
// 1 is searching for a random gift in chain, and 2 is writing a thank you card
class Servant implements Runnable{
    birthdayPresents bp;
    Random r = new Random();
    public Servant(birthdayPresents bp){
        this.bp = bp;
    }
    public void run(){
        while(bp.thankYouCards.size() < bp.numGuests){
            int job = r.nextInt(3);
            if(job == 0){
                // add gift to list in order to process
                try{
                    bp.lock.acquire();
                    if(bp.presents.size() > 0){
                        int present = bp.presents.pop();
                        bp.list.add(present);
                    }
                    bp.lock.release();
                } catch (Exception e){
                    e.printStackTrace();
                }
            } else if(job == 1){
                // search for gift
                int randGift = r.nextInt(bp.numGuests);
                boolean found = bp.list.contains(randGift) && bp.thankYouCards.contains(randGift);
                // prints out when gift is found
                if(bp.printOutput)
                    System.out.println("Gift " + (randGift + 1) + " has been found: " + found);
            } else if(job == 2){
                // this task is for writing card
                if(bp.list.size == 0){
                    continue;
                }

                int gift = bp.list.pop();
                try{   
                    bp.lock.acquire(); 
                    if(gift != -1){
                        bp.thankYouCards.add(gift);
                        if(bp.printOutput)
                            System.out.println("Thank you guest number " + (gift + 1) + " for the present!");
                    }
                    bp.lock.release();
                } catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
    }
}

// store concurrent LinkedList node
class ConcurrentLLNode {
    int data;
    ConcurrentLLNode next;
    ConcurrentLLNode prev;
    Semaphore lock = new Semaphore(1);
    ConcurrentLLNode(int data) {
        this.data = data;
        this.next = null;
    }
    public String toString() {
        return Integer.toString(data);
    }
}

// store concurrent LinkedList
class ConcurLinkedList {
    ConcurrentLLNode head;
    ConcurrentLLNode tail;
    int size;
    Semaphore lock = new Semaphore(1);
    ConcurLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }
    public String toString(){
        String str = "";
        try{
            lock.acquire();
            ConcurrentLLNode cur = head;
            while(cur != null){
                str += cur.data + " ";
                cur = cur.next;
            }
            lock.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return str;
    }
    // add will add to end of linked list
    public void add(int data){
        try{
            lock.acquire();
            ConcurrentLLNode nextNode = new ConcurrentLLNode(data);
            // insert between head and its next value
            if(head != null){
                tail.next = nextNode;
                nextNode.prev = tail;
                tail = nextNode;
            } else {
                head = nextNode;
                tail = nextNode;
            }
            size++;
            lock.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    // checks if list has certain value
    public boolean contains(int data){
        boolean found = false;
        try{
            lock.acquire();
            ConcurrentLLNode cur = head;
            while(cur != null){
                if(cur.data == data){
                    //System.out.println("content found");
                    found = true;
                    break;
                }
                cur = cur.next;
            }
            lock.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return found;
    }
    // pop operation will remove head and give integer value
    public int pop(){
        int data = -1;
        try{
            lock.acquire();
            if(head != null){
                data = head.data;
                head = head.next;
                if(head != null){
                    head.prev = null;
                }
                size--;
            }
            lock.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return data;
    }
    // remove will remove the first instance of the data
    public void remove(int data){
        if(head == null){
            return;
        }
        try{
            ConcurrentLLNode cur = head;
            lock.acquire();
            
            if(cur.data == data){
                head = cur.next;
                if(head != null){
                    head.prev = null;
                }
                size--;
            
            } else {
                while(cur.next != null){
                    if(cur.next.data == data){
                        cur.next = cur.next.next;
                        if(cur.next != null){
                            cur.next.prev = cur;
                        }
                        size--;
                        break;
                    }
                    cur = cur.next;
                }
            }
            lock.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
