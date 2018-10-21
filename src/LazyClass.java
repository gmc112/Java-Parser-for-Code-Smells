import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.SourceRoot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.List;
import java.util.Optional;

public class LazyClass {
    public static void main(String[] args) throws IOException {
        SourceRoot src = new SourceRoot(FileSystems.getDefault().getPath("Data"));
        List<ParseResult<CompilationUnit>> parseResults = src.tryToParse();

        for(ParseResult<CompilationUnit> p: parseResults)
            if(p.isSuccessful()) {
                CompilationUnit cu = p.getResult().get();
                for (Comment c : cu.getComments()) {       // Strips out comments from Comp. Unit
                    c.remove();
                }
                cu.accept(new LazyClass.LazyClassVisitor(), null);
            }
    }

    private static class LazyClassVisitor extends VoidVisitorAdapter {
        public void visit(ClassOrInterfaceDeclaration n, Object arg){
            super.visit(n, arg);
            if(n.isInterface())
                return;
            if (hasComplexStm(n)){
                return;
            } else if(!checkMethods(n)){
                System.out.println("-----------------------");
                System.out.println(n.getName());
                System.out.println("This is a Lazy Class");
                System.out.println("-----------------------");
            }
        }
        private boolean checkMethods(ClassOrInterfaceDeclaration n){
            List<MethodDeclaration> lm = n.findAll(MethodDeclaration.class);       // Does it have no methods excluding constructors
            for(MethodDeclaration m: lm)
                if(m.isDefault())
                    lm.remove(m);
            return lm.size()>4;
        }
        private boolean hasComplexStm(ClassOrInterfaceDeclaration n) {
            for(MethodDeclaration m: n.findAll(MethodDeclaration.class)){
                m = m.clone();
                m.remove(m);
                if(!m.isDefault()){
                    int b = m.findAll(BlockStmt.class).size();
                    if(m.findAll(Statement.class).size()-b>4 && b!=0)
                        return true;
                }
            }
            return false;
        }

    }
}
