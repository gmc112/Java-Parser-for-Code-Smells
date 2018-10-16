import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.FileInputStream;
import java.io.IOException;

public class PrimitiveObsession {
    public static void main(String[] args) throws IOException {
        FileInputStream in = new FileInputStream("Data/Bloaters/Bresenham.java");
        CompilationUnit cu;
        try {
            cu = JavaParser.parse(in);
        } finally {
            in.close();
        }
        for(Comment c: cu.getComments()){       // Strips out comments from Comp. Unit
            c.remove();
        }

        cu.accept(new PrimitiveObsession.PrimitiveObsessionVisitor(), null);
    }

    private static class PrimitiveObsessionVisitor extends VoidVisitorAdapter {
        public void visit(MethodDeclaration n, Object arg){
            n.findAll(VariableDeclarator.class).stream().
                    forEach(f->{
                        System.out.println(f.getType());
                    });
        }
    }
}
