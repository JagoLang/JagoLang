package jago.compiler.common;

import jago.compiler.bytecodegeneration.BytecodeGenerator;
import jago.compiler.domain.ClazzWrapper;
import jago.compiler.domain.CompilationUnit;
import jago.compiler.parsing.Parser;
import jago.compiler.validation.ARGUMENT_ERRORS;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 *
 */
public class Compiler {

    private final String sourceFolder;
    private final String targetFolder;

    public Compiler(String sourceFolder, String targetFolder) {
        this.sourceFolder = sourceFolder;
        this.targetFolder = targetFolder;
    }

    private static final Logger LOGGER = LogManager.getLogger(Compiler.class);

    public static void main(String[] args) throws Exception {
        try {
            Compiler compiler = new Compiler("src", "target");
            compiler.compile(args);
        } catch (IOException exception) {
            LOGGER.error("ERROR: " + exception.getMessage());
        }
    }


    public void compile(String... args) throws Exception {
        CountDownLatch latch = new CountDownLatch(args.length);
        ExecutorService executor = Executors.newFixedThreadPool(args.length);
        Map<String, Future<CompilationUnit>> units = new HashMap<>(args.length);
        long start = System.currentTimeMillis();
        for (String fileName : args) {

            ARGUMENT_ERRORS argumentsErrors = getArgumentValidationErrors(fileName);
            if (argumentsErrors != ARGUMENT_ERRORS.NONE) {
                String errorMessage = argumentsErrors.getMessage();
                LOGGER.error(errorMessage);
                return;
            }

            File enkelFile = new File(fileName);
            String fileAbsolutePath = enkelFile.getAbsolutePath();
            LOGGER.info("Trying to parse '{}'.", enkelFile.getAbsolutePath());
            units.put(fileName, executor.submit(() -> new Parser().getCompilationUnit(fileAbsolutePath, latch)));

        }
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        System.out.println("Time took:" + (System.currentTimeMillis() - start));
        for (String fileName : args) {
            CompilationUnit compilationUnit = units.get(fileName).get();
            saveBytecodeToClassFile(compilationUnit, fileName);
        }

        LOGGER.info("Finished Parsing. Started compiling to bytecode.");


    }

    private ARGUMENT_ERRORS getArgumentValidationErrors(String arg) {
        if (arg == null) {
            return ARGUMENT_ERRORS.NO_FILE;
        }
        if (!arg.endsWith(".jago")) {
            return ARGUMENT_ERRORS.BAD_FILE_EXTENSION;
        }
        return ARGUMENT_ERRORS.NONE;
    }

    private void saveBytecodeToClassFile(CompilationUnit compilationUnit, String srcFileName) throws IOException {
        BytecodeGenerator bytecodeGenerator = new BytecodeGenerator();
        List<ClazzWrapper> wrappers = bytecodeGenerator.generate(compilationUnit);

        for (ClazzWrapper cW : wrappers) {
            String targetFileName = srcFileName.replaceFirst("\\.jago", ".class").replaceFirst(sourceFolder, targetFolder);

            LOGGER.info("Finished Compiling. Saving bytecode to '{}'.", Paths.get(targetFileName).toAbsolutePath());
            File targetFile = new File(targetFileName);
            targetFile.getParentFile().mkdirs();
            OutputStream os = new FileOutputStream(targetFile, false);
            IOUtils.write(cW.getBytecode(), os);
        }

        LOGGER.info("Done. To run compiled file execute: 'java {}' in current dir", compilationUnit.getCompilationUnitScope().getClassName());
    }
}
