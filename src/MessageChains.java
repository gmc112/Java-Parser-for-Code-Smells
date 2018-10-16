import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.FileInputStream;
import java.io.IOException;

public class MessageChains {
    public static void main(String[] args) throws IOException {
        FileInputStream in = new FileInputStream("Data/Couplers/MessageChainsBetter/Client.java");
        CompilationUnit cu;
        try {
            cu = JavaParser.parse(in);
        } finally {
            in.close();
        }
        for(Comment c: cu.getComments()){       // Strips out comments from Comp. Unit
            c.remove();
        }

        cu.accept(new MessageChains.MessageChainsVisitor(), null);
    }

    private static class MessageChainsVisitor extends VoidVisitorAdapter{
        public void visit(MethodCallExpr n, Object arg){
            System.out.println(n.getParentNode());
        }
    }
}
