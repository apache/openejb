package org.openejb.alt.config;

import java.io.OutputStream;

public interface Compiler {

    /**
     * Specify where the compiler can be found
     */ 
    void setCompilerPath(String compilerPath);

    /**
     * Set the encoding (character set) of the source
     */ 
    void setEncoding(String encoding);

    /**
     * Set the class path for the compiler
     */ 
    void setClasspath(String classpath);

    /**
     * Set the output directory
     */ 
    void setOutputDir(String outdir);

    /**
     * Set where you want the compiler output (messages) to go 
     */ 
    void setMsgOutStream(OutputStream out);

    /**
     * Execute the compiler
     * @param source - file name of the source to be compiled
     */ 
    boolean compile(String source);

}

