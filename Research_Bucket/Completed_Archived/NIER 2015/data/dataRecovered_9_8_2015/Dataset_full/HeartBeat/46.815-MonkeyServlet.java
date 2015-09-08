package org.archive.monkeys.harness;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

/**
 * The Monkey Servlet is responsible for accepting pings from the browser monkey
 * and performing other operations requested by the monkey.
 * @author Eugene Vahlis
 */
public class MonkeyServlet extends HttpServlet {

	/**
	 * Searial version UID
	 */
	private static final long serialVersionUID = 386927649505528063L;

	private MonkeyHeartbeatTracker heartbeat;

	@Override
	/**
	 * Initializes the servlet. A new heart beat tracker is created and started.
	 */
	public void init(ServletConfig conf) throws ServletException {
		try {
			heartbeat = new MonkeyHeartbeatTracker();
			heartbeat.start();
			super.init(conf);
			System.err.println("In monkey servlet init..");
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	/**
	 * Handles a method call by the monkey. The monkey's HTTP request
	 * should contain a parameter 'method' which is used to determine which method to call.
	 * For example: if method is 'test' then doTest will be executed.
	 */
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// get the special method name if any
		String method = request.getParameter("method");
		System.out.println("Got method " + method);
		if (method == null || method.equals("")) {
			// if it's not a special method then handle the regular
			// POST / GET request
			super.service(request, response);
		} else {
			// otherwise try to call the appropriate instance method
			method = Character.toUpperCase(method.charAt(0))
					+ method.substring(1);
			Class[] argTypes = { HttpServletRequest.class,
					HttpServletResponse.class };
			Object[] args = { request, response };
			try {
				Method handler = this.getClass().getMethod("do" + method,
						argTypes);
				handler.invoke(this, args);
			} catch (NoSuchMethodException e) {
				// If an instance method wasn't found
				// return an error code
				response.setContentType("text/html");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println(
						"The method " + method + " is not handled.");
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println("<h1>Hello SimpleServlet</h1>");
	}

	public void doTest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println("<h1>This is doTest</h1>");
	}

	/**
	 * Accepts a ping from the monkey and pings the heart beat tracker.
	 */
	public void doHeartBeat(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.heartbeat.ping();
		response.setContentType("text");
		response.setStatus(200);
		response.getWriter().println("ping OK");
	}

	public void doEcho(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		String l;
		while ((l = request.getReader().readLine()) != null) {
			response.getWriter().println(l);
		}
		response.getWriter().println("oops");
	}

	public void doGetControllerUrl(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		Properties conf = Harness.loadOrCreateProperties();
		JSONObject res = new JSONObject();
		res.put("controller_url", conf.getProperty("controller.url"));
		res.put("linksSubmit_url", conf.getProperty("linksSubmit.url"));
		response.getWriter().println(res.toString());
	}
	
	@Override
	/**
	 * Destroys the servlet. The heart beat tracker is shut down.
	 */
	public void destroy() {
		this.heartbeat.interrupt();
		try {
			this.heartbeat.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.destroy();
	}
}
