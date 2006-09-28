/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.apache.openejb.server.telnet;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.ejb.EJBHome;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.naming.IvmContext;

/**
 */
public class Lookup extends Command
{

    javax.naming.Context ctx = OpenEJB.getJNDIContext();

    public static void register()
    {
        Lookup cmd = new Lookup();
        Command.register( "lookup", cmd );
        //Command.register("list", cmd);
    }

    static String PWD = "";

    // execute jndi lookups
    public void exec( Arguments args, DataInputStream in, PrintStream out ) throws IOException
    {
        try
        {
            String name = "";
            if ( args == null || args.count() == 0 )
            {
                name = PWD;
            }
            else
            {
                name = args.get();
            }

            Object obj = null;
            try
            {
                obj = ctx.lookup( name );
            }
            catch ( NameNotFoundException e )
            {
                out.print( "lookup: " );
                out.print( name );
                out.println( ": No such object or subcontext" );
                return;
            }
            catch ( Throwable e )
            {
                out.print( "lookup: error: " );
                e.printStackTrace( new PrintStream( out ) );
                return;
            }

            if ( obj instanceof Context )
            {
                list( name, in, out );
                return;
            }

            // TODO:1: Output the different data types differently
            out.println( "" + obj );
        }
        catch ( Exception e )
        {
            e.printStackTrace( new PrintStream( out ) );
        }
    }

    public void list( String name, DataInputStream in, PrintStream out ) throws IOException
    {
        try
        {
            NamingEnumeration ne = null;
            try
            {

                ne = ctx.list( name );
            }
            catch ( NameNotFoundException e )
            {
                out.print( "lookup: " );
                out.print( name );
                out.println( ": No such object or subcontext" );
                return;
            }
            catch ( Throwable e )
            {
                out.print( "lookup: error: " );
                e.printStackTrace( new PrintStream( out ) );
                return;
            }

            if ( ne == null )
            {
                return;
            }

            while ( ne.hasMore() )
            {

                NameClassPair entry = ( NameClassPair ) ne.next();
                String eName = entry.getName();
                Class eClass = null;

                if ( IvmContext.class.getName().equals( entry.getClassName() ) )
                {
                    eClass = IvmContext.class;
                }
                else
                {
                    try
                    {
                        ClassLoader cl = org.apache.openejb.util.ClasspathUtils.getContextClassLoader();
                        eClass = Class.forName( entry.getClassName(), true, cl );
                    }
                    catch ( Throwable t )
                    {
                        eClass = java.lang.Object.class;
                    }
                }

                if ( Context.class.isAssignableFrom( eClass ) )
                {
                    //out.print("-c- ");
                    out.print( TextConsole.TTY_Bright );
                    out.print( TextConsole.TTY_FG_Blue );
                    out.print( entry.getName() );
                    out.print( TextConsole.TTY_Reset );
                }
                else if ( EJBHome.class.isAssignableFrom( eClass ) )
                {
                    //out.print("-b- ");
                    out.print( TextConsole.TTY_Bright );
                    out.print( TextConsole.TTY_FG_Green );
                    out.print( entry.getName() );
                    out.print( TextConsole.TTY_Reset );
                }
                else
                {
                    //out.print("-o- ");
                    out.print( entry.getName() );
                }
                out.println();
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace( new PrintStream( out ) );
        }
    }
}

