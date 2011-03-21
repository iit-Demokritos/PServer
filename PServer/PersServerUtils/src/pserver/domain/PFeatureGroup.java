package pserver.domain;
import java.util.*;

public class PFeatureGroup{
    //sto anisma afto apothikebode i xristes pou anikoun stin kinotita
    private Vector users=null;
    private Hashtable features=null;
    //to onoma tis kinotitas
    private String name=null;
    
    public PFeatureGroup() {
        name="";
        users=new Vector();
        features=new Hashtable();
    }
    
    public PFeatureGroup(String name){
        this.name=name;
        users=new Vector();
        features=new Hashtable();
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
    
    public Vector getUsers(){
        return users;
    }
    
    public String getUser(int index){
        return (String)users.get(index);
    }
    
    public void addFeature(String feature,boolean value){
        features.put(feature,new Boolean(value));
        
    }
    
    public Hashtable getFeatures(){
        return features;
    }
    
    public String getFeature(int index){
        return (String)features.get (index);
    }
    
    public boolean getFeature_value(String feature){
        return ((Boolean)features.get(feature)).booleanValue();
    }
    
    public boolean containsFeature(String feature){
        return features.contains(feature);
    }
    
    public int getNumberOfFeatures(){
        return features.size();
    }
    
    public String[] getFeatureNames(){
        Enumeration e=features.keys();
        Vector keys=new Vector();
        while(e.hasMoreElements()){
            keys.add((String)e.nextElement());
        }
        
        String[] keyNames=new String[keys.size()];
        for(int i=0;i<keyNames.length;i++){
            keyNames[i]=(String)keys.get(i);
        }
        return keyNames;
    }
}
