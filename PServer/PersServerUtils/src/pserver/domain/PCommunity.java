/*
 * Community.java
 *
 * Created on 21 Σεπτέμβριος 2005, 9:07 μμ
 */

package pserver.domain;
import java.util.*;

public class PCommunity {
    //sto anisma afto apothikebode i xristes pou anikoun stin kinotita
    private ArrayList<String> users=null;
    private HashMap<String,Float> features=null;
    //to onoma tis kinotitas
    private String name=null;
    
    public PCommunity() {
        name="";
        users=new ArrayList<String>();
        features=new HashMap<String, Float>();
    }
    
    public PCommunity(String name){
        this.name=name;
        users=new ArrayList<String>();
        features=new HashMap<String, Float>();
    }
    
    public void setName(String name){
        this.name=name;
    }
    
    public String getName(){
        return name;
    }
    
    public void addUser(String user){
        users.add(user);
    }
    
    public ArrayList getUsers(){
        return users;
    }
    
    public String getUser(int index){
        return (String)users.get(index);
    }
    
    public boolean containsUser(String user){
        return users.contains(user);
    }
    
    public int getCommunityNumberOfUsers(){
        return users.size();
    }
   
    
    public void addFeature(String feature,float value){
        features.put(feature,new Float(value));
        
    }
    
    public HashMap getFeatures(){
        return features;
    }
    
    public boolean containsFeature(String feature){
        return features.containsKey(feature);
    }
    
    public int getNumberOfFeatures(){
        return features.size();
    }
    
    public String[] getFeatureNames(){
        return (String[])this.features.keySet().toArray();
    }
}
