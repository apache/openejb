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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
/**
 * Copyright (C) The Community OpenORB Project. All rights reserved.
 *
 * This software is published under the terms of The OpenORB Community Software
 * License version 1.0, a copy of which has been included with this distribution
 * in the LICENSE.txt file.
 */
package org.openejb.corba.proxy;

import java.io.File;
import java.io.PrintWriter;

import org.openorb.compiler.CompilerHost;
import org.openorb.compiler.object.IdlFactoryMember;
import org.openorb.compiler.object.IdlFixed;
import org.openorb.compiler.object.IdlIdent;
import org.openorb.compiler.object.IdlInterface;
import org.openorb.compiler.object.IdlObject;
import org.openorb.compiler.object.IdlSimple;
import org.openorb.compiler.object.IdlUnion;
import org.openorb.compiler.object.IdlUnionMember;
import org.openorb.compiler.object.IdlValue;
import org.openorb.compiler.object.IdlValueBox;
import org.openorb.compiler.parser.IdlType;
import org.openorb.compiler.parser.Token;
import org.openorb.compiler.rmi.RmiCompilerProperties;
import org.openorb.compiler.rmi.generator.Javatoidl;


/**
 * @author Jerome Daniel
 * @version $Revision$ $Date$
 */
public class StubGenerator extends Javatoidl {

    public StubGenerator(RmiCompilerProperties rcp, CompilerHost ch) {
        super(rcp, ch);
    }

    public void translate_object(IdlObject obj) {
        java.io.File writeInto = createDirectory("", m_cp.getM_destdir());

        obj.reset();

        while (obj.end() != true) {
            java.io.File tmpInto = writeInto;

            if (obj.current().included() == false) {
                String old_pkg = current_pkg;

                if (m_cp.getM_usePrefix()) {
                    if ((obj.current().getPrefix() != null) && (obj.kind() == IdlType.e_root)) {
                        tmpInto = createPrefixDirectories(obj.current().getPrefix(), writeInto);

                        if (m_cp.getM_reversePrefix())
                            addToPkg(obj, inversedPrefix(obj.current().getPrefix()));
                        else
                            addToPkg(obj, obj.current().getPrefix());
                    }
                }

                if (obj.current().kind() == IdlType.e_interface) {
                    translate_interface(obj, tmpInto);
                }

                current_pkg = old_pkg;
            }

            obj.next();
        }
    }

