#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ${package}.web;


import java.io.IOException;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ${package}.ejb.CalculatorImpl;

public class CalculatorServlet extends HttpServlet {
    @EJB
    private CalculatorImpl calc;
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        String op = request.getParameter("op");
        int num1 = 0;
        int num2 = 0;
        int result = 0;
        
        if (op != null && op.length() > 0) {
            try {
                num1 = Integer.parseInt(request.getParameter("num1"));
                num2 = Integer.parseInt(request.getParameter("num2"));
            } catch (Exception e) {
            }

            if ("sum".equals(op)) {
                result = calc.sum(num1, num2);
            } else if ("multiply".equals(op)) {
                result = calc.multiply(num1, num2);
            }
            
            request.setAttribute("num1", num1);
            request.setAttribute("num2", num2);
            request.setAttribute("op", op);
            request.setAttribute("result", result);
        }
        
        request.getRequestDispatcher("WEB-INF/calculator.jspx").forward(request, response);
    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
