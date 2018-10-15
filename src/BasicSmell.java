import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

public class BasicSmell {
    public static void main(String[] args) throws IOException {
        FileInputStream in = new FileInputStream("Car.java");
        CompilationUnit cu;
        try {
            cu = JavaParser.parse(in);
        } finally {
            in.close();
        }

        cu.accept(new LargeMethodVisitor(), null);
    }

    private static class LargeMethodVisitor extends VoidVisitorAdapter {
        private int  count = 0;
        private void increment(){
            count++;
        }

        public void visit(MethodDeclaration n, Object arg){
            n.findAll(Statement.class).stream()
                    .forEach(f-> {
                        increment();
                    });
            if(count>10){
                System.out.println(n);
                System.out.println("This Method is too long");
            }
            count = 0;
            }
        }

    private static class LargeClassVisitor extends VoidVisitorAdapter {
        private int  count = 0;
        private void increment(){
            count++;
        }

        public void visit(ClassOrInterfaceDeclaration n, Object arg){
            n.findAll(Statement.class).stream()
                    .forEach(f-> {
                        increment();
                    });
            if(count>100){
                System.out.println(n);
                System.out.println("This Class is too long");
            }
            count = 0;
        }
    }

}
