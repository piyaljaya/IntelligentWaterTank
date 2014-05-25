// A Hello, World Servlet example.
// Written 3/2001 by Wayne Pollock, Tampa Florida USA.
import java.text.*;
import java.util.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.pi4j.io.gpio.*;
import com.pi4j.io.*;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.rule.Rule;

public class Itank extends HttpServlet
{
   
   final GpioController Gpio=GpioFactory.getInstance();
   final GpioPinDigitalInput pina=Gpio.provisionDigitalInputPin(RaspiPin.GPIO_04,PinPullResistance.PULL_DOWN);
   final GpioPinDigitalInput pinb=Gpio.provisionDigitalInputPin(RaspiPin.GPIO_05,PinPullResistance.PULL_DOWN);  
   int status=0,l=0,x=0,y=0,z=0;
   public void doPost(HttpServletRequest req,HttpServletResponse res)
   throws IOException,ServletException 
   {
        doGet(req,res);
   } 
   public void doGet ( HttpServletRequest req, HttpServletResponse res )
      throws IOException, ServletException
  {
      res.setIntHeader("Refresh",5);//auto refresh page
      res.setContentType( "text/html" );  // Can also use "text/plain" or others.
      PrintWriter out = res.getWriter();
      String action=req.getParameter("action");
      
      // Get The Level
      Level level=new  Level();
      // 
      String file ="/WEB-INF/level.txt";
      ServletContext context=getServletContext();
      String path=context.getRealPath(file);
      //file reader
      File fl=new File(path);
      FileReader fr=new FileReader(fl);
      char [] text=new char[10];
      fr.read(text);

      l=Character.getNumericValue(text[0]);//level
      x=Character.getNumericValue(text[1]);//level
      y=Character.getNumericValue(text[2]);//time 10 
      z=Character.getNumericValue(text[3]);//time 1
      fr.close();
      // changing of level monitor
     if((level.Distance(pina,pinb)==1)&&(x<9)){
      x=x+1;
      }
     if((level.Distance(pina,pinb)==2)&&(y<9)){
      y=y+1;
      }
     if((level.Distance(pina,pinb)==3)&&(z<9)){
      z=z+1;
      }
      //reset all
      if("Reset".equals(action)){
       x=0;y=0;z=0;action="no";
       }
     // file writter
       
      FileWriter writer=new FileWriter(fl);
      writer.write(String.valueOf(level.Distance(pina,pinb)));
      writer.write(String.valueOf(x));
      writer.write(String.valueOf(y));
      writer.write(String.valueOf(z));
      writer.close();
      
      // Jfuzzy Initialization
      String fclfile="/WEB-INF/level.fcl";
      String fclpath=context.getRealPath(fclfile);
      FIS fis=FIS.load(fclpath,true);
      fis.setVariable("level_one",x);
      fis.setVariable("level_two",y);
      fis.setVariable("level_three",z);
      fis.evaluate();
      status=(int)fis.getVariable("status").getValue();

      //Create output (the response):
    
      out.println( "<HTML><HEAD><TITLE>HelloServlet in myServletWAR</TITLE>" );
      out.println("<link rel='stylesheet'type='text/css' href='"+req.getContextPath()+"/cstyle/hello.css'/>");
      out.println("</HEAD>"); 
      out.println( "<BODY>" );
       // table center
      out.println("<center>");
      out.println("<h1>INTELLIGENT WATER TANK<h1>");
      out.println("<table >");
      out.println("<tr><td>");
      out.println("<h1>Water Level</h1></td><td><h1>Current Status</h1></td></tr>");
      out.println("<tr><td class=a><h1>Level "+l+"</h1></td>");
      if((status<11)&&(status>0)){
      out.println("<td class=d><h1>Water Wastage is very HIGH</h1></td></tr>");}
     else if(status>18){
      out.println("<td class=o><h1>No Water Wastage</h1></td></tr>");}
     else{
     out.println("<td class=r><h1>Water Wastage RISK</h1></td></tr>");}
      out.println("</table></center></body></html>");
      out.println("<form action='tank' method='GET'><input type='submit' name='action' value='Reset'/></form>");// To Reset The System 
      out.close();

   }
}

 class Level{
 public int Distance(GpioPinDigitalInput pina,GpioPinDigitalInput pinb){
       int level=0;
       if((pina.getState()==PinState.HIGH)&&(pinb.getState()==PinState.HIGH)){level=2;}
       if((pina.getState()==PinState.HIGH)&&(pinb.getState()==PinState.LOW)){level=3;}
       if((pina.getState()==PinState.LOW)&&(pinb.getState()==PinState.HIGH)){level=1;}
//       if((pina.getState()==PinState.LOW)&&(pinb.getState()==PinState.LOW)){distance=3;}

       return level;
       }
}
