package org.example;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.impl.BitsetArrayIntVarImpl;
import org.chocosolver.solver.variables.impl.BoolVarImpl;

public class ChocoTestBool {
    public static void main(String[] args) {
        Model model = new Model();
        //BoolVar tmp = model.boolVar();
        IntVar[] a = model.intVarArray("a", 4, 0, 1);
        IntVar[] b = model.intVarArray("b", 4, 0, 1);
//        IntVar[] c = model.intVarArray("x", 4, 0, 1);
        IntVar suma = a[0];
        for (int i = 1; i < a.length; i++) {
            suma = suma.add(a[i]).intVar();
        }
        model.arithm(suma,"=",2).post();
        IntVar sumb = b[0];
        for (int i = 1; i < b.length; i++) {
            sumb = sumb.add(b[i]).intVar();
        }
        model.arithm(suma,">",sumb).post();
//        model.sum(a, "=", 3).post();
//        model.sum(b, "=", 2).post();
//        model.sum(c, "=", 1).post();
//        IntVar[] mul = new IntVar[a.length];
//        for (int i = 0; i < a.length; i++) {
//            mul[i] = a[i].mul(b[i]).intVar();
//        }
//        for (int i = 0; i < mul.length; i++) {
//            model.arithm(mul[i], "=", b[i]).post();
//        }
//        //测试，对c和a加包含约束
//        IntVar[] mulac = new IntVar[a.length];
//        for (int i = 0; i < mulac.length; i++) {
//            mulac[i] = a[i].mul(c[i]).intVar();
//        }
//        for (int i = 0; i < mulac.length; i++) {
//            Constraint contain = model.arithm(mulac[i], "=", c[i]);
//            Constraint distinct = model.arithm(mulac[i], "=", 0);
//            contain.impliedBy(tmp);
//            distinct.implies(tmp.not());
//        }
//        //对bc添加约束
//        BoolVar tmp1 = model.boolVar();
//        IntVar[] mulbc = new IntVar[a.length];
//        for (int i = 0; i < mulbc.length; i++) {
//            mulbc[i] = b[i].mul(c[i]).intVar();
//        }
//        for (int i = 0; i < mulbc.length; i++) {
//            Constraint contain = model.arithm(mulbc[i], "=", c[i]);
//            Constraint distinct = model.arithm(mulbc[i], "=", 0);
//            contain.impliedBy(tmp1);
//            distinct.implies(tmp1.not());
//        }
        Solver solver = model.getSolver();
        int all = 0;
        while (solver.solve()) {
//            boolean distinct = true;
//            for (int i = 0; i < a.length; i++) {
//                if (a[i].getValue() * c[i].getValue() != 0) {
//                    distinct = false;
//                }
//            }
//            if (distinct) {
            for (IntVar integers : a) {
                System.out.println(integers.getValue());
            }
            System.out.println("_________________");
            for (IntVar integers : b) {
                System.out.println(integers.getValue());
            }
//            System.out.println("_________________");
//            for (IntVar integers : c) {
//                System.out.println(integers.getValue());
//            }
            System.out.println("__________________________________");
//            }
            all++;
        }
        System.out.println("all:" + all);
//        System.out.println("distinct:" + dis);
    }
}
