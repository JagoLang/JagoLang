package jago;


import jago.compiler.Compiler;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "compile", defaultPhase = LifecyclePhase.COMPILE)
public class JagoPlugin extends AbstractMojo {


    @Parameter(defaultValue = "src")
    private String sourceFolder;
    @Parameter(defaultValue = "target")
    private String targetFolder;

    public void execute() throws MojoExecutionException {
        File dir = new File(sourceFolder);

        List<File> files = new ArrayList<>(FileUtils.listFiles(dir, new String[]{"jago"}, true));

        for (File file : files) {
            try {
                getLog().info("file: " + file.getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String[] filePaths = files.stream().map(File::getAbsolutePath).toArray(String[]::new);
        Compiler compiler = new Compiler(sourceFolder, targetFolder + "/classes");
        try {
            compiler.compile(filePaths);
            getLog().info("Compilation complete");
        } catch (Exception e) {
            throw new MojoExecutionException("Compilation failed", e);
        }

    }
}
