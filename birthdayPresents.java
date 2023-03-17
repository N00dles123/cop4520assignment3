import java.util.*;
import java.util.concurrent.*;
// make a linked list type structure
public class birthdayPresents {
    public static void main(String[] args){
        
    }

}

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

class LinkedList {
    ConcurrentLLNode head;
    ConcurrentLLNode tail;
    int size;
    Semaphore lock = new Semaphore(1);
    LinkedList() {
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
    // add will add to keep list in growing order to make finding stuff easier
    public void add(int data){
        try{
            lock.acquire();
            ConcurrentLLNode nextNode = new ConcurrentLLNode(data);
            if (head == null) {
                head = nextNode;
                tail = nextNode;
            } else if(head.data >= nextNode.data){
                nextNode.next = head;
                nextNode.next.prev = nextNode;
                head = nextNode;
            } else {
                ConcurrentLLNode cur = head;
                while(cur.next != null && cur.next.data < nextNode.data){
                    cur = cur.next;
                }

                nextNode.next = cur.next;

                if(cur.next != null){
                    nextNode.next.prev = nextNode;
                }

                cur.next = nextNode;
                nextNode.prev = cur;

                if(nextNode.next == null){
                    tail = nextNode;
                }
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
