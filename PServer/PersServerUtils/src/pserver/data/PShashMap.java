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

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Panagiotis Giotis <giotis.p@gmail.com>
 */
public class PShashMap<K, V> extends HashMap<K, V> {

    private HashMap<String, String> regexMap = new HashMap<String, String>();

    public PShashMap() {
    }

    public PShashMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

    public PShashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public PShashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    @Override
    public V put(K key, V value) {
        String newKey = "";
        //if key is restfull clear it from the variables
        if (key.toString().contains(".")) {
            String[] temp = key.toString().split("/");
            int count = 1;

            for (String s : temp) {

                if (count == 1) {
                    newKey = newKey + "(\\d+)\\.(\\d+)/";


                } else if (s.startsWith(":")) {

                    if (count < temp.length) {
                        newKey = newKey + "(\\S+)/";

                    } else {
                        newKey = newKey + "(\\S+)";

                    }

                } else {
                    if (count < temp.length) {
                        newKey = newKey + s + "/";

                    } else {
                        newKey = newKey + s;

                    }
                }

                count++;
            }
            newKey = newKey + "\\.(xml|json)";
            regexMap.put(newKey, (String) key);
//            debugline
//System.out.println(regexMap.keySet());
//System.out.println(regexMap.values());
//System.out.println(value.toString());
            key = (K) newKey;
            return super.put(key, value);

        } else {//else forward to super
            newKey = "^" + key + "$";
            regexMap.put(newKey, (String) key);
            key = (K) newKey;
            return super.put(key, value);

        }

    }

    //override the method and catch regex style.
    @Override
    public boolean containsKey(Object key) {

        //Check if key is restfull

        boolean rvalue = true;

        for (K temp : keySet()) {

            Pattern regexPattern = Pattern.compile(temp.toString());
            Matcher matcher = regexPattern.matcher(key.toString());

            if (matcher.find()) {

                rvalue = true;
                break;

            } else {

                rvalue = false;

            }

        }

        return rvalue;
    }

    @Override
    public V get(Object key) {
        //Check if key is restfull
        V KeyValue = null;

        for (K temp : keySet()) {

            Pattern regexPattern = Pattern.compile(temp.toString());
            Matcher matcher = regexPattern.matcher(key.toString());

            if (matcher.find()) {
                KeyValue = super.get(temp);
//                    debuglines
//                    System.out.println("++++++++  IN   +++++++++");
//                    System.out.println("regex-->" + temp.toString());
//                    System.out.println("key-->" + key.toString());
//                    System.out.println("++++++++  OUT   +++++++++");

            }
        }

        return KeyValue;
    }

    /**
     * Find from the given URL the variables.
     *
     * @param BaseURL The base URL.
     * @return A array with all rest variables.
     */
    public HashMap<String, String> getRestVariables(String BaseURL) {
        HashMap<String, String> RestVariables = new HashMap<String, String>();
        //impliment method which returns the VariablesPattern


        for (K temp : keySet()) {

            Pattern regexPattern = Pattern.compile(temp.toString());
            Matcher matcher = regexPattern.matcher(BaseURL);

            if (matcher.find()) {
                StringTokenizer parser = new StringTokenizer(regexMap.get(temp.toString()), " ", true);
//                int varCount = matcher.groupCount();
//                for (int i = 1; i <= varCount; i++) {
                parser.nextToken(":");
                RestVariables.put(parser.nextToken("/").substring(1), matcher.group(3));
//                }
                break;
            }
        }

        return RestVariables;
    }
}
