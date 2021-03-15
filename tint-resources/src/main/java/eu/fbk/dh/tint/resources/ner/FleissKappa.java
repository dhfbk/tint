package eu.fbk.dh.tint.resources.ner;

import java.util.Arrays;

/**
 * Computes the Fleiss' Kappa value as described in (Fleiss, 1971)
 */
public class FleissKappa
{
    public static final boolean DEBUG = true ;

    /**
     * Exemple on this Wikipedia article data set
     */
    public static void main(String[] args)
    {
        int[][] mat = new int[][]
        {
            {0,0,0,0,14},
            {0,2,6,4,2},
            {0,0,3,5,6},
            {0,3,9,2,0},
            {2,2,8,1,1},
            {7,7,0,0,0},
            {3,2,6,3,0},
            {2,5,3,2,2},
            {6,5,2,1,0},
            {0,2,2,3,7}
        } ;

        float kappa = computeKappa(mat) ;
    }

    /**
     * Computes the Kappa value
     * @param mat Matrix[subjects][categories]
     * @return The Kappa value
     */
    public static float computeKappa(int[][] mat)
    {
        final int n = checkEachLineCount(mat) ;  // PRE : every line count must be equal to n
        final int N = mat.length ;
        final int k = mat[0].length ;

        if(DEBUG) System.out.println(n+" raters.") ;
        if(DEBUG) System.out.println(N+" subjects.") ;
        if(DEBUG) System.out.println(k+" categories.") ;

        // Computing p[]
        float[] p = new float[k] ;
        for(int j=0 ; j<k ; j++)
        {
            p[j] = 0 ;
            for(int i=0 ; i<N ; i++)
                p[j] += mat[i][j] ;
            p[j] /= N*n ;
        }
        if(DEBUG) System.out.println("p = "+Arrays.toString(p)) ;

        // Computing P[]
        float[] P = new float[N] ;
        for(int i=0 ; i<N ; i++)
        {
            P[i] = 0 ;
            for(int j=0 ; j<k ; j++)
                P[i] += mat[i][j] * mat[i][j] ;
            P[i] = (P[i] - n) / (n * (n - 1)) ;
        }
        if(DEBUG) System.out.println("P = "+Arrays.toString(P)) ;

        // Computing Pbar
        float Pbar = 0 ;
        for(float Pi : P)
            Pbar += Pi ;
        Pbar /= N ;
        if(DEBUG) System.out.println("Pbar = "+Pbar) ;

        // Computing PbarE
        float PbarE = 0 ;
        for(float pj : p)
            PbarE += pj * pj ;
        if(DEBUG) System.out.println("PbarE = "+PbarE) ;

        final float kappa = (Pbar - PbarE)/(1 - PbarE) ;
        if(DEBUG) System.out.println("kappa = "+kappa) ;

        return kappa ;
    }

    /**
     * Assert that each line has a constant number of ratings
     * @param mat The matrix checked
     * @return The number of ratings
     * @throws IllegalArgumentException If lines contain different number of ratings
     */
    private static int checkEachLineCount(int[][] mat)
    {
        int n = 0 ;
        boolean firstLine = true ;

        for(int[] line : mat)
        {
            int count = 0 ;
            for(int cell : line)
                count += cell ;
            if(firstLine)
            {
                n = count ;
                firstLine = false ;
            }
            if(n != count)
                throw new IllegalArgumentException("Line count != "+n+" (n value).") ;
        }
        return n ;
    }
}
