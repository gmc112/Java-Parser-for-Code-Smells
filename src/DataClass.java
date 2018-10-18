import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
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

public class DataClass {
    private boolean flag = false;
    public static void main(String[] args) throws IOException {
        SourceRoot src = new SourceRoot(FileSystems.getDefault().getPath("Data\\Dispensibles\\a"));
        List<ParseResult<CompilationUnit>> parseResults = src.tryToParse();

        for(ParseResult<CompilationUnit> p: parseResults)
            if(p.isSuccessful()) {
                CompilationUnit cu = p.getResult().get();
                for (Comment c : cu.getComments()) {       // Strips out comments from Comp. Unit
                    c.remove();
                }

                cu.accept(new DataClass.DataClassVisitor(), null);
            }
    }

    private static class DataClassVisitor extends VoidVisitorAdapter {


        public void visit(ClassOrInterfaceDeclaration n, Object arg){
            super.visit(n, arg);

            if(n.isInterface()){
                return;
//            if(!hasMethods(n)){
//                System.out.println(n);
//                System.out.println("This is a Data Class");
//            }
//            else if (!hasNonGetSet(n)){
//                System.out.println(n);
//                System.out.println("This is a Data Class");
//                flag = false;
            } else{
                int fieldCount = 0;
                for(FieldDeclaration field: n.getFields())
                    fieldCount = fieldCount + field.getVariables().size();
//                System.out.println(n);
//                System.out.println(fieldCount);
                int getterCount = 0;
                int setterCount = 0;
                boolean flag = true;
                for(MethodDeclaration m: n.findAll(MethodDeclaration.class)){
                    System.out.println(m.clone().removeBody());
                    if(isGet(m)) {
                        getterCount++;
                    } else if(isSet(m)) {
                        setterCount++;
                    } else if(!isToString(m) || !m.isDefault()){
                        flag = false;
                        break;
                    }

                }
                System.out.println(getterCount);
                if((getterCount<=fieldCount&&setterCount<=fieldCount) && flag) {
                    System.out.println(n);
                    System.out.println("This is a Data Class");
                }
            }

            // ToDo: Add Method to check for getters and setters
            List<ClassOrInterfaceDeclaration> lc = n.findAll(ClassOrInterfaceDeclaration.class);
            lc.remove(n);
            for(ClassOrInterfaceDeclaration c: lc){
                super.visit(c, null);
            }


        }

//        private boolean hasNonGetSet(ClassOrInterfaceDeclaration n) {
//            n.findAll(MethodDeclaration.class).forEach(f->{
//                        if(!f.isDefault()){
//                            f.findAll(Statement.class).forEach(g-> checkBlock(g));
//                        }
//            });
//            return flag;
//        }

        private boolean isToString(MethodDeclaration n){
            return n.getNameAsString().equals("toString");
        }
        private boolean isGet(MethodDeclaration n) {
            List<Statement> ls = n.getBody().get().findAll(Statement.class);
            ls.remove(0);
            System.out.println(ls);
            if (ls.size() != 1)
                return false;

            return ls.get(0).isReturnStmt();
        }

        private boolean isSet(MethodDeclaration n){
            List<Parameter> lp = n.getParameters();
            int i = 0;
            List<Statement> ls = n.getBody().get().findAll(Statement.class);
            ls.remove(0);
            for(Statement s: ls) {

                if(i<=lp.size()) {
                    if (!s.isExpressionStmt()) {
                        return false;
                    } else{
                        Expression e = s.asExpressionStmt().getExpression();
                        if(!e.isAssignExpr())
                            return false;
                    }
                    i++;
                }
            }
            return true;
        }
//        private void checkBlock(Statement g){
//            if(!g.isBlockStmt() && !g.isExpressionStmt() && !g.isReturnStmt()){
//                setFlag();
//            } else if(g.isBlockStmt()){
//                BlockStmt b = g.asBlockStmt();
////                checkBlock(b);
//            }
//        }

//        private void setFlag() {
//            flag = true;
//        }


        private boolean hasMethods(ClassOrInterfaceDeclaration n){
            List<MethodDeclaration> lm = n.findAll(MethodDeclaration.class);       // Does it have no methods excluding constructors
            for(MethodDeclaration m: lm)
                if(m.isDefault())
                    lm.remove(m);
            return !lm.isEmpty();
        }
    }
}
