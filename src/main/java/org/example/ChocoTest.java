package org.example;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

public class ChocoTest {
    public static void main(String[] args) {
        int n = 8;
        Model model = new Model();
        IntVar[] x = model.intVarArray("x", 8, 0, 100);
        IntVar[] b1 = model.intVarArray("b1", 8, 0, 1);
        IntVar[] b2 = model.intVarArray("b2", 8, 0, 1);
        IntVar[] b3 = model.intVarArray("b3", 8, 0, 1);

        String op = "="; // among ">=", ">", "<=", "<", "="
        // and "!="
        IntVar[] vectorMul1 = new IntVar[8];
        IntVar[] vectorMul2 = new IntVar[8];
        IntVar[] vectorMul3 = new IntVar[8];
        for (int i = 0; i < x.length; i++) {
            vectorMul1[i] = x[i].mul(b1[i]).intVar();
            vectorMul2[i] = x[i].mul(b2[i]).intVar();
            vectorMul3[i] = x[i].mul(b3[i]).intVar();
        }
        model.sum(x, "<=", 100).post();
        model.sum(vectorMul1, "=", 80).post();
        model.sum(vectorMul2, "=", 50).post();
        model.sum(vectorMul3, "=", 70).post();
        model.sum(b1, "=", 4).post();
        model.sum(b2, "=", 3).post();
        model.sum(b3, "=", 3).post();
        Solver solver = model.getSolver();
        if (solver.solve()) {
            // do something, e.g. print out variable values
            for (int i = 0; i < x.length; i++) {
                System.out.println(x[i].getValue() + "\t" + b1[i].getValue()+ "\t" + b2[i].getValue()+ "\t" + b3[i].getValue());
            }
        }
    }
}
