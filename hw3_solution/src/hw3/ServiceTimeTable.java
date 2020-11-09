package hw3;

import java.util.Random;

/**
 * Created by ParkerStone on 10/17/20.
 */
public class ServiceTimeTable {
    private double [][] matrix;
    private final Random rand = new Random(System.nanoTime());

    ServiceTimeTable(double ... args) {
        matrix = new double[args.length / 2][2];
        int i = 0;
        int j = 0;
        for (double d : args) {
            matrix[i][j] = d;
            if (j == 1) {
                j = 0;
                i++;
            } else {
                j = 1;
            }
        }
    }

    public double getServiceTime() {
        double r = rand.nextDouble();
        double server3ServiceTime = 0.0;
        for (int i = 0; i < matrix.length; i++) {
            if (r < matrix[i][0]) {
                server3ServiceTime = matrix[i][1];
                break;
            }
        }
        return server3ServiceTime;
    }

    public Double getMinServiceTime() {
        double min = 999999999.0;
        for (int i = 0; i < matrix.length; i++) {
           min = Math.min(matrix[i][1], min);
        }
        return min;
    }
}
