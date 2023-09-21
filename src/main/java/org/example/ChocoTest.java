package org.example;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.example.solver.TopoGraph;

import java.util.List;
import java.util.Queue;

public class ChocoTest {
    public static void main(String[] args) {
        Model model = new Model();
        IntVar[] x = model.intVarArray("x", 10, 0, 7);
        IntVar[] b1 = model.intVarArray("b1", 10, 0, 1);
        IntVar[] b2 = model.intVarArray("b2", 10, 0, 1);
        IntVar[] b3 = model.intVarArray("b3", 10, 0, 1);
        IntVar[] b4 = model.intVarArray("b4", 10, 0, 1);
        IntVar[] b5 = model.intVarArray("b5", 10, 0, 1);
        IntVar[] b6 = model.intVarArray("b6", 10, 0, 1);
        IntVar[] b7 = model.intVarArray("b7", 10, 0, 1);
        IntVar[] b8 = model.intVarArray("b8", 10, 0, 1);
        IntVar[] b9 = model.intVarArray("b9", 10, 0, 1);

        String op = "="; // among ">=", ">", "<=", "<", "="
        IntVar[] vectorMul1 = new IntVar[10];
        IntVar[] vectorMul2 = new IntVar[10];
        IntVar[] vectorMul3 = new IntVar[10];
        IntVar[] vectorMul4 = new IntVar[10];
        IntVar[] vectorMul5 = new IntVar[10];
        IntVar[] vectorMul6 = new IntVar[10];
        IntVar[] vectorMul7 = new IntVar[10];
        IntVar[] vectorMul8 = new IntVar[10];
        IntVar[] vectorMul9 = new IntVar[10];

        for (int i = 0; i < x.length; i++) {
            vectorMul1[i] = x[i].mul(b1[i]).intVar();
            vectorMul2[i] = x[i].mul(b2[i]).intVar();
            vectorMul3[i] = x[i].mul(b3[i]).intVar();
            vectorMul4[i] = x[i].mul(b4[i]).intVar();
            vectorMul5[i] = x[i].mul(b5[i]).intVar();
            vectorMul6[i] = x[i].mul(b6[i]).intVar();
            vectorMul7[i] = x[i].mul(b7[i]).intVar();
            vectorMul8[i] = x[i].mul(b8[i]).intVar();
            vectorMul9[i] = x[i].mul(b9[i]).intVar();
        }
        model.sum(x, "<=", 7).post();
        model.sum(vectorMul1, "=", 5).post();
        model.sum(vectorMul2, "=", 2).post();
        model.sum(vectorMul3, "=", 2).post();
        model.sum(vectorMul4, "=", 1).post();
        model.sum(vectorMul5, "=", 4).post();
        model.sum(vectorMul6, "=", 2).post();
        model.sum(vectorMul7, "=", 1).post();
        model.sum(vectorMul8, "=", 1).post();
        model.sum(vectorMul9, "=", 1).post();

        //addExclusivePredicateInLikeModel(model, b1, b5);
        addExclusivePredicateInLikeModel(model, b2, b8);
        addExclusivePredicateInLikeModel(model, b2, b3);
        addExclusivePredicateInLikeModel(model, b3, b8);
        addExclusivePredicateInLikeModel(model, b6, b7);

        addInclusivePredicateInLikeModel(model, b1, b2);
        addInclusivePredicateInLikeModel(model, b1, b8);
        addInclusivePredicateInLikeModel(model, b1, b3);
        addInclusivePredicateInLikeModel(model, b2, b4);
        addInclusivePredicateInLikeModel(model, b5, b6);
        addInclusivePredicateInLikeModel(model, b5, b7);
        addInclusivePredicateInLikeModel(model, b6, b9);
        Solver solver = model.getSolver();
        if (solver.solve()) {
            // do something, e.g. print out variable values
            for (int i = 0; i < x.length; i++) {
                System.out.println(x[i].getValue() + "\t" + b1[i].getValue() + "\t" + b2[i].getValue() + "\t" + b3[i].getValue() + "\t" + b4[i].getValue() + "\t" + b5[i].getValue() + "\t" + b6[i].getValue() + "\t" + b7[i].getValue() + "\t" + b8[i].getValue() + "\t" + b9[i].getValue() + "\t");
            }
        }
    }

    public static void addInclusivePredicateInLikeModel(Model model, IntVar[] a, IntVar[] b) {
        IntVar[] iMulJ = new IntVar[10];
        for (int i = 0; i < 10; i++) {
            iMulJ[i] = a[i].mul(b[i]).intVar();
        }
        for (int i = 0; i < 10; i++) {
            model.arithm(iMulJ[i], "=", b[i]).post();
        }
    }

    public static void addExclusivePredicateInLikeModel(Model model, IntVar[] a, IntVar[] b) {
        IntVar[] iMulJ = new IntVar[10];
        for (int i = 0; i < 10; i++) {
            iMulJ[i] = a[i].mul(b[i]).intVar();
        }
        model.sum(iMulJ, "=", 0).post();
    }
}
