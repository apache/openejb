package org.openejb.alt.config;

import java.io.OutputStream;

public class SunCompiler implements org.openejb.alt.config.Compiler {

    String encoding;
    String classpath; // not used here.
    String compilerPath;
    String outdir;
    OutputStream out;

    /**
     * Specify where the compiler can be found
     */ 
    public void setCompilerPath(String compilerPath) {
        // not used here.
	    this.compilerPath = compilerPath;
    }

    /**
     * Set the encoding (character set) of the source
     */ 
    public void setEncoding(String encoding) {
      this.encoding = encoding;
    }

    /**
     * Set the class path for the compiler
     */ 
    public void setClasspath(String classpath) {
      this.classpath = classpath;
    }

    /**
     * Set the output directory
     */ 
    public void setOutputDir(String outdir) {
      this.outdir = outdir;
    }

    /**
     * Set where you want the compiler output (messages) to go 
     */ 
    public void setMsgOutStream(OutputStream out) {
      this.out = out;
    }

    
    public boolean compile(String source) {
        
        sun.tools.javac.Main compiler = new sun.tools.javac.Main(out, "OpenEJB::javac");
    	
    	String[] args = null;
    
    	if( outdir == null ) {
            args = new String[]{
                    "-classpath", classpath,
                    "-encoding", encoding,
                    source       
                };    
    	} else {
    	    args = new String[]{
                    "-classpath", classpath,
                    "-encoding", encoding,
                    "-d", outdir,
                    source
                };
                
    	}
    
            return compiler.compile(args);
        }
}
