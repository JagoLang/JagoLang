package jago.parsing.visitor;

import jago.JagoBaseVisitor;
import jago.JagoParser;
import jago.domain.Clazz;

import java.util.ArrayList;

/**
 *
 */
public class ClassVisitor extends JagoBaseVisitor<Clazz> {
    @Override
    public Clazz visitClassDeclaration( JagoParser.ClassDeclarationContext ctx) {
        String name = ctx.className().getText();
        return new Clazz(name, new ArrayList<>());
    }
}
