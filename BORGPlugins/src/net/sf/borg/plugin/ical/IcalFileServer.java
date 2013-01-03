package net.sf.borg.plugin.ical;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Calendar;
import java.util.GregorianCalendar;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class IcalFileServer {

	private static HttpServer server = null;
	
	private static PrefName serverPort = new PrefName("ical-server-port", new Integer(8844));

	public static void start() throws Exception {
		if (server != null)
			stop();
		server = HttpServer.create(new InetSocketAddress(Prefs.getIntPref(serverPort)), 0);
		server.createContext("/icals/borg.ics", new MyHandler());
		server.setExecutor(null);
		server.start();
	}

	public static void stop() {
		if (server != null) {
			server.stop(0);
			server = null;
		}
	}

	public static void main(String[] args) throws Exception {
		start();
	}

	static class MyHandler implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {

			try {
				GregorianCalendar cal = new GregorianCalendar();
				cal.add(Calendar.YEAR, -2);
				String response = AppointmentIcalAdapter.exportIcalToString(cal.getTime());
				
				t.getResponseHeaders().add("Content-Disposition",
						"attachment; filename=borg.ics");
				byte[] bytes = response.getBytes();
				t.sendResponseHeaders(200, bytes.length);

				OutputStream os = t.getResponseBody();
				os.write(bytes);
				os.close();

			} catch (Exception e) {
				Errmsg.getErrorHandler().errmsg(e);
			}

			
		}
	}

}