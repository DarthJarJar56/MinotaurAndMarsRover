import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

// Main class
public class Minotaur
{
    // Main method
    public static void main(String [] args)
    {
        // Instantiate custom concurrent linked list for presents
        ConcurrentLinky presentList = new ConcurrentLinky();
        // use a blockingqueue for an efficient threadsafe queue
        BlockingQueue<Integer> presents = new LinkedBlockingQueue<>();
        BlockingQueue<Integer> thankYou = new LinkedBlockingQueue<>();
        List<Integer> temp = new ArrayList<>(); // temp to randomize presents

        // randomize and offer presents to the blocking queue
        for (int i = 1; i <= 500000; i++)
            temp.add(i);
        Collections.shuffle(temp);
        for (Integer n : temp)
            presents.offer(n);

        // Create 4 threads for this problem test
        Thread [] servants = new Thread[4];
        for (int i = 0; i < 4; i++)
        {
            servants[i] = new Thread(new ServantAction(presentList, presents, thankYou));
        }

        for (Thread t : servants)
        {
            t.start();
        }

        for (Thread t : servants)
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
        System.out.println("Total presents: " + (presents.size() + thankYou.size()));
        System.out.println("Total thank you notes: " + thankYou.size());
    }
}


class ServantAction implements Runnable
{
    private ConcurrentLinky presentList;
    private BlockingQueue<Integer> presents;
    private BlockingQueue<Integer> thankYou;

    public ServantAction(ConcurrentLinky presentList, BlockingQueue<Integer> presents, BlockingQueue<Integer> thankYou)
    {
        this.presentList = presentList;
        this.presents = presents;
        this.thankYou = thankYou;
    }

    @Override
    public void run()
    {
        boolean go = true;
        while (go)
        {
            // keep going until both presents and thank yous are empty, that means we're done!
            synchronized (presents)
            {
                if (presents.isEmpty() && presentList.isEmpty())
                    break;
            }
            // make a random choice for the servant action
            int choice = ThreadLocalRandom.current().nextInt(1, 4);
            if (choice == 1)
            {
                // get the next tag, if it exists
                Integer tag = presents.poll();
                if (tag != null)
                    presentList.add(tag);
            }
            else if (choice == 2)
            {
                // if there was something to remove, add -1 to thankYous
                if (presentList.remove()) {
                    synchronized (thankYou) {
                        thankYou.offer(-1);
                    }
                }

            }
            // verify that the present exists
            else if (choice == 3)
            {
                int tag = ThreadLocalRandom.current().nextInt(1, 500001);
                presentList.search(tag);
            }
        }
    }
}

// LINKED LIST
class Present
{
    public int tag;
    public Present next;
    public Lock lock;
    public Present(int tag)
    {
        this.tag = tag;
        this.next = null;
        this.lock = new ReentrantLock();
    }
}

// Fine-grained concurrent linked list impelemntation with slight modification
// to remove method. Instead of deleting by value, we delete the first value (excluding dummy head)
class ConcurrentLinky
{
    private Present head;
    private Lock lock = new ReentrantLock();

    public ConcurrentLinky()
    {
        head = new Present(Integer.MIN_VALUE);
        head.next = new Present(Integer.MAX_VALUE);
    }

    public boolean add(int tag)
    {
        head.lock.lock();
        Present prev = head;
        try
        {
            Present curr = prev.next;
            curr.lock.lock();
            try
            {
                while (curr.tag < tag)
                {
                    prev.lock.unlock();
                    prev = curr;
                    curr = curr.next;
                    curr.lock.lock();
                }
                if (curr.tag == tag)
                {
                    return false;
                }
                Present newPresent = new Present(tag);
                newPresent.next = curr;
                prev.next = newPresent;
                return true;
            }
            finally
            {
                curr.lock.unlock();
            }
        }
        finally
        {
            prev.lock.unlock();
        }
    }

    public boolean remove()
    {
        head.lock.lock();
        try
        {
            Present first = head.next;
            first.lock.lock();
            try
            {
                if (first.tag != Integer.MAX_VALUE)
                {
                    head.next = first.next;
                    return true;
                }
                return false;
            }
            finally
            {
                first.lock.unlock();
            }
        }
        finally
        {
            head.lock.unlock();
        }
    }

    public boolean search(int tag) 
    {
        head.lock.lock();
        Present prev = head;
        try 
        {
            Present curr = prev.next;
            curr.lock.lock();
            try 
            {
                while (curr.tag < tag) 
                {
                    prev.lock.unlock();
                    prev = curr;
                    curr = curr.next;
                    curr.lock.lock();
                }
                return curr.tag == tag;
            } 
            finally 
            {
                curr.lock.unlock();
            }
        } 
        finally 
        {
            prev.lock.unlock();
        }
    }

    public boolean isEmpty() 
    {
        head.lock.lock();
        try 
        {
            Present firstElement = head.next;
            firstElement.lock.lock();
            try 
            {
                return firstElement.tag == Integer.MAX_VALUE;
            } 
            finally 
            {
                firstElement.lock.unlock();
            }
        } 
        finally 
        {
            head.lock.unlock();
        }
    }
}