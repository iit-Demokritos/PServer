/*
 * 
 */

package pserver.data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import pserver.domain.PUser;

/**
 *
 * @author Panagiotis Giotis <giotis.p@gmail.com>
 */
public class UserFeatureCache {
    Map<String,PUser> mCache;
    PUserDBAccess pdbDB;
            
    int iMaxSize = 100;

    public UserFeatureCache(int maxSize, PUserDBAccess db) {
        iMaxSize = maxSize;
        pdbDB = db;
        initCache();
    }
    
    public PUser getUser(String sUserID) {
        // if user already in cache
        if (mCache.containsKey(sUserID)) {
            // return user
            return mCache.get(sUserID);
        }
        
        // if not in cache
        // fetch from DB
        // put in cache
        // if cache is over the limit
            // remove an item
            // update last seen list
        // update last seen
        // return user
        return null;
    }
    
    public void initCache() {
        mCache = new HashMap<>();
    }
}
