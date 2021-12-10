// ========================================================================
// Copyright 2004-2004 Mort Bay Consulting Pty. Ltd.
// Copyright 2021 Regione Piemonte
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package it.csi.periferico;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.servlet.ProxyServlet;
import org.mortbay.util.IO;

public class AnalyzerProxy extends ProxyServlet.Transparent {

	public AnalyzerProxy(String prefix, String server, int port) {
		super(prefix, server, port);
	}

	// The code of this function is a modified version of the same method in the file 
	// ProxyServlet.java from jetty-util-6.1.26
	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		boolean doFilter = false;
		int rootDistance = 0;
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		if ("CONNECT".equalsIgnoreCase(request.getMethod())) {
			handleConnect(request, response);
		} else {
			String uri = request.getRequestURI();
			if (request.getQueryString() != null)
				uri += "?" + request.getQueryString();

			URL url = proxyHttpURL(request.getScheme(), request.getServerName(), request.getServerPort(), uri);

			URLConnection connection = url.openConnection();
			connection.setAllowUserInteraction(false);

			// Set method
			HttpURLConnection http = null;
			if (connection instanceof HttpURLConnection) {
				http = (HttpURLConnection) connection;
				http.setRequestMethod(request.getMethod());
				http.setInstanceFollowRedirects(false);
			}

			// check connection header
			String connectionHdr = request.getHeader("Connection");
			if (connectionHdr != null) {
				connectionHdr = connectionHdr.toLowerCase();
				if (connectionHdr.equals("keep-alive") || connectionHdr.equals("close"))
					connectionHdr = null;
			}

			// copy headers
			boolean xForwardedFor = false;
			boolean hasContent = false;
			Enumeration enm = request.getHeaderNames();
			while (enm.hasMoreElements()) {
				// TODO could be better than this!
				String hdr = (String) enm.nextElement();
				String lhdr = hdr.toLowerCase();

				if (_DontProxyHeaders.contains(lhdr))
					continue;
				if (connectionHdr != null && connectionHdr.indexOf(lhdr) >= 0)
					continue;

				if ("content-type".equals(lhdr))
					hasContent = true;

				Enumeration vals = request.getHeaders(hdr);
				while (vals.hasMoreElements()) {
					String val = (String) vals.nextElement();
					if (val != null) {
						connection.addRequestProperty(hdr, val);
						xForwardedFor |= "X-Forwarded-For".equalsIgnoreCase(hdr);
					}
				}
			}

			// Proxy headers
			connection.setRequestProperty("Via", "1.1 (jetty)");
			if (!xForwardedFor) {
				connection.addRequestProperty("X-Forwarded-For", request.getRemoteAddr());
				connection.addRequestProperty("X-Forwarded-Proto", request.getScheme());
				connection.addRequestProperty("X-Forwarded-Host", request.getServerName());
				connection.addRequestProperty("X-Forwarded-Server", request.getLocalName());
			}

			// a little bit of cache control
			String cache_control = request.getHeader("Cache-Control");
			if (cache_control != null
					&& (cache_control.indexOf("no-cache") >= 0 || cache_control.indexOf("no-store") >= 0))
				connection.setUseCaches(false);

			// customize Connection

			try {
				connection.setDoInput(true);

				// do input thang!
				InputStream in = request.getInputStream();
				if (hasContent) {
					connection.setDoOutput(true);
					IO.copy(in, connection.getOutputStream());
				}

				// Connect
				connection.connect();
			} catch (Exception e) {
				_context.log("proxy", e);
			}

			InputStream proxy_in = null;

			// handler status codes etc.
			int code = 500;
			if (http != null) {
				proxy_in = http.getErrorStream();

				code = http.getResponseCode();
				String ct = http.getContentType();
				doFilter = ct != null && ct.toLowerCase().startsWith("text/html");
				if (doFilter) {
					String pathInfo = request.getPathInfo();
					int count = 0;
					for (int i = 0; i < pathInfo.length(); i++) {
						if (pathInfo.charAt(i) == '/') {
							count++;
						}
					}
					rootDistance = count - 1;
				}
				response.setStatus(code, http.getResponseMessage());
			}

			if (proxy_in == null) {
				try {
					proxy_in = connection.getInputStream();
				} catch (Exception e) {
					_context.log("stream", e);
					proxy_in = http.getErrorStream();
				}
			}

			// clear response defaults.
			response.setHeader("Date", null);
			response.setHeader("Server", null);

			// set response headers
			int h = 0;
			String hdr = connection.getHeaderFieldKey(h);
			String val = connection.getHeaderField(h);
			while (hdr != null || val != null) {
				String lhdr = hdr != null ? hdr.toLowerCase() : null;
				if (hdr != null && val != null && !_DontProxyHeaders.contains(lhdr))
					response.addHeader(hdr, val);

				h++;
				hdr = connection.getHeaderFieldKey(h);
				val = connection.getHeaderField(h);
			}
			response.addHeader("Via", "1.1 (jetty)");

			// Handle
			if (proxy_in != null) {
				if (doFilter) {
					ProxyOutputStream pos = new ProxyOutputStream(response.getOutputStream(), rootDistance);
					IO.copy(proxy_in, pos);
					pos.done();
				} else {
					IO.copy(proxy_in, response.getOutputStream());
				}
			}

		}
	}

	private class ProxyOutputStream extends ByteArrayOutputStream {

		private OutputStream out;
		private String prefix = "./";

		ProxyOutputStream(OutputStream out, int levels) {
			this.out = out;
			if (levels > 0) {
				prefix = "../";
				for (int i = 1; i < levels; i++)
					prefix = prefix + "../";
			}
		}

		void done() throws IOException {
			byte[] content = toByteArray();
			String str = new String(content);
			str = str.replaceAll("[ \\t][hH][rR][eE][fF][ \\t]*=[ \\t]*\"[ \\t]*/", " href=\"" + prefix);
			str = str.replaceAll("[ \\t][hH][rR][eE][fF][ \\t]*=[ \\t]*[ \\t]*/", " href=" + prefix);
			str = str.replaceAll("[ \\t][sS][rR][cC][ \\t]*=[ \\t]*\"[ \\t]*/", " src=\"" + prefix);
			str = str.replaceAll("[ \\t][sS][rR][cC][ \\t]*=[ \\t]*[ \\t]*/", " src=" + prefix);
			out.write(str.getBytes());
			out.flush();
		}

	}

}
