import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.SourceRoot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemporaryField {
    public static void main(String[] args) throws IOException {
        SourceRoot src = new SourceRoot(FileSystems.getDefault().getPath("Data"));
        List<ParseResult<CompilationUnit>> parseResults = src.tryToParse();
        for(ParseResult<CompilationUnit> p: parseResults) {
            if (p.isSuccessful()) {
                CompilationUnit cu = p.getResult().get();
                for (Comment c : cu.getComments()) {       // Strips out comments from Comp. Unit
                    c.remove();
                }
                cu.accept(new TempFieldVisitor(), null);
            }
        }
    }

    private static class TempFieldVisitor extends VoidVisitorAdapter {
        public void visit(ClassOrInterfaceDeclaration n, Object arg) {
            super.visit(n, arg);
            List<MethodDeclaration> methods = n.findAll(MethodDeclaration.class);
            Map<VariableDeclarator, Integer> varMap = new HashMap<>();
            for (FieldDeclaration fd : n.getFields()) {
                for (VariableDeclarator vd : fd.getVariables()) {
                    varMap.put(vd, 0);
                }
            }
//            System.out.println(varMap);


            for (MethodDeclaration m : methods) {
//                System.out.println(m.get());
                for(Node node:m.getChildNodes()) {
//                    System.out.println(node);

                    for (VariableDeclarationExpr v : node.findAll(VariableDeclarationExpr.class)) {
                        for (VariableDeclarator vd : v.getVariables()) {
                            if (varMap.containsKey(vd)) {
                                varMap.put(vd, varMap.get(vd) + 1);
                            }
                        }

                    }
                }
            }
//            System.out.println(varMap);
            varMap.forEach((f, g)->{
                if(g<=1){
                    System.out.println(n.getName());
                    System.out.println(f);
                    System.out.println("This is a temporary field");
                    System.out.println("-------------------------");
                }
            });
        }
    }
}
