package jago.parsing;

import com.google.common.io.Files;
import jago.JagoLexer;
import jago.JagoParser;
import jago.domain.CompilationUnit;
import jago.parsing.visitor.CompilationUnitVisitor;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 *
 */
public class Parser {

    public CompilationUnit getCompilationUnit(String fileAbsolutePath, CountDownLatch latch) throws IOException {
        CharStream charStream = new ANTLRFileStream(fileAbsolutePath); //fileAbolutePath - file containing first enk code file
        JagoLexer lexer = new JagoLexer(charStream);  //create lexer (pass enk file to it)
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        JagoParser parser = new JagoParser(tokenStream);

        ANTLRErrorListener errorListener = new JagoTreeWalkErrorListener(); //JagoTreeWalkErrorListener - handles parse tree visiting error events
        parser.addErrorListener(errorListener);
        String fileName = Files.getNameWithoutExtension(new File(fileAbsolutePath).getName());
        CompilationUnitVisitor compilationUnitVisitor = new CompilationUnitVisitor(fileName, latch);
        return parser.compilationUnit().accept(compilationUnitVisitor);
    }
}