    /**
     * Add a Helper for a data type
     *
     * @param obj the object to translate
     */
    public void translate_interface(IdlObject obj, File dest_dir) {

        PrintWriter output = newFile(dest_dir, obj.name() + "Helper");
        boolean real_corba_object = false;
        boolean abstract_object = false;

        if (current_pkg != null) {
            if (current_pkg.equals("generated")) {
                if (m_cp.getM_use_package() == true) {
                    output.println("package " + current_pkg + ";");
                    output.println("");
                }
            } else if (!current_pkg.equals("")) {
                output.println("package " + current_pkg + ";");
                output.println("");
            }
        }

        output.println("/** ");
        output.println(" * Helper class for : " + obj.name());
        output.println(" *  ");
        output.println(" * @author OpenORB Compiler");
        output.println(" */ ");

        // Define the Helper class
        if (obj.kind() == IdlType.e_value_box)
            output.println("public class " + obj.name() + "Helper implements org.omg.CORBA.portable.BoxedValueHelper");
        else
            output.println("public class " + obj.name() + "Helper");

        output.println("{");

        switch (final_kind(obj)) {

            case IdlType.e_struct:

            case IdlType.e_union:

            case IdlType.e_exception:

            case IdlType.e_sequence:

            case IdlType.e_array:

            case IdlType.e_fixed:
                output.println(tab + "/** extract_X methods found for the current ORBs Any type. */");
                output.println(tab + "private static java.lang.Object [] _extractMethods;");
                output.println(tab + "");
                output.println(tab + "static");
                output.println(tab + "{");
                output.println(tab2 + "try");
                output.println(tab2 + "{");
                output.println(tab3 + "Class clz = Thread.currentThread().getContextClassLoader().loadClass( \"org.openorb.orb.core.Any\" );");
                output.println(tab3 + "java.lang.reflect.Method meth = clz.getMethod( \"extract_Streamable\", null );");
                output.println(tab3 + "_extractMethods = new java.lang.Object[] { clz, meth };");
                output.println(tab2 + "}");
                output.println(tab2 + "catch ( Exception ex )");
                output.println(tab2 + "{");
                output.println(tab3 + "// do nothing");
                output.println(tab2 + "}");
                output.println(tab2 + "");
                output.println(tab2 + "if ( _extractMethods == null )");
                output.println(tab2 + "{");
                output.println(tab3 + "_extractMethods = new java.lang.Object[ 0 ];");
                output.println(tab2 + "}");
                output.println(tab + "}");
                output.println();
                output.println(tab + "private static java.lang.reflect.Method getExtract( Class clz )");
                output.println(tab + "{");
                output.println(tab2 + "int len = _extractMethods.length;");
                output.println(tab2 + "for ( int i = 0; i < len; i += 2 )");
                output.println(tab2 + "{");
                output.println(tab3 + "if ( clz.equals( _extractMethods[ i ] ) )");
                output.println(tab3 + "{");
                output.println(tab4 + "return ( java.lang.reflect.Method ) _extractMethods[ i + 1 ];");
                output.println(tab3 + "}");
                output.println(tab2 + "}");
                output.println(tab2 + "");
                output.println(tab2 + "// unknown class, look for method.");
                output.println(tab2 + "synchronized ( org.omg.CORBA.Any.class )");
                output.println(tab2 + "{");
                output.println(tab3 + "for ( int i = len; i < _extractMethods.length; i += 2 )");
                output.println(tab3 + "{");
                output.println(tab4 + "if ( clz.equals( _extractMethods[ i ] ) )");
                output.println(tab4 + "{");
                output.println(tab5 + "return ( java.lang.reflect.Method ) _extractMethods[ i + 1 ];");
                output.println(tab4 + "}");
                output.println(tab3 + "}");
                output.println(tab3 + "");
                output.println(tab3 + "java.lang.Object [] tmp = new java.lang.Object[ _extractMethods.length + 2 ];");
                output.println(tab3 + "System.arraycopy( _extractMethods, 0, tmp, 0, _extractMethods.length );");
                output.println(tab3 + "tmp[ _extractMethods.length ] = clz;");
                output.println(tab3 + "try");
                output.println(tab3 + "{");
                output.println(tab4 + "tmp[ _extractMethods.length + 1 ] = clz.getMethod( \"extract_Streamable\", null );");
                output.println(tab3 + "}");
                output.println(tab3 + "catch ( Exception ex )");
                output.println(tab3 + "{");
                output.println(tab4 + "// do nothing");
                output.println(tab3 + "}");
                output.println(tab3 + "_extractMethods = tmp;");
                output.println(tab3 + "return ( java.lang.reflect.Method )_extractMethods[ _extractMethods.length - 1 ];");
                output.println(tab2 + "}");
                output.println(tab + "}");

        }

        // The method  insert
        output.println(tab + "/**");

        output.println(tab + " * Insert " + obj.name() + " into an any");

        output.println(tab + " * @param a an any");

        output.println(tab + " * @param t " + obj.name() + " value");

        output.println(tab + " */");

        output.print(tab + "public static void insert(org.omg.CORBA.Any a, ");

        translate_type(obj, output);

        output.println(" t)");

        output.println(tab + "{");

        switch (final_kind(obj)) {

            case IdlType.e_interface:

                output.println(tab2 + "if ( t instanceof org.omg.CORBA.Object )");
                output.println(tab3 + "a.insert_Object( ( org.omg.CORBA.Object ) t , type() );");
                output.println(tab2 + "else if(t instanceof java.io.Serializable)");
                output.println(tab3 + "a.insert_Value((java.io.Serializable)t, type());");
                output.println(tab2 + "else");
                output.println(tab3 + "throw new org.omg.CORBA.BAD_PARAM();");

                break;

            case IdlType.e_value:

            case IdlType.e_value_box:
                output.println(tab2 + "a.insert_Value(t, type());");

                break;

            case IdlType.e_struct:

            case IdlType.e_union:

            case IdlType.e_exception:
                output.print(tab2 + "a.insert_Streamable(new ");

                translate_type(obj, output);

                output.println("Holder(t));");

                break;

            case IdlType.e_sequence:

            case IdlType.e_array:

            case IdlType.e_fixed:
                output.println(tab2 + "a.insert_Streamable(new " + fullname(obj) + "Holder(t));");

                break;

            default:
                output.println(tab2 + "a.type(type());");

                output.println(tab2 + "write(a.create_output_stream(),t);");
        }

        output.println(tab + "}");
        output.println("");

        // The method  extract
        output.println(tab + "/**");
        output.println(tab + " * Extract " + obj.name() + " from an any");
        output.println(tab + " *");
        output.println(tab + " * @param a an any");
        output.println(tab + " * @return the extracted " + obj.name() + " value");
        output.println(tab + " */");
        output.print(tab + "public static ");

        translate_type(obj, output);

        output.println(" extract( org.omg.CORBA.Any a )");
        output.println(tab + "{");
        output.println(tab2 + "if ( !a.type().equivalent( type() ) )");
        output.println(tab2 + "{");
        output.println(tab3 + "throw new org.omg.CORBA.MARSHAL();");
        output.println(tab2 + "}");

        switch (final_kind(obj)) {

            case IdlType.e_interface:
                output.println(tab2 + "try");
                output.println(tab2 + "{");
                output.print(tab3 + "return ");
                translate_type(obj, output);
                output.println("Helper.narrow( a.extract_Object() );");
                output.println(tab2 + "}");
                output.println(tab2 + "catch ( final org.omg.CORBA.BAD_PARAM e )");
                output.println(tab2 + "{");

                writeThrowException(output, tab3, "org.omg.CORBA.MARSHAL", "e.getMessage()", "e");

                output.println(tab2 + "}");

                if (!((IdlInterface) final_type(obj)).abstract_interface())
                    break;

                output.println(tab2 + "catch ( org.omg.CORBA.BAD_OPERATION ex )");
                output.println(tab2 + "{");
                output.println(tab3 + "// do nothing");
                output.println(tab2 + "}");

                // fallthrough

            case IdlType.e_value:

            case IdlType.e_value_box:
                output.println(tab2 + "try");
                output.println(tab2 + "{");

                output.print(tab3 + "return (");

                translate_type(obj, output);

                output.println(") a.extract_Value();");

                output.println(tab2 + "}");

                output.println(tab2 + "catch ( final ClassCastException e )");
                output.println(tab2 + "{");


                writeThrowException(output, tab3, "org.omg.CORBA.MARSHAL", "e.getMessage()", "e");

                output.println(tab2 + "}");

                break;

            case IdlType.e_struct:

            case IdlType.e_union:

            case IdlType.e_exception:
                output.println(tab3 + "// streamable extraction. The jdk stubs incorrectly define the Any stub");
                output.println(tab2 + "java.lang.reflect.Method meth = getExtract( a.getClass() );");
                output.println(tab2 + "if ( meth != null )");
                output.println(tab2 + "{");
                output.println(tab3 + "try");
                output.println(tab3 + "{");
                output.println(tab4 + "org.omg.CORBA.portable.Streamable s =");
                output.println(tab5 + "( org.omg.CORBA.portable.Streamable ) meth.invoke( a, null );");

                output.print(tab4 + "if ( s instanceof ");
                translate_type(obj, output);
                output.println("Holder )");
                output.print(tab5 + "return ( ( ");
                translate_type(obj, output);
                output.println("Holder ) s ).value;");

                output.println(tab3 + "}");
                output.println(tab3 + "catch ( final IllegalAccessException e )");
                output.println(tab3 + "{");

                writeThrowException(output, tab4, "org.omg.CORBA.INTERNAL", "e.toString()", "e");

                output.println(tab3 + "}");
                output.println(tab3 + "catch ( final IllegalArgumentException e )");
                output.println(tab3 + "{");

                writeThrowException(output, tab4, "org.omg.CORBA.INTERNAL", "e.toString()", "e");

                output.println(tab3 + "}");
                output.println(tab3 + "catch ( final java.lang.reflect.InvocationTargetException e )");
                output.println(tab3 + "{");
                output.println(tab4 + "Throwable rex = e.getTargetException();");
                output.println(tab4 + "if ( rex instanceof org.omg.CORBA.BAD_INV_ORDER )");
                output.println(tab4 + "{");
                output.println(tab5 + "// do nothing");
                output.println(tab4 + "}");
                output.println(tab4 + "else if ( rex instanceof Error )");
                output.println(tab4 + "{");
                output.println(tab5 + "throw ( Error ) rex;");
                output.println(tab4 + "}");
                output.println(tab4 + "else if ( rex instanceof RuntimeException )");
                output.println(tab4 + "{");
                output.println(tab5 + "throw ( RuntimeException ) rex;");
                output.println(tab4 + "}");
                output.println(tab4 + "else");
                output.println(tab4 + "{");

//                writeThrowException(output, tab5, "org.omg.CORBA.INTERNAL", "rex.toString()", "rex");

                output.println(tab4 + "}");

                output.println(tab3 + "}");

                output.print(tab3 + "");
                translate_type(obj, output);
                output.print("Holder h = new ");
                translate_type(obj, output);
                output.println("Holder( read( a.create_input_stream() ) );");
                output.println(tab3 + "a.insert_Streamable( h );");
                output.println(tab3 + "return h.value;");
                output.println(tab2 + "}");
                output.println(tab2 + "return read( a.create_input_stream() );");
                break;

            case IdlType.e_sequence:

            case IdlType.e_array:

            case IdlType.e_fixed:
                output.println(tab2 + "// streamable extraction. The jdk stubs incorrectly define the Any stub");
                output.println(tab2 + "java.lang.reflect.Method meth = getExtract( a.getClass() );");
                output.println(tab2 + "if ( meth != null )");
                output.println(tab2 + "{");
                output.println(tab3 + "try");
                output.println(tab3 + "{");
                output.println(tab4 + "org.omg.CORBA.portable.Streamable s ");
                output.println(tab5 + "= ( org.omg.CORBA.portable.Streamable ) meth.invoke( a, null );");


                output.println(tab4 + "if ( s instanceof " + fullname(obj) + "Holder )");
                output.println(tab4 + "{");
                output.println(tab5 + "return ( ( " + fullname(obj) + "Holder ) s ).value;");
                output.println(tab4 + "}");

                output.println(tab3 + "}");
                output.println(tab3 + "catch ( final IllegalAccessException e )");
                output.println(tab3 + "{");

                writeThrowException(output, tab4, "org.omg.CORBA.INTERNAL", "e.toString()", "e");

                output.println(tab3 + "}");
                output.println(tab3 + "catch ( final IllegalArgumentException e )");
                output.println(tab3 + "{");

                writeThrowException(output, tab4, "org.omg.CORBA.INTERNAL", "e.toString()", "e");

                output.println(tab3 + "}");
                output.println(tab3 + "catch ( final java.lang.reflect.InvocationTargetException e )");
                output.println(tab3 + "{");
                output.println(tab4 + "final Throwable rex = e.getTargetException();");
                output.println(tab4 + "if ( rex instanceof org.omg.CORBA.BAD_INV_ORDER )");
                output.println(tab4 + "{");
                output.println(tab5 + "// do nothing");
                output.println(tab4 + "}");
                output.println(tab4 + "else if ( rex instanceof Error )");
                output.println(tab4 + "{");
                output.println(tab5 + "throw ( Error ) rex;");
                output.println(tab4 + "}");
                output.println(tab4 + "else if ( rex instanceof RuntimeException )");
                output.println(tab4 + "{");
                output.println(tab5 + "throw ( RuntimeException ) rex;");
                output.println(tab4 + "}");

//                writeThrowException(output, tab4, "org.omg.CORBA.INTERNAL", "rex.toString()", "rex");

                output.println(tab3 + "}");
                output.println(tab3 + "" + fullname(obj) + "Holder h = new " + fullname(obj) + "Holder( read( a.create_input_stream() ) );");
                output.println(tab3 + "a.insert_Streamable( h );");
                output.println(tab3 + "return h.value;");
                output.println(tab2 + "}");
                output.println(tab2 + "return read( a.create_input_stream() );");
                break;

            default:
                output.println(tab2 + "return read( a.create_input_stream() );");
        }

        output.println(tab + "}");
        output.println("");

        // The method static _tc
        output.println(tab + "//");
        output.println(tab + "// Internal TypeCode value");
        output.println(tab + "//");
        output.println(tab + "private static org.omg.CORBA.TypeCode _tc = null;");

        switch (final_kind(obj)) {

            case IdlType.e_value:

            case IdlType.e_struct:

            case IdlType.e_union:

            case IdlType.e_exception:
                output.println(tab + "private static boolean _working = false;");
        }

        output.println("");

        // The method type
        output.println(tab + "/**");
        output.println(tab + " * Return the " + obj.name() + " TypeCode");
        output.println(tab + " * @return a TypeCode");
        output.println(tab + " */");
        output.println(tab + "public static org.omg.CORBA.TypeCode type()");
        output.println(tab + "{");
        output.println(tab2 + "if (_tc == null) {");

        switch (final_kind(obj)) {

            case IdlType.e_value:

            case IdlType.e_struct:

            case IdlType.e_union:

            case IdlType.e_exception:
                output.println(tab3 + "synchronized(org.omg.CORBA.TypeCode.class) {");
                output.println(tab4 + "if (_tc != null)");
                output.println(tab5 + "return _tc;");
                output.println(tab4 + "if (_working)");
                output.println(tab5 + "return org.omg.CORBA.ORB.init().create_recursive_tc(id());");
                output.println(tab4 + "_working = true;");
        }

        translate_new_typecode(obj, output);

        switch (final_kind(obj)) {

            case IdlType.e_value:

            case IdlType.e_struct:

            case IdlType.e_union:

            case IdlType.e_exception:
                output.println(tab4 + "_working = false;");
                output.println(tab3 + "}");
        }

        output.println(tab2 + "}");
        output.println(tab2 + "return _tc;");
        output.println(tab + "}");
        output.println("");

        // The method id
        output.println(tab + "/**");
        output.println(tab + " * Return the " + obj.name() + " IDL ID");
        output.println(tab + " * @return an ID");
        output.println(tab + " */");
        output.println(tab + "public static String id()");
        output.println(tab + "{");
        output.println(tab2 + "return _id;");
        output.println(tab + "}");
        output.println("");
        output.println(tab + "private final static String _id = \"" + obj.getId() + "\";");
        output.println("");

        // The method read
        output.println(tab + "/**");
        output.println(tab + " * Read " + obj.name() + " from a marshalled stream");
        output.println(tab + " * @param istream the input stream");
        output.println(tab + " * @return the readed " + obj.name() + " value");
        output.println(tab + " */");
        output.print(tab + "public static ");

        translate_type(obj, output);

        output.println(" read(org.omg.CORBA.portable.InputStream istream)");
        output.println(tab + "{");

        if (final_kind(obj) == IdlType.e_value_box) {
            output.print(tab2 + "return (");

            translate_type(final_type(obj), output);

            output.println(") ((org.omg.CORBA_2_3.portable.InputStream)istream).read_value(new " + fullname(final_type(obj)) + "Helper());");
        } else if (final_kind(obj) == IdlType.e_forward_interface) {
            if ((((IdlInterface) final_type(obj)).getInterface().local_interface()) || (m_cp.getM_pidl()))
                output.println(tab2 + "throw new org.omg.CORBA.MARSHAL();");
            else
                translate_unmarshalling(obj, output, "istream");
        } else if (final_kind(obj) == IdlType.e_interface) {
            if ((((IdlInterface) final_type(obj)).local_interface()) || (m_cp.getM_pidl()))
                output.println(tab2 + "throw new org.omg.CORBA.MARSHAL();");
            else
                translate_unmarshalling(obj, output, "istream");
        } else if (final_kind(obj) == IdlType.e_fixed) {
            output.println(tab2 + "java.math.BigDecimal _f = istream.read_fixed();");

            if (((IdlFixed) final_type(obj)).scale() != 0)
                output.println(tab2 + "return _f.movePointLeft(" + ((IdlFixed) final_type(obj)).scale() + ");");
            else
                output.println(tab2 + "return _f;");
        } else if ((obj.kind() == IdlType.e_typedef) &&
                   (final_kind(obj) != IdlType.e_sequence) &&
                   (final_kind(obj) != IdlType.e_array) &&
                   (final_kind(obj) != IdlType.e_string) &&
                   (final_kind(obj) != IdlType.e_wstring) &&
                   (final_kind(obj) != IdlType.e_simple)) {
            output.print(tab2 + "return ");

            translate_type(final_type(obj), output);

            output.println("Helper.read(istream);");
        } else
            translate_unmarshalling(obj, output, "istream");

        output.println(tab + "}");

        output.println("");

        // La fonction write
        output.println(tab + "/**");

        output.println(tab + " * Write " + obj.name() + " into a marshalled stream");

        output.println(tab + " * @param ostream the output stream");

        output.println(tab + " * @param value " + obj.name() + " value");

        output.println(tab + " */");

        output.print(tab + "public static void write(org.omg.CORBA.portable.OutputStream ostream, ");

        translate_type(obj, output);

        output.println(" value)");

        output.println(tab + "{");

        if (final_kind(obj) == IdlType.e_value_box) {
            output.println(tab2 + "((org.omg.CORBA_2_3.portable.OutputStream)ostream).write_value(value, new " + fullname(final_type(obj)) + "Helper());");
        } else if (final_kind(obj) == IdlType.e_interface) {
            if ((((IdlInterface) final_type(obj)).local_interface()) || (m_cp.getM_pidl()))
                output.println(tab2 + "throw new org.omg.CORBA.MARSHAL();");
            else
                translate_marshalling(obj, output, "ostream", "value");
        } else if (final_kind(obj) == IdlType.e_fixed) {
            if (((IdlFixed) final_type(obj)).scale() != 0) {
                output.println(tab2 + "if (value.scale() != " + ((IdlFixed) final_type(obj)).scale() + ")");
                output.println(tab3 + "throw new org.omg.CORBA.DATA_CONVERSION();");
            }

            output.println(tab2 + "ostream.write_fixed(value);");
        } else if ((obj.kind() == IdlType.e_typedef) &&
                   (final_kind(obj) != IdlType.e_sequence) &&
                   (final_kind(obj) != IdlType.e_array) &&
                   (final_kind(obj) != IdlType.e_string) &&
                   (final_kind(obj) != IdlType.e_wstring) &&
                   (final_kind(obj) != IdlType.e_simple)) {
            output.print(tab2 + "");

            translate_type(final_type(obj), output);

            output.println("Helper.write(ostream, value);");
        } else
            translate_marshalling(obj, output, "ostream", "value");

        output.println(tab + "}");

        output.println("");

        // The narrow function
        if (obj.kind() == IdlType.e_interface) {

            if (((IdlInterface) obj).abstract_interface())
                abstract_object = true;
            else
                real_corba_object = true;

            if (isAbstractBaseInterface(obj))
                abstract_object = true;

            if (abstract_object) {
                output.println(tab + "/**");
                output.println(tab + " * Narrow CORBA::Object to " + obj.name());
                output.println(tab + " * @param obj the abstract Object");
                output.println(tab + " * @return " + obj.name() + " Object");
                output.println(tab + " */");
                output.println(tab + "public static " + obj.name() + " narrow(Object obj)");
                output.println(tab + "{");
                output.println(tab2 + "if (obj == null)");
                output.println(tab3 + "return null;");
                output.println(tab2 + "if (obj instanceof " + obj.name() + ")");
                output.println(tab3 + "return (" + obj.name() + ")obj;");

                if (!m_cp.getM_pidl() && !((IdlInterface) obj).local_interface()) {
                    output.println();
                    output.println(tab2 + "if (obj instanceof org.omg.CORBA.portable.ObjectImpl) {");
                    output.println(tab3 + "org.omg.CORBA.portable.ObjectImpl objimpl = (org.omg.CORBA.portable.ObjectImpl)obj;");
                    output.println(tab3 + "if (objimpl._is_a(id())) {");
                    output.println(tab4 + "_" + obj.name() + "_Stub stub = new _" + obj.name() + "_Stub();");
                    output.println(tab4 + "stub._set_delegate(objimpl._get_delegate());");
                    output.println(tab4 + "return stub;");
                    output.println(tab3 + "}");
                    output.println(tab2 + "}");
                    output.println();
                }

                output.println("");
                output.println(tab2 + "throw new org.omg.CORBA.BAD_PARAM();");
                output.println(tab + "}");
                output.println("");

                // Unchecked narrow
                output.println(tab + "/**");
                output.println(tab + " * Unchecked Narrow CORBA::Object to " + obj.name());
                output.println(tab + " * @param obj the abstract Object");
                output.println(tab + " * @return " + obj.name() + " Object");
                output.println(tab + " */");
                output.println(tab + "public static " + obj.name() + " unchecked_narrow(Object obj)");
                output.println(tab + "{");
                output.println(tab2 + "if (obj == null)");
                output.println(tab3 + "return null;");
                output.println(tab2 + "if (obj instanceof " + obj.name() + ")");
                output.println(tab3 + "return (" + obj.name() + ")obj;");

                if (!m_cp.getM_pidl() && !((IdlInterface) obj).local_interface()) {
                    output.println();
                    output.println(tab2 + "if (obj instanceof org.omg.CORBA.portable.ObjectImpl) {");
                    output.println(tab3 + "org.omg.CORBA.portable.ObjectImpl objimpl = (org.omg.CORBA.portable.ObjectImpl)obj;");
                    output.println(tab3 + "_" + obj.name() + "_Stub stub = new _" + obj.name() + "_Stub();");
                    output.println(tab3 + "stub._set_delegate(objimpl._get_delegate());");
                    output.println(tab3 + "return stub;");
                    output.println(tab2 + "}");
                    output.println();
                }

                output.println("");
                output.println(tab2 + "throw new org.omg.CORBA.BAD_PARAM();");
                output.println(tab + "}");
                output.println("");
            }

            if (real_corba_object) {
                output.println(tab + "/**");
                output.println(tab + " * Narrow CORBA::Object to " + obj.name());
                output.println(tab + " * @param obj the CORBA Object");
                output.println(tab + " * @return " + obj.name() + " Object");
                output.println(tab + " */");

                if (!m_cp.getM_pidl())
                    output.println(tab + "public static " + obj.name() + " narrow(org.omg.CORBA.Object obj)");
                else
                    output.println(tab + "public static " + obj.name() + " narrow(Object obj)");

                output.println(tab + "{");

                output.println(tab2 + "if (obj == null)");

                output.println(tab3 + "return null;");

                output.println(tab2 + "if (obj instanceof " + obj.name() + ")");

                output.println(tab3 + "return (" + obj.name() + ")obj;");

                output.println("");

                if (!m_cp.getM_pidl() && !((IdlInterface) obj).local_interface()) {
                    output.println(tab2 + "if (obj._is_a(id()))");
                    output.println(tab2 + "{");
                    output.println(tab3 + "_" + obj.name() + "_Stub stub = new _" + obj.name() + "_Stub();");
                    output.println(tab3 + "stub._set_delegate(((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate());");
                    output.println(tab3 + "return stub;");
                    output.println(tab2 + "}");
                    output.println("");
                }

                output.println(tab2 + "throw new org.omg.CORBA.BAD_PARAM();");
                output.println(tab + "}");
                output.println("");

                // Unchecked narrow
                output.println(tab + "/**");
                output.println(tab + " * Unchecked Narrow CORBA::Object to " + obj.name());
                output.println(tab + " * @param obj the CORBA Object");
                output.println(tab + " * @return " + obj.name() + " Object");
                output.println(tab + " */");

                if (m_cp.getM_pidl() == false)
                    output.println(tab + "public static " + obj.name() + " unchecked_narrow(org.omg.CORBA.Object obj)");
                else
                    output.println(tab + "public static " + obj.name() + " unchecked_narrow(Object obj)");

                output.println(tab + "{");

                output.println(tab2 + "if (obj == null)");

                output.println(tab3 + "return null;");

                output.println(tab2 + "if (obj instanceof " + obj.name() + ")");

                output.println(tab3 + "return (" + obj.name() + ")obj;");

                output.println("");

                if (!m_cp.getM_pidl() && !((IdlInterface) obj).local_interface()) {
                    output.println(tab2 + "_" + obj.name() + "_Stub stub = new _" + obj.name() + "_Stub();");
                    output.println(tab2 + "stub._set_delegate(((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate());");
                    output.println(tab2 + "return stub;");
                    output.println();
                } else {
                    output.println(tab2 + "throw new org.omg.CORBA.BAD_PARAM();");
                }

                output.println(tab + "}");
                output.println("");
            }
        }

        // Special for value type
        if (obj.kind() == IdlType.e_value) {
            if (((IdlValue) obj).abstract_value() == false) {
                obj.reset();

                while (obj.end() != true) {
                    if (obj.current().kind() == IdlType.e_factory) {
                        output.println(tab + "/**");
                        output.println(tab + " * Create a value type (using factory method)");
                        output.println(tab + " */");
                        output.print(tab + "public static " + obj.name() + " " + obj.current().name() + "(");

                        output.print("org.omg.CORBA.ORB orb");

                        obj.current().reset();

                        while (obj.current().end() != true) {
                            output.print(", ");

                            IdlFactoryMember member = (IdlFactoryMember) obj.current().current();

                            member.reset();
                            translate_type(member.current(), output);
                            output.print(" " + member.name());

                            obj.current().next();
                        }

                        output.println(")");
                        output.println(tab + "{");
                        output.println(tab2 + "org.omg.CORBA.portable.ValueFactory _factory = ((org.omg.CORBA_2_3.ORB)orb).lookup_value_factory(id());");
                        output.println(tab2 + "if ( _factory == null )");
                        output.println(tab3 + "throw new org.omg.CORBA.BAD_INV_ORDER();");
                        output.print(tab2 + "return ( ( " + fullname(obj) + "ValueFactory ) ( _factory ) )." + obj.current().name() + "(");

                        obj.current().reset();

                        while (obj.current().end() != true) {
                            IdlFactoryMember member = (IdlFactoryMember) obj.current().current();

                            member.reset();
                            output.print(" " + member.name());

                            obj.current().next();

                            if (obj.current().end() != true)
                                output.print(", ");
                        }

                        output.println(");");
                        output.println(tab + "}");
                        output.println("");
                    }

                    obj.next();
                }
            }
        }

        // Special for value box
        if (obj.kind() == IdlType.e_value_box) {
            output.println(tab + "/**");
            output.println(tab + " * Read a value from an input stream");
            output.println(tab + " */");
            output.println(tab + "public java.io.Serializable read_value(org.omg.CORBA.portable.InputStream is)");
            output.println(tab + "{");

            translate_unmarshalling(obj, output, "is");

            output.println(tab + "}");
            output.println("");

            output.println(tab + "/**");
            output.println(tab + " * Write a value into an output stream");
            output.println(tab + " */");
            output.println(tab + "public void write_value(org.omg.CORBA.portable.OutputStream os, java.io.Serializable value)");
            output.println(tab + "{");

            translate_marshalling(obj, output, "os", "value");

            output.println(tab + "}");
            output.println("");

            output.println(tab + "/**");
            output.println(tab + " * Return the value id");
            output.println(tab + " */");
            output.println(tab + "public String get_id()");
            output.println(tab + "{");
            output.println(tab2 + "return id();");
            output.println(tab + "}");
            output.println("");

        }

        output.println("}");
        output.close();
    }

