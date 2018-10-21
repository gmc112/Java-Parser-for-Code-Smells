import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.SourceRoot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.List;

public class PrimitiveObsession {
    public static void main(String[] args) throws IOException {
        SourceRoot src = new SourceRoot(FileSystems.getDefault().getPath("Data"));
        List<ParseResult<CompilationUnit>> parseResults = src.tryToParse();
        for(ParseResult<CompilationUnit> p: parseResults) {
            if (p.isSuccessful()) {
                CompilationUnit cu = p.getResult().get();
                for (Comment c : cu.getComments()) {       // Strips out comments from Comp. Unit
                    c.remove();
                }

                cu.accept(new PrimitiveObsessionVisitor(), null);
            }
        }
    }

    private static class PrimitiveObsessionVisitor extends VoidVisitorAdapter {
        private int p;
        private int q;

        public void visit(MethodDeclaration n, Object arg){
            super.visit(n, arg);
            p=0;
            q=0;
            n.findAll(VariableDeclarator.class).
                    forEach(f-> {
                        if(f.getType().isPrimitiveType())
                            iterateP();
                        else
                            iterateQ();
                    });
            if(p>0)
                if(p/(p+q)>0.8 && p+q>3) {
                    System.out.println("---------------------------------");
                    System.out.println(n.clone().removeBody());
                    System.out.println("This method is Primitive Obsessed");
                    System.out.println("---------------------------------");
                }
        }
        private void iterateP(){
            p++;
        }

        private void iterateQ(){
            q++;
        }
    }
}
