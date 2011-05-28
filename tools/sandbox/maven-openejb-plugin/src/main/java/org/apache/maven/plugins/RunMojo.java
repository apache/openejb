package org.apache.maven.plugins;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.openejb.cli.Bootstrap;

/**
 * Goal which delegates commands to OpenEJB.
 *
 * @goal run
 * @phase test
 */
public class RunMojo extends AbstractOpenEJBMojo {
    
    public void execute() throws MojoExecutionException {
        getLog().info("[Run] Enterring execute");
        getLog().info("[Run] openejb.home=" + openejbHome);
        getLog().info("[Run] openejb.base=" + openejbBase);
        
        Set<String> argsSet = new HashSet<String>();
        argsSet.add(String.format("-Dopenejb.home=%s", openejbHome));
        argsSet.add(String.format("-Dopenejb.base=%s", openejbBase));
        if (hasCommandlineArgs()) {
            argsSet.addAll(Arrays.asList(parseCommandlineArgs()));
        }
        
        try {
            Bootstrap.main((String[]) argsSet.toArray(new String[argsSet.size()]));
        } catch (Exception e) {
            throw new MojoExecutionException("Error while running OpenEJB run command", e);
        }
    }
}
