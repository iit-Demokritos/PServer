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

package pserver.domain;

import java.util.*;

public class PFeatureGroup {
    //sto anisma afto apothikebode i xristes pou anikoun stin kinotita    

    private LinkedList<String> features = null;
    private LinkedList<String> users = null;
    //to onoma tis kinotitas
    private String name = null;

    public PFeatureGroup() {
        name = "";
        features = new LinkedList<String>();
        users = new LinkedList<String>();
    }

    public PFeatureGroup(String name) {
        this.name = name;
        features = new LinkedList<String>();
        users = new LinkedList<String>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public LinkedList<String> getFeatures() {
        return features;
    }

    public void setFeatures(LinkedList<String> features) {
        this.features = features;
    }

    public LinkedList<String> getUsers() {
        return users;
    }

    public void setUsers(LinkedList<String> users) {
        this.users = users;
    }

    public void addFeature(String ftr) {
        this.features.add(ftr);
    }

    public void addUser(String user) {
        this.users.add(user);
    }
}
