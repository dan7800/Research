import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import com.vlee.beans.jdbc.DbConnectionPool;

public class SetupDbPoolServlet extends HttpServlet 
{
   private DbConnectionPool pool;

   public void init(ServletConfig config) 
		throws ServletException
   {
      super.init(config);

      ServletContext app = config.getServletContext();
      pool = new DbConnectionPool(
                  config.getInitParameter("jdbcDriver"),
                  config.getInitParameter("jdbcURL"),
                  config.getInitParameter("jdbcUser"),
                  config.getInitParameter("jdbcPwd"));

      app.setAttribute("db-connection-pool", pool);
   }

   public void destroy() 
   {
      pool.shutdown();
      pool = null;
      super.destroy();
   }
}
