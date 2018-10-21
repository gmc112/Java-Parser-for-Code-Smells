import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.SourceRoot;
import com.github.javaparser.symbolsolver.resolution.SymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class BasicSmells {
    public static void main(String[] args) throws IOException {
        SourceRoot src = new SourceRoot(FileSystems.getDefault().getPath("Data"));
        List<ParseResult<CompilationUnit>> parseResults = src.tryToParse();
        for(ParseResult<CompilationUnit> p: parseResults) {
            if (p.isSuccessful()) {
                TypeSolver ts = new CombinedTypeSolver(new ReflectionTypeSolver(), new JavaParserTypeSolver(FileSystems.getDefault().getPath("Data" +
                        "")));
                JavaSymbolSolver symbolSolver = new JavaSymbolSolver(ts);
                JavaParser.getStaticConfiguration().setSymbolResolver(symbolSolver);
                CompilationUnit cu = p.getResult().get();
                for (Comment c : cu.getComments()) {       // Strips out comments from Comp. Unit
                    c.remove();
                }
                cu.accept(new LargeMethodVisitor(), null);
                cu.accept(new LargeClassVisitor(), null);
                cu.accept(new LongParListVisitor(), null);
                cu.accept(new SwitchTypeVisitor(), ts);
            }
        }
    }



    private static class LargeMethodVisitor extends VoidVisitorAdapter {
        public void visit(MethodDeclaration n, Object arg){
            super.visit(n, arg);
            if(n.getBody().isPresent()){
                BlockStmt b = n.getBody().get();
                if((b.findAll(Statement.class).size()-b.findAll(BlockStmt.class).size())>10){
                    System.out.println("-----------------------");
                    System.out.println(n.clone().removeBody());
                    System.out.println("This Method is too large");
                    System.out.println("-----------------------");
                }
            }

            }
        }

    private static class LargeClassVisitor extends VoidVisitorAdapter {
        private int count = 0;
        public void visit(ClassOrInterfaceDeclaration n, Object arg){
            super.visit(n, arg);

            for(FieldDeclaration f: n.findAll(FieldDeclaration.class)){
                count = count + f.getVariables().size();
            }
            count = count + (n.findAll(Statement.class).size()-n.findAll(BlockStmt.class).size());

            if(count>100){
                System.out.println("----------------------------------------------------------------- ");
                System.out.println(n.getName());
                System.out.println("This Class is too long");
                System.out.println("-----------------------------------------------------------------");
            }
        }
    }
    private static class LongParListVisitor extends VoidVisitorAdapter{
        public void visit(MethodDeclaration n, Object arg){
            super.visit(n, arg);
            if(n.findAll(Parameter.class).size()>5) {
                System.out.println("-----------------------------------");
                System.out.println(n.clone().removeBody());
                System.out.println("This method has too many parameters");
                System.out.println("-----------------------------------");
            }
        }
    }

    private static class SwitchTypeVisitor extends VoidVisitorAdapter{
        @Override
        public void visit(MethodDeclaration n, Object arg) {
            super.visit(n, arg);
            TypeSolver ts = (TypeSolver) arg;
            List<SwitchStmt> ls = n.findAll(SwitchStmt.class);
            if(!ls.isEmpty()){
                for(SwitchStmt s: ls){
                    System.out.println("----------------------------------------");
                    System.out.println(n.clone().removeBody());
                    System.out.println("This method has a switch statement of type " + JavaParserFacade.get(ts).getType(s.getSelector()).describe());
                    System.out.println("----------------------------------------");
                }

            }

        }
    }

}
