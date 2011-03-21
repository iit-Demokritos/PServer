//===================================================================
// VectorMap
//
// A general-purpose class.
//
// A Vector implementation that maps keys to values. Each key is
// mapped to one value, and duplicate keys are allowed. In contrast
// to hash table, this class is intented to be used for sequential
// access of its key elements (commonly in a loop).
//===================================================================
package pserver.data;

import java.util.*;

public class VectorMap {
    private Vector keys;
    private Vector vals;
    
    //initializers
    public VectorMap(int initialCapacity) {
        //capacity increment is zero
        keys = new Vector(initialCapacity);
        vals = new Vector(initialCapacity);
    }
    public VectorMap(int initialCapacity, int capacityIncrement) {
        //capacity increment specifies step that vector grows
        keys = new Vector(initialCapacity, capacityIncrement);
        vals = new Vector(initialCapacity, capacityIncrement);
    }
    
    //class info methods
    public int capacity() {
        return keys.capacity();
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
        keys.add(key);
        vals.add(val);
    }
    public boolean updateKey(Object newKey, int idx) {
        //returns false if idx out of size() bounds
        try {
            keys.setElementAt(newKey, idx);
        } catch(ArrayIndexOutOfBoundsException e) {
            return false;
        }
        return true;
    }
    public boolean updateVal(Object newVal, int idx) {
        //returns false if idx out of size() bounds
        try {
            vals.setElementAt(newVal, idx);
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
        return keys.indexOf(key, startIdx);
    }
    public int indexOfVal(Object val, int startIdx) {
        //returns -1 if val not exist
        return vals.indexOf(val, startIdx);
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