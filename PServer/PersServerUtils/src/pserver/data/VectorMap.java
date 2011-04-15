//===================================================================
// ArrayListMap
//
// A general-purpose class.
//
// A ArrayList implementation that maps keys to values. Each key is
// mapped to one value, and duplicate keys are allowed. In contrast
// to hash table, this class is intented to be used for sequential
// access of its key elements (commonly in a loop).
//===================================================================
package pserver.data;

import java.util.*;

public class VectorMap {
    private int capacityIncrement;
    private int capacity;
    private ArrayList<Object> keys;
    private ArrayList<Object> vals;
    
    //initializers
    public VectorMap(int initialCapacity) {
        //capacity increment is zero
        capacityIncrement = 1;
        capacity = initialCapacity;
        keys = new ArrayList<Object>(initialCapacity);
        vals = new ArrayList<Object>(initialCapacity);
    }
    public VectorMap(int initialCapacity, int capacityIncrement) {
        //capacity increment specifies step that ArrayList grows
        this.capacityIncrement = capacityIncrement;
        capacity = initialCapacity;
        keys = new ArrayList<Object>(initialCapacity);
        vals = new ArrayList<Object>(initialCapacity);
    }
    
    public int size() {
        return keys.size();
    }
    public boolean isEmpty() {
        return keys.isEmpty();
    }
    
    //insert, update, remove, query methods
    public void add(Object key, Object val) {
        //may fail if capacity = size and increment is 0
        if( this.keys.size() == capacity ) {
            keys.ensureCapacity( keys.size() + capacityIncrement );
            vals.ensureCapacity( keys.size() + capacityIncrement );
            capacity += capacityIncrement;
        }
        keys.add(key);
        vals.add(val);
    }
    public boolean updateKey(Object newKey, int idx) {
        //returns false if idx out of size() bounds
        try {
            keys.set(idx , newKey );
        } catch(ArrayIndexOutOfBoundsException e) {
            return false;
        }
        return true;
    }
    public boolean updateVal(Object newVal, int idx) {
        //returns false if idx out of size() bounds
        try {
            vals.set(idx, newVal);
        } catch(ArrayIndexOutOfBoundsException e) {
            return false;
        }
        return true;
    }
    public boolean remove(int idx) {
        //returns false if idx out of size() bounds
        try {
            keys.remove(idx);
            vals.remove(idx);
        } catch(ArrayIndexOutOfBoundsException e) {
            return false;
        }
        return true;
    }
    public void clear() {
        keys.clear();
        vals.clear();
    }
    public int indexOfKey(Object key, int startIdx) {
        //returns -1 if key not exist
        for( int i = startIdx ; i < keys.size() ; i ++ ) {
            if( keys.get(i).equals(key) )
                return i;
        }
        return -1;
    }
    public int indexOfVal(Object val, int startIdx) {
        //returns -1 if val not exist
        for( int i = startIdx ; i < vals.size() ; i ++ ) {
            if( vals.get(i).equals(val) )
                return i;
        }
        return -1;
    }
    public Object getKey(int idx) {
        //returns null if idx out of size() bounds
        Object key;
        try {
            key = keys.get(idx);
        } catch(ArrayIndexOutOfBoundsException e) {
            key = null;
        }
        return key;
    }
    public Object getVal(int idx) {
        //returns null if idx out of size() bounds
        Object val;
        try {
            val = vals.get(idx);
        } catch(ArrayIndexOutOfBoundsException e) {
            val = null;
        }
        return val;
    }
    
    //misc methods
    public void trimToSize() {
        //Trims the capacity to be the current size
        keys.trimToSize();
        vals.trimToSize();
    }
    
    public int qpIndexOfKeyNoCase(String key) {
        //performs a case independent search for
        //first occurence of 'key' in 'queryParam' keys
        //returns index, or -1 if 'key' not found
        int idx = indexOfKey(key.toLowerCase(), 0);
        if (idx == -1)
            idx = indexOfKey(key.toUpperCase(), 0);
        return idx;
    }
}