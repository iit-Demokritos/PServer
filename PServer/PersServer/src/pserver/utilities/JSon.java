
package pserver.utilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;



    /**
     * Class used as wrapper for Gson with data types that aren't in this package
     * JSonizable interface is implemented for the other classes
     */
public class JSon {
    
    /**
     * Static attribute, available to all callers. Disables Html Escaping by default.
     */
    public static Gson json = new GsonBuilder().disableHtmlEscaping().create();
     
    /**
     * Returns a String of the JSON format of the object
     * 
     * @param object any object to be converted.
     * @param classOfT the class that the object belongs to
     * @return JSON format of object.
     */
    public static String jsonize(Object object, Class<? extends Object> classOfT){
    
        return json.toJson(object, classOfT);
    
    } 

    /**
     * Returns an instance of the Object relevant to the JSON string
     * 
     * @param jsonstring the String in json format to be converted.
     * @param classOfT the template the object corresponds to.
     * @return instance of object corresponding to the JSON String.
     */
    public static <T> T unjsonize(String jsonstring, Class<T> classOfT){
    
        return json.fromJson(jsonstring, classOfT);
    
    }
    
    /**
     * Use to jsonize an object '@Exposed' attributes only.
     * @param object any object to be converted.
     * @param classOfT the class that the object belongs to
     * @return JSON format of object.     
     */
    public static String jsoniseExposedOnly(Object object, Class<? extends Object> classOfT) {
        
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        
        return gson.toJson(object, classOfT);
        
    }
}
