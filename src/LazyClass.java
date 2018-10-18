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
    private boolean flag = false;
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
        private boolean flag;
        private int fieldCount;
        private int getterCount;
        private int setterCount;
        public void visit(ClassOrInterfaceDeclaration n, Object arg){
            super.visit(n, arg);
            flag = false;
            fieldCount = n.getFields().size();
            getterCount = 0;
            setterCount = 0;
            if(n.isInterface())
                return;
            if(!hasMethods(n)){
                System.out.println(n);
                System.out.println("This is a Lazy Class");
            }
            else if (!hasNonComplexStm(n)){
                System.out.println(n);
                System.out.println("This is a Lazy Class");
                flag = false;
            }

            // ToDo: Add Method to check for getters and setters
            List<ClassOrInterfaceDeclaration> lc = n.findAll(ClassOrInterfaceDeclaration.class);
            lc.remove(n);
            for(ClassOrInterfaceDeclaration c: lc){
                super.visit(c, null);
                flag = false;
            }


        }

        private boolean hasNonComplexStm(ClassOrInterfaceDeclaration n) {
            n.findAll(MethodDeclaration.class).forEach(f->{
                if(!f.isDefault()){
                    f.findAll(Statement.class).forEach(g-> checkBlock(g));
                }
            });
            return flag;
        }

        private void checkBlock(Statement g){
            if(!g.isBlockStmt() && !g.isExpressionStmt() && !g.isReturnStmt()){
                setFlag();
            } else if(g.isBlockStmt()){
                BlockStmt b = g.asBlockStmt();
//                checkBlock(b);
            }
        }

        private void setFlag() {
            flag = true;
        }


        private boolean hasMethods(ClassOrInterfaceDeclaration n){
            List<MethodDeclaration> lm = n.findAll(MethodDeclaration.class);       // Does it have no methods excluding constructors
            for(MethodDeclaration m: lm)
                if(m.isDefault())
                    lm.remove(m);
            return !lm.isEmpty();
        }
    }
}
