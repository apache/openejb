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
package org.openejb.entity.cmp.pkgenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.connector.outbound.ManagedConnectionFactoryWrapper;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.tranql.field.FieldAccessor;
import org.tranql.pkgenerator.PrimaryKeyGenerator;
import org.tranql.pkgenerator.PrimaryKeyGeneratorException;
import org.tranql.pkgenerator.SQLPrimaryKeyGenerator;
import org.tranql.sql.jdbc.binding.BindingFactory;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class SQLPrimaryKeyGeneratorWrapper implements PrimaryKeyGenerator, GBeanLifecycle {
    private static final Log log = LogFactory.getLog(SQLPrimaryKeyGeneratorWrapper.class);
    
    private final ManagedConnectionFactoryWrapper connectionFactoryWrapper;
    private final String initSql;
    private final String sql;
    private final Class returnType;
    private PrimaryKeyGenerator delegate;

    public SQLPrimaryKeyGeneratorWrapper(ManagedConnectionFactoryWrapper connectionFactoryWrapper, String initSql, String sql, Class returnType) {
        this.connectionFactoryWrapper = connectionFactoryWrapper;
        this.initSql = initSql;
        this.sql = sql;
        this.returnType = returnType;
    }
    
    public void doStart() throws WaitingException, Exception {
        DataSource dataSource = (DataSource) connectionFactoryWrapper.getProxy();
        
        Connection c = dataSource.getConnection();
        try {
            PreparedStatement updateStatement = c.prepareStatement(initSql);
            try {
                updateStatement.execute();
            } catch (SQLException e) {
                log.warn("Can not initialize SQLPrimaryKeyGeneratorWrapper with query " + initSql, e);
                //ignore... Already existing?
            } finally {
                updateStatement.close();
            }
        } finally {
            c.close();
        }

        delegate = new SQLPrimaryKeyGenerator(dataSource, sql, BindingFactory.getResultBinding(1, new FieldAccessor(0, returnType)));
    }

    public void doStop() throws WaitingException, Exception {
        delegate = null;
    }

    public void doFail() {
        delegate = null;
    }

    public Object getNextPrimaryKey() throws PrimaryKeyGeneratorException {
        return delegate.getNextPrimaryKey();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(SQLPrimaryKeyGeneratorWrapper.class);
        infoFactory.addInterface(PrimaryKeyGenerator.class);
        
        infoFactory.addReference("ManagedConnectionFactoryWrapper", ManagedConnectionFactoryWrapper.class);
        infoFactory.addAttribute("initSQL", String.class, true);
        infoFactory.addAttribute("sql", String.class, true);
        infoFactory.addAttribute("returnType", Class.class, true);

        infoFactory.setConstructor(new String[]{"ManagedConnectionFactoryWrapper", "initSQL", "sql", "returnType"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    
    
}
