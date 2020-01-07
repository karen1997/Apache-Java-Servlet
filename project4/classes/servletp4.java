
package project4;

/* Name: Karen Dorvil
 Course: CNT 4714 â€“ Spring 2019 â€“ Project Four
 Assignment title: A Three-Tier Distributed Web-Based Application
 Date: April 16, 2019
*/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author karen
 */
import javax.servlet.*;
import java.io.*;
import java.sql.*;
import javax.servlet.http.*;
public class servletp4  extends HttpServlet {
    private Connection connect;
    private Statement statement;
    @Override
    public void init(ServletConfig config) throws ServletException{
        super.init(config);
        try{
            Class.forName(config.getInitParameter("driver"));
            connect= DriverManager.getConnection(config.getInitParameter("name"), config.getInitParameter("username"), config.getInitParameter("password"));
            statement= connect.createStatement();
        }
        catch(Exception x){
            throw new UnavailableException(x.getMessage());
        }
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse answer) throws IOException, ServletException{
        String text= request.getParameter("text");
        String lowercasetext= text.toLowerCase();
        String results= null;
        if(lowercasetext.contains("select")){
            try{
                results= Select(lowercasetext);
            }
            catch(SQLException x){
                results= "<span>"+x.getMessage()+"</span>";
            }
        }
        else{
            try{
                results= doUpdate(lowercasetext);
            }
            catch(SQLException x){
                results= "<span>"+x.getMessage()+"</span>";
            }
        }
        HttpSession session= request.getSession();
        session.setAttribute("result", results);
        session.setAttribute("textbox", text);
        RequestDispatcher dispatch= getServletContext().getRequestDispatcher("/index.jsp");
        dispatch.forward(request, answer);
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse answer) throws IOException, ServletException{
        doGet(request, answer);
    }
    public String Select(String text) throws SQLException{
        String results;
        ResultSet table= statement.executeQuery(text);
        ResultSetMetaData mdata= table.getMetaData();
        int colnum= mdata.getColumnCount();
        String opentable= "<div class='container-fluid'><div class='row justify-content-center'><div class='table-responsive-sm-10 table-responsive-md-10 table-responsive-lg-10'><table class='table'>";
        String tablecolhtml= "<thead class='thead-dark'><tr>";
        for(int i=1; i<colnum+1; i++){
            tablecolhtml+= "<th scope='col'>" + mdata.getColumnName(i) + "</th>";
        }
        tablecolhtml+= "</tr></thead>";
        String tablebodyhtml= "<tbody>";
        while(table.next()){
            tablebodyhtml+= "<tr>";
            for(int i=1; i<colnum+1; i++){
                if(i==1){
                    tablebodyhtml+= "<td scope'row'>"+ table.getString(i)+ "</th>";
                }
                else{
                    tablebodyhtml += "<td>" + table.getString(i) + "</th>";
                }
            }
            tablebodyhtml+= "</tr>";
        }
        tablebodyhtml+= "</tbody>";
        String closetable= "</table></div></div></div>";
        results= opentable+ tablecolhtml+ tablebodyhtml+ closetable;
        return results;
    }
    private String doUpdate(String text) throws SQLException{
        String results= null;
        int rowupdated= 0;
        ResultSet initquantity = statement.executeQuery("select COUNT(*) from shipments where quantity >= 100");
	initquantity.next();
        int init100quantity= initquantity.getInt(1);
        statement.executeUpdate("create table beforeupdate like shipments");
        statement.executeUpdate("insert into beforeupdate select * from shipments");
        rowupdated= statement.executeUpdate(text);
        results=  "<div> The statement executed succesfully.</div><div>" + rowupdated + " row(s) affected</div>";
        ResultSet postquantity = statement.executeQuery("select COUNT(*) from shipments where quantity >= 100");
	postquantity.next();
	int post100quantity = postquantity.getInt(1);
	results+= "<div>" + init100quantity + " < " + post100quantity + "</div>";
	if(init100quantity < post100quantity) {
            int affectedrows = statement.executeUpdate("update suppliers set status = status + 5 where snum in ( select distinct snum from shipments left join beforeupdate using (snum, pnum, jnum, quantity) where beforeupdate.snum is null)");
            results += "<div>Business Logic Detected! - Updating Supplier Status</div>";
            results += "<div>Business Logic Updated " + affectedrows + " Supplier(s) status marks</div>";
        }
        statement.executeUpdate("drop table beforeupdate");
	return results;
    }
}