    /**
     * Decode a data type
     *
     * @param obj    the data type to decode
     * @param output write access
     * @param inname inputstream name
     */
    public void translate_unmarshalling(IdlObject obj, java.io.PrintWriter output, String inname) {
        int i;
        int idx;

        switch (obj.kind()) {

            case IdlType.e_simple:

                if (((IdlSimple) obj).internal() == Token.t_typecode)
                    output.println(tab2 + "return " + inname + ".read_TypeCode();");

                break;

            case IdlType.e_enum:
                output.println(tab2 + "return " + obj.name() + ".from_int(" + inname + ".read_ulong());");

                break;

            case IdlType.e_struct:
                obj.reset();

                output.println(tab2 + "" + fullname(obj) + " new_one = new " + fullname(obj) + "();");

                output.println("");

                while (obj.end() != true) {
                    obj.current().reset();
                    translate_unmarshalling_member(obj.current().current(), output, inname, "new_one." + obj.current().name(), tab2 + "");
                    obj.next();
                }

                output.println("");
                output.println(tab2 + "return new_one;");
                break;

            case IdlType.e_union:
                idx = ((IdlUnion) obj).index();
                obj.reset();
                output.println(tab2 + "" + fullname(obj) + " new_one = new " + fullname(obj) + "();");
                output.println("");

                obj.current().reset();
                boolean enumeration = false;

                if (final_kind(obj.current().current()) == IdlType.e_enum)
                    enumeration = true;

                IdlObject d = obj.current().current();

                translate_unmarshalling_member(obj.current().current(), output, inname, "new_one._" + obj.current().name(), tab2 + "");

                obj.next();

                String discrim = null;

                if (((IdlUnionMember) obj.current()).getExpression().equals("true ") ||
                    ((IdlUnionMember) obj.current()).getExpression().equals("false ")) {
                    discrim = "new_one.toInt()";
                } else {
                    if (enumeration)
                        discrim = "new_one.__d.value()";
                    else
                        discrim = "new_one.__d";
                }

                i = 0;

                while (obj.end() != true) {
                    if (i != idx) {
                        output.print(tab2 + "if (" + discrim + " == ");

                        if (((IdlUnionMember) obj.current()).getExpression().equals("true "))
                            output.println("1)");
                        else if (((IdlUnionMember) obj.current()).getExpression().equals("false "))
                            output.println("0)");
                        else {
                            if (!enumeration) {
                                output.print("(");
                                translate_type(d, output);
                                output.print(")");
                            }

                            output.println(translate_to_java_expression(((IdlUnionMember) obj.current()).getExpression(), false, ((IdlUnionMember) obj.current())) + ")");
                        }

                        output.println(tab2 + "{");

                        if (((IdlUnionMember) obj.current()).isAsNext() == false) {
                            obj.current().reset();
                            translate_unmarshalling_member(obj.current().current(), output, inname, "new_one._" + obj.current().name(), tab3 + "");
                        } else {
                            IdlObject next = getAsNext(obj);

                            next.reset();
                            translate_unmarshalling_member(next.current(), output, inname, "new_one._" + obj.current().name(), tab3 + "");
                        }

                        output.println(tab2 + "}");

                    }

                    obj.next();

                    if ((obj.end() != true) && ((i + 1) != idx))
                        output.println(tab2 + "else");

                    i++;
                }

                i = 0;
                obj.reset();
                obj.next();

                while (obj.end() != true) {
                    if (i == idx) {
                        if (obj.length() != 2)
                            output.println(tab2 + "else");

                        output.println(tab2 + "{");

                        obj.current().reset();

                        translate_unmarshalling_member(obj.current().current(), output, inname, "new_one._" + obj.current().name(), tab3 + "");

                        output.println(tab2 + "}");

                    }

                    obj.next();

                    i++;
                }

                output.println("");
                output.println(tab2 + "return new_one;");
                break;

            case IdlType.e_typedef:
                obj.reset();

                switch (obj.current().kind()) {

                    case IdlType.e_string:

                    case IdlType.e_wstring:

                    case IdlType.e_simple:

                    case IdlType.e_sequence:

                    case IdlType.e_fixed:

                    case IdlType.e_array:
                        output.print(tab2 + "");
                        translate_type(obj.current(), output);
                        output.println(" new_one;");
                        translate_unmarshalling_member(obj.current(), output, inname, "new_one", tab2 + "");
                        output.println("");
                        output.println(tab2 + "return new_one;");
                        break;

                    default :
                        translate_unmarshalling(obj.current(), output, inname);
                }

                break;

            case IdlType.e_ident:
                translate_unmarshalling(((IdlIdent) obj).internalObject(), output, inname);
                break;

            case IdlType.e_exception:
                obj.reset();
                output.println(tab2 + "" + fullname(obj) + " new_one = new " + fullname(obj) + "();");
                output.println("");
                output.println(tab2 + "if (!" + inname + ".read_string().equals(id()))");
                output.println(tab2 + " throw new org.omg.CORBA.MARSHAL();");

                while (obj.end() != true) {
                    obj.current().reset();
                    translate_unmarshalling_member(obj.current().current(), output, inname, "new_one." + obj.current().name(), tab2 + "");
                    obj.next();
                }

                output.println("");
                output.println(tab2 + "return new_one;");
                break;

            case IdlType.e_native:
                output.println(tab2 + "throw new org.omg.CORBA.MARSHAL();");
                break;

            case IdlType.e_interface:

            case IdlType.e_forward_interface:

                if (((IdlInterface) obj).local_interface()) {
                    output.println(tab2 + "throw new org.omg.CORBA.MARSHAL();");
                    break;
                }

                if (((IdlInterface) obj).abstract_interface()) {
                    output.print(tab2 + "Object new_one = ((org.omg.CORBA_2_3.portable.InputStream)" + inname + ").read_abstract_interface(");

                    String stubname = fullname(obj);

                    if (stubname.lastIndexOf(".") != -1)
                        stubname = stubname.substring(0, stubname.lastIndexOf(".") + 1);
                    else
                        stubname = "";

                    stubname = stubname + "_" + obj.name() + "Stub";

                    output.println(stubname + ".class);");

                    output.println(tab2 + "return (" + fullname(obj) + ") new_one;");
                } else {
                    String stubname = fullname(obj);

                    if (stubname.lastIndexOf(".") != -1)
                        stubname = stubname.substring(0, stubname.lastIndexOf(".") + 1);
                    else
                        stubname = "";

                    stubname = stubname + "_" + obj.name() + "_Stub";

                    output.println(tab2 + "return(" + fullname(obj) + ")" + inname + ".read_Object(" + stubname + ".class);");
                }

                break;

            case IdlType.e_value:

                if (((IdlValue) obj).abstract_value())
                    output.println(tab2 + "return (" + obj.name() + ") ((org.omg.CORBA_2_3.portable.InputStream)istream).read_value(_id);");
                else
                    output.println(tab2 + "return (" + obj.name() + ") ((org.omg.CORBA_2_3.portable.InputStream)istream).read_value(_id);");

                break;

            case IdlType.e_value_box:
                obj.reset();

                if (((IdlValueBox) obj).simple()) {
                    if (is_boolean(obj.current()))
                        output.println(tab2 + "" + fullname(obj) + " _box = new " + fullname(obj) + "(false);");
                    else {
                        output.print(tab2 + "" + fullname(obj) + " _box = new " + fullname(obj) + "((");
                        translate_type(obj.current(), output);
                        output.println(")0);");
                    }

                    translate_unmarshalling_member(obj.current(), output, inname, "_box.value", tab2 + "");
                } else {
                    output.print(tab2 + "");
                    translate_type(obj.current(), output);
                    output.println(" _box = null;");

                    translate_unmarshalling_member(obj.current(), output, inname, "_box", tab2 + "");
                }

                output.println(tab2 + "return _box;");
                break;
        }
    }

    private void writeThrowException(final java.io.PrintWriter output,
                                     final String indent, final String exceptionName, final String args,
                                     final String causeName) {
        output.print(indent);
        output.print("throw ");

        if (m_cp.getM_jdk1_4()) {
            output.print("(");
            output.print(exceptionName);
            output.print(")");
        }

        output.print("new ");
        output.print(exceptionName);
        output.print("(");
        output.print(args);
        output.print(")");

        if (m_cp.getM_jdk1_4()) {
            output.print(".initCause(");
            output.print(causeName);
            output.print(")");
        }
        output.println(";");
    }

    /**
     * Return the typed member for an union member
     */
    private IdlObject getAsNext(IdlObject obj) {
        int p = obj.pos();

        while (obj.end() != true) {
            IdlUnionMember member = (IdlUnionMember) obj.current();

            if (member.isAsNext() == false) {
                obj.pos(p);
                return member;
            }

            obj.next();
        }

        obj.pos(p);
        return null;
    }

    private boolean is_boolean(IdlObject obj) {
        switch (final_kind(obj)) {

            case IdlType.e_simple:

                if (((IdlSimple) obj).internal() == Token.t_boolean)
                    return true;

            default :
                return false;
        }
    }

}

