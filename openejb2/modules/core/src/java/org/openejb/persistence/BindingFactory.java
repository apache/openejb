/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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
 *    (http://openejb.org/).
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */

/**
 *
 * Originally part of o.o.n.deployment.RDBMS2CMPMappingHelper
 *
 * Second pass at code, based on FeedBack.
 * Moved this chunk of code for Binding determination to it's own factory.
 *
 * Now supports determination based upon both strings and actual objects.
 *
 */
package org.openejb.persistence;

import org.openejb.persistence.jdbc.Binding;
import org.openejb.persistence.jdbc.binding.AsciiStreamBinding;
import org.openejb.persistence.jdbc.binding.BigDecimalBinding;
import org.openejb.persistence.jdbc.binding.BooleanBinding;
import org.openejb.persistence.jdbc.binding.ByteBinding;
import org.openejb.persistence.jdbc.binding.BytesBinding;
import org.openejb.persistence.jdbc.binding.CharacterBinding;
import org.openejb.persistence.jdbc.binding.CharacterStreamBinding;
import org.openejb.persistence.jdbc.binding.DoubleBinding;
import org.openejb.persistence.jdbc.binding.FloatBinding;
import org.openejb.persistence.jdbc.binding.IntBinding;
import org.openejb.persistence.jdbc.binding.LongBinding;
import org.openejb.persistence.jdbc.binding.ObjectBinding;
import org.openejb.persistence.jdbc.binding.ShortBinding;
import org.openejb.persistence.jdbc.binding.StringBinding;


// TODO: A method which takes javaType as an Object for determination
public class BindingFactory {
    /**
     * Determine the proper binding for input and output
     * This is me being lazy and hacky.
     * Need a way to map shit like java.lang.Integer -> IntBinding for Query generation.
     * There has to be a better way to do this
     *
     * @return A Binding Object (typically a subtype such as StringBinding or IntBinding)
     */

    public static Binding getBinding(String javaType, int index, int slot) {
        if (javaType.equals("java.io.InputStream")) {
            // I'm pretty sure this is NOT the right mapping for this binding
            return new AsciiStreamBinding(index, slot);
        } else if (javaType.equals("java.math.BigDecimal")) {
            return new BigDecimalBinding(index, slot);
        } else if (javaType.equals("java.lang.Boolean")) {
            return new BooleanBinding(index, slot);
        } else if (javaType.equals("java.lang.Byte")) {
            return new ByteBinding(index, slot);
        } else if (javaType.equals("byte[]")) {
            // this one is iffy too
            return new BytesBinding(index, slot);
        } else if (javaType.equals("java.lang.CharacterType")) {
            return new CharacterBinding(index, slot);
        } else if (javaType.equals("java.io.Reader")) {
            // this is probably NOT correct either
            return new CharacterStreamBinding(index, slot);
        } else if (javaType.equals("java.lang.Double")) {
            return new DoubleBinding(index, slot);
        } else if (javaType.equals("java.lang.Float")) {
            return new FloatBinding(index, slot);
        } else if (javaType.equals("java.lang.Integer")) {
            return new IntBinding(index, slot);
        } else if (javaType.equals("java.lang.Long")) {
            return new LongBinding(index, slot);
        } else if (javaType.equals("java.lang.Object")) {
            return new ObjectBinding(index, slot);
        } else if (javaType.equals("java.lang.Short")) {
            return new ShortBinding(index, slot);
        } else if (javaType.equals("java.lang.String")) {
            return new StringBinding(index, slot);
        }
        return null;
    }
}
