package pserver.data;

public class Barrier {

    protected int threshold;
    private int count = 0;

    public Barrier( int t ) {
        threshold = t;
        count = 0;
    }

    public void reset() {
        setCount(0);
    }

    public synchronized void down(){
        setCount(getCount() + 1);
        if ( getCount() == threshold ) {
            try {
                this.wait();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    public synchronized void up(){
        setCount(getCount() - 1);
        if ( getCount() < threshold ) {
            this.notify();
        }
    }

    public synchronized void waitForRelease()
            throws InterruptedException {
        setCount(getCount() + 1);
        //wait();
        if ( getCount() == threshold ) {
            notifyAll();
        } else {
            while ( getCount() < threshold ) {
                wait();
            }
        }
    }

    public void action() {
        System.out.println( "done" );
    }

    /**
     * @return the count
     */
    public synchronized int getCount() {
        return count;
    }

    /**
     * @param count the count to set
     */
    public void setCount(int count) {
        this.count = count;
    }
}
