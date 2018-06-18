package jago.compiler.parsing.visitor;

import jago.antlr.JagoBaseVisitor;
import jago.antlr.JagoParser;
import jago.compiler.domain.Clazz;

import java.util.ArrayList;

/**
 *
 */
public class ClassVisitor extends JagoBaseVisitor<Clazz> {
    @Override
    public Clazz visitClassDeclaration(JagoParser.ClassDeclarationContext ctx) {
        String name = ctx.className().getText();
        return new Clazz(name, new ArrayList<>());
    }
}
