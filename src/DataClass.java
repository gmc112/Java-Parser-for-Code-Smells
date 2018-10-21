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
        SourceRoot src = new SourceRoot(FileSystems.getDefault().getPath("Data"));
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
            } else{
                int fieldCount = 0;
                for(FieldDeclaration field: n.getFields())
                    fieldCount = fieldCount + field.getVariables().size();
                int getterCount = 0;
                int setterCount = 0;
                boolean flag = true;
                for(MethodDeclaration m: n.findAll(MethodDeclaration.class)){
                    if(isGet(m)) {
                        getterCount++;
                    } else if(isSet(m)) {
                        setterCount++;
                    } else if(!isToString(m) || !m.isDefault() || !isEquals(m) || !isHashCode(m)){
                        flag = false;
                        break;
                    }

                }
                if((getterCount<=fieldCount&&setterCount<=fieldCount) && flag) {
                    System.out.println(n.getName());
                    System.out.println("This is a Data Class");
                }
            }

            List<ClassOrInterfaceDeclaration> lc = n.findAll(ClassOrInterfaceDeclaration.class);
            lc.remove(n);
            for(ClassOrInterfaceDeclaration c: lc){
                super.visit(c, null);
            }


        }
        private boolean isToString(MethodDeclaration n){
            return n.getNameAsString().equals("toString");
        }
        private boolean isHashCode(MethodDeclaration n){
            return n.getNameAsString().equals("hashCode");
        }
        private boolean isEquals(MethodDeclaration n){
            return n.getNameAsString().equals("equals");
        }
        private boolean isGet(MethodDeclaration n) {
            if(n.getParameters().size()==0)
                if(n.getBody().isPresent()){
                    List<Statement> ls = n.getBody().get().findAll(Statement.class);
                    ls.remove(0);
                    if (ls.size() != 1)
                        return false;
                    return ls.get(0).isReturnStmt();
                }
            return false;
        }

        private boolean isSet(MethodDeclaration n){
            if(n.getBody().isPresent()) {
                List<Parameter> lp = n.getParameters();
                if(lp.size()== 1){
                    int i = 0;
                    List<Statement> ls = n.getBody().get().findAll(Statement.class);
                    ls.remove(0);
                    for (Statement s : ls) {
                        if (i <= lp.size()) {
                            if (!s.isExpressionStmt()) {
                                return false;
                            } else {
                                Expression e = s.asExpressionStmt().getExpression();
                                if (!e.isAssignExpr())
                                    return false;
                            }
                            i++;
                        }
                    }
                    return true;
                }
            }
            return false;
        }
    }
}
