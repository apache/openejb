#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ${package}.ejb;

import javax.ejb.Stateless;

@Stateless
public class CalculatorImpl {
    public int sum (int num1, int num2) {
        return num1 + num2;
    }
    
    public int multiply (int num1, int num2) {
        return num1 * num2;
    }
}
