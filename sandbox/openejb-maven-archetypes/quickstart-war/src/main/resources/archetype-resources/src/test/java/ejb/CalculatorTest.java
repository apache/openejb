#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ${package}.ejb;

import org.junit.Test;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.junit.Before;
import static org.junit.Assert.*;

public class CalculatorTest {
    
    private InitialContext context;
    
    @Before
    public void setup() throws Exception {
        Properties p = new Properties();
        p.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
        context = new InitialContext(p);
    }
    
    @Test
    public void testAdd() throws Exception {
        CalculatorImpl calc = (CalculatorImpl) context.lookup("CalculatorImplLocalBean");
        assertEquals(200, calc.sum(100, 100));
        assertEquals(10000, calc.multiply(100, 100));
    }
}
