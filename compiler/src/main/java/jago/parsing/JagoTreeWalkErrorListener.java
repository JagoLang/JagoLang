package jago.parsing;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 */
public class JagoTreeWalkErrorListener extends BaseErrorListener {

    private static final Logger LOGGER = LogManager.getLogger(JagoTreeWalkErrorListener.class);

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        String errorFormat = "You fucked up at line %d,char %d :(. Details:%n%s";
        String errorMsg = String.format(errorFormat, line, charPositionInLine, msg);;
        LOGGER.error(errorMsg);
    }
}
