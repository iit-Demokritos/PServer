/*
 * Copyright 2013 IIT , NCSR Demokritos - http://www.iit.demokritos.gr,
 *                            SciFY NPO - http://www.scify.org
 *
 * This product is part of the PServer Free Software.
 * For more information about PServer visit http://www.pserver-project.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *                 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * If this code or its output is used, extended, re-engineered, integrated,
 * or embedded to any extent in another software or hardware, there MUST be
 * an explicit attribution to this work in the resulting source code,
 * the packaging (where such packaging exists), or user interface
 * (where such an interface exists).
 *
 * The attribution must be of the form
 * "Powered by PServer, IIT NCSR Demokritos , SciFY"
 */

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
