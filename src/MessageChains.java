import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import com.github.javaparser.utils.SourceRoot;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.List;



public class MessageChains {
    public static void main(String[] args) throws IOException {
        SourceRoot src = new SourceRoot(FileSystems.getDefault().getPath("Data"));
        List<ParseResult<CompilationUnit>> parseResults = src.tryToParse();
        for(ParseResult<CompilationUnit> p: parseResults) {
            if (p.isSuccessful()) {
                CompilationUnit cu = p.getResult().get();
                for (Comment c : cu.getComments()) {       // Strips out comments from Comp. Unit
                    c.remove();
                }
                cu.accept(new MessageChainsVisitor(), null);
            }
        }
    }

    private static class MessageChainsVisitor extends VoidVisitorAdapter{
        public void visit(MethodCallExpr n, Object arg){
            super.visit(n, arg);
            List<MethodCallExpr> lm = n.findAll(MethodCallExpr.class);
            lm.remove(n);
            System.out.println(n.getScope());
            for(MethodCallExpr m: lm){
                for(MethodCallExpr o: m.findAll(MethodCallExpr.class)){
                    if((o.getScope()!= m.getScope())&&(m.getScope()!= n.getScope())&&(n.getScope()!= o.getScope())){
                        System.out.println(n);
                        System.out.println("This method call uses message chaining");
                    }
                }
            }

        }
    }
}
