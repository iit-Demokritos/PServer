/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pserver.algorithms.graphs;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 *
 * @author alexm
 */
public class MaximalCliques {

    private double[][] adjMatrix;
    LinkedList<Set<Integer>> cliques;
    private int breakNumCliques;

    public MaximalCliques(double[][] adjMatrix) {
        this.adjMatrix = adjMatrix;
        cliques = new LinkedList<Set<Integer>>();
        breakNumCliques = 0;
    }

    public Collection<Set<Integer>> getMaximalCliques() {
        LinkedList<Integer> R = new LinkedList<Integer>();
        LinkedList<Integer> P = new LinkedList<Integer>();
        for (int i = 0; i < this.adjMatrix.length; i++) {
            P.add(i);
        }
        LinkedList<Integer> X = new LinkedList<Integer>();
        System.out.println("calling new algorithm");
        BK(R, P, X);
        R = null;
        P = null;
        X = null;
        System.gc();
        System.out.println("hola it works");
        return this.cliques;
    }

    private void BK(LinkedList<Integer> R, LinkedList<Integer> P, LinkedList<Integer> X) {        
        if (P.size() == 0 && X.size() == 0) {
            Set<Integer> clique = new HashSet<Integer>(R.size());
            for (int node : R) {
                clique.add(node);
            }
            this.cliques.add(clique);
            System.out.println( " new clique with size " + clique.size() );
            System.out.println( " now i have " + this.cliques.size() );
            //System.gc();            
        } else {
            int max = 0;
            int degree = 0;
            for( int n : P ){
                int newDegree = 0;
                for( int f = 0 ; f < this.adjMatrix.length ; f ++ ){
                    if( this.adjMatrix[ n ][ f ] == 1 ){
                        newDegree ++;
                    }
                    if( degree < newDegree ) {
                        max = n;
                        degree = newDegree;
                    }
                }
            }
            Iterator<Integer> it = P.iterator();
            while( it.hasNext() ){
            //for ( Integer i : P ) {
                Integer i = it.next();
                if( this.adjMatrix[ i ][ max ] == 1 && i != max ) {
                    continue;
                }
                it.remove();
                LinkedList<Integer> Rnew = new LinkedList<Integer>(R);
                Rnew.add(i);
                LinkedList<Integer> Pnew = new LinkedList<Integer>();
                LinkedList<Integer> Xnew = new LinkedList<Integer>();
                for (int j = 0; j < this.adjMatrix.length; j++) {
                    if ( j != i && this.adjMatrix[i][j] == 1 ) {
                        //System.out.println("i has neigh " + j + "becouse abj is " + this.adjMatrix[i][j] );
                        if( P.contains( j ))
                            Pnew.add(j);
                        if( X.contains( j ) ){                            
                            Xnew.add(j);
                        }
                    }
                }
                BK(Rnew, Pnew, Xnew);
                Rnew = null;
                Pnew = null;
                Xnew = null;
                //System.gc();
                X.add(i);                
            }
        }
    }
}
