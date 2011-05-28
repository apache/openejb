package org.apache.maven.plugins;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.openejb.cli.Bootstrap;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Goal which starts OpenEJB before launching tests.
 *
 * @goal start
 * @phase test
 */
public class StartMojo extends AbstractOpenEJBMojo {
    
    /**
     * Location of the file.
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    public void execute() throws MojoExecutionException {
        getLog().info("[Start] Enterring execute");
        getLog().info("[Start] openejb.home=" + openejbHome);
        getLog().info("[Start] openejb.base=" + openejbBase);
        
        Set<String> argsSet = new HashSet<String>();
        argsSet.add(String.format("-Dopenejb.home=%s", openejbHome));
        argsSet.add(String.format("-Dopenejb.base=%s", openejbBase));
        if (hasCommandlineArgs()) {
            argsSet.addAll(Arrays.asList(parseCommandlineArgs()));
        }
        
        // set the command to start
        argsSet.add("start");
        
        try {
            Bootstrap.main((String[]) argsSet.toArray(new String[argsSet.size()]));
        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoExecutionException("Error while running OpenEJB Start command", e);
        }
    }
}
