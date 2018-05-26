package jago.parsing.visitor;

import jago.JagoBaseVisitor;
import jago.JagoParser;
import jago.domain.imports.Import;
import jago.util.ParserUtils;

import java.util.List;

public class ImportsVisitor extends JagoBaseVisitor<List<Import>> {


    public List<Import> visitImports(JagoParser.ImportsContext ctx) {
        return ParserUtils.parseImports(ctx);
    }
}
