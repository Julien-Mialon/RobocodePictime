package com.company;

import robocode.*;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello world!");

        var x = "plop";

        TesterCDouter a = new TesterCDouter();

        var alpha = a.FindA(1, 5, 2, 7);
        var beta = a.FindB(alpha, 1, 5);

        System.out.println("A = " + alpha + " B = " + beta);

        System.out.println();
	// write your code here
    }
}
