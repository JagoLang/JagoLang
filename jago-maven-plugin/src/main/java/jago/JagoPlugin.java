package jago;


import jago.compiler.Compiler;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "jagoify", defaultPhase = LifecyclePhase.COMPILE)
public class JagoPlugin extends AbstractMojo {

    private String source = null;
    private String target = null;

    public void execute() throws MojoExecutionException {
        if (source == null) {
            source = "src";
        }
        if (target == null) {
            target = "target";
        }

        File dir = new File(source);

        String[] extensions = new String[]{"jago"};

        List<File> files = new ArrayList<>(FileUtils.listFiles(dir, extensions, true));

        for (File file : files) {
            try {
                getLog().info("file: " + file.getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String[] filePaths = files.stream().map(File::getAbsolutePath).toArray(String[]::new);
        Compiler compiler = new Compiler(source, target);
        try {
            compiler.compile(filePaths);
        } catch (Exception e) {
            getLog().error(e);
        }

        getLog().info("Easy compile");
    }
}
