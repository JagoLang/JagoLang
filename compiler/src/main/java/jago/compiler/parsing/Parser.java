package jago.compiler.parsing;

import com.google.common.io.Files;
import jago.antlr.JagoLexer;
import jago.antlr.JagoParser;
import jago.compiler.domain.CompilationUnit;
import jago.compiler.parsing.visitor.CompilationUnitVisitor;
import org.antlr.v4.runtime.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 *
 */
public class Parser {

    public CompilationUnit getCompilationUnit(String fileAbsolutePath, CountDownLatch latch) throws IOException {
        CharStream charStream = CharStreams.fromFileName(fileAbsolutePath); //fileAbolutePath - file containing code file
        JagoLexer lexer = new JagoLexer(charStream);  //create lexer
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        JagoParser parser = new JagoParser(tokenStream);

        ANTLRErrorListener errorListener = new JagoTreeWalkErrorListener(); //JagoTreeWalkErrorListener - handles parse tree visiting error events
        parser.addErrorListener(errorListener);
        String fileName = Files.getNameWithoutExtension(new File(fileAbsolutePath).getName());
        CompilationUnitVisitor compilationUnitVisitor = new CompilationUnitVisitor(fileName, latch);
        return parser.compilationUnit().accept(compilationUnitVisitor);
    }
}