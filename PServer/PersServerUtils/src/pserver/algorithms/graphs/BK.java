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
package pserver.algorithms.graphs;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import pserver.data.DBAccess;
import pserver.data.GraphClusterManager;
import pserver.data.PServerResultSet;

/**
 *
 * @author alexm
 */
public class BK implements GraphClustering {

    private DBAccess dbAccess;
    private HashMap<String, ArrayList<String>> connections = null;
    private HashMap<String, Integer> nodes = null;
    private LinkedList<Set<String>> cliques;
    private String tmpStr;
    private int cl = 0;
    private GraphClusterManager graphClusterManager;

    public BK() {
        connections = new HashMap<String, ArrayList<String>>();
        nodes = new HashMap<String, Integer>();
        cliques = new LinkedList<Set<String>>();
    }

    public void execute(String querySql, GraphClusterManager graphClusterManager, DBAccess dbAccess) throws SQLException {
        this.dbAccess = dbAccess;
        this.graphClusterManager = graphClusterManager;
        PServerResultSet rs = this.dbAccess.executeQuery(querySql);

        while (rs.next()) {

            String obj1 = rs.getRs().getString(1);
            String obj2 = rs.getRs().getString(2);

            //System.out.println( obj1 + " " + obj2 );
            if (connections.get(obj1) == null) {
                this.connections.put(obj1, new ArrayList<String>());
            }
            if (connections.get(obj2) == null) {
                this.connections.put(obj2, new ArrayList<String>());
            }

            connections.get(obj1).add(obj2);
            connections.get(obj2).add(obj1);

            if (nodes.get(obj1) == null) {
                nodes.put(obj1, 1);
            } else {
                nodes.put(obj1, nodes.get(obj1) + 1);
            }

            if (nodes.get(obj2) == null) {
                nodes.put(obj2, 1);
            } else {
                nodes.put(obj2, nodes.get(obj2) + 1);
            }
        }

        rs.close();
        Set<String> nodeNames = nodes.keySet();

        //System.out.println( "\nTotal Num of nodes " + this.nodes.size() );
        for (String node : nodeNames) {
            //this.connections.get( node ).add( node );
            System.out.println("node " + node + " has " + this.connections.get(node).size() + " links");
        }
        if (nodeNames.size() == 0) {
            return;
        }
        getMaximalCliques();
    }

    public Collection<Set<String>> getMaximalCliques() throws SQLException {
        LinkedList<String> R = new LinkedList<String>();
        LinkedList<String> P = new LinkedList<String>();
        Set<String> nodeNames = nodes.keySet();
        P.addAll(nodeNames);
//        for ( String node : nodeNames ) {
//            P.add( node );
//        }
        LinkedList<String> X = new LinkedList<String>();
        System.out.println("\r\ncalling Bron-Kerbosch algorithm \r\n");
        runBK(R, P, X);
        R = null;
        P = null;
        X = null;
        System.out.println("Bron-Kerbosch Algorithm terminated\r\n");
        return this.cliques;
    }

    private int svar = 0;

    private void runBK(LinkedList<String> R, LinkedList<String> P, LinkedList<String> X) throws SQLException {
        if (P.size() == 0 && X.size() == 0) {
            Set<String> clique = new HashSet<String>(R.size());
            clique.addAll(R);
//            for (String node : R) {
//                clique.add(node);
//            }
            System.out.println("New clique num - " + (cl++) + " size " + clique.size());
            graphClusterManager.addGraphCluster(clique);
            svar = 0;
        } else {
            svar++;
            if (svar > this.connections.size()) {
                svar = 0;
                return;
            }
            String max = "";
            int degree = 0;
            for (String n : P) {
                int newDegree = this.connections.get(n).size();
                if (degree < newDegree) {
                    max = n;
                    degree = newDegree;
                }
            }
            Iterator<String> it = P.iterator();
            while (it.hasNext()) {
                String i = it.next();
                if (this.connections.get(i).contains(max) == true) {
                    continue;
                }
                it.remove();
                LinkedList<String> Rnew = new LinkedList<String>(R);
                Rnew.add(i);
                LinkedList<String> Pnew = new LinkedList<String>();
                LinkedList<String> Xnew = new LinkedList<String>();
                ArrayList<String> links = this.connections.get(i);
                for (String l : links) {
                    if (P.contains(l)) {
                        Pnew.add(l);
                    }
                    if (X.contains(l)) {
                        Xnew.add(l);
                    }
                }
                runBK(Rnew, Pnew, Xnew);
                Rnew = null;
                Pnew = null;
                Xnew = null;
                System.gc();
                X.add(i);
            }
        }
    }

}
