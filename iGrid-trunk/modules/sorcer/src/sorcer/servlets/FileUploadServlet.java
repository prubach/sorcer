/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sorcer.servlets;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * This is a basic file upload servlet. It will handle file uploads, as
 * performed by Netscape 3 and 4. Note: This program does not implement RFC
 * 1867, merely a subset of it.
 */
public class FileUploadServlet extends HttpServlet {
	// By default write an uploaded file into a tmpFile file
	static protected boolean writeInFile = true;

	// By default, 1Mb max file size per file;
	protected int maxSize = 1024 * 1024 * 1; // 1MB

	public void init(ServletConfig sc) {
		String max = sc.getInitParameter("maxSize");
		try {
			maxSize = Integer.parseInt(max);
		} catch (NumberFormatException nfe) {
			// Do nothing, leave at default value
		}
	}

	/**
	 * Since fileupload requires POST, we only override this method.
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		Hashtable table;

		if (!req.getContentType().toLowerCase().startsWith(
				"multipart/form-data")) {
			// Since this servlet only handles File Upload, bail out
			// Note: this isn't strictly legal - I should send
			// a custom error message
			sendFailure(res, "Wrong content type set for file upload");
			return;
		}

		if (req.getContentLength() > maxSize) {
			sendFailure(res,
					"Content too long - sent files are limited in size");
			return;
		}

		int ind = req.getContentType().indexOf("boundary=");
		if (ind == -1) {
			sendFailure(res, "boundary is not set");
			return;
		}

		String boundary = req.getContentType().substring(ind + 9);

		if (boundary == null) {
			sendFailure(res, "boundary is not set");
			return;
		}

		try {
			table = parseMulti(boundary, req.getInputStream());
		} catch (Throwable t) {
			t.printStackTrace(new PrintWriter(res.getOutputStream()));
			return;
		}

		ServletOutputStream out = res.getOutputStream();
		out.println("<HTML><HEAD><TITLE>FileUpload Output");
		out.println("</TITLE></HEAD><BODY>");
		out.println("<h1>Response from your input:</h1>");
		for (Enumeration fields = table.keys(); fields.hasMoreElements();) {
			String name = (String) fields.nextElement();
			out.println("<b>" + name + ":</b><br>");
			Object obj = table.get(name);
			if (obj instanceof Hashtable) {
				// its a file!
				Hashtable filehash = (Hashtable) obj;
				out.println("<hr>Filename: " + filehash.get("filename")
						+ "<br>");
				out.println("Content-Type: " + filehash.get("content-type")
						+ "<br>");
				obj = filehash.get("content");
				byte[] bytes = (byte[]) obj;
				out.println("Contents: ");
				out.write(bytes, 0, bytes.length);
				out.println();
				out.println("<BR>");
			} else if (obj instanceof String[]) {
				String[] values = (String[]) obj;
				for (int i = 0; i < values.length; i++)
					out.println(values[i] + "<br>");
			}
		}

		out.flush();
		return;
	}

	/**
	 * Obtain information on this servlet.
	 * 
	 * @return String describing this servlet.
	 */
	public String getServletInfo() {
		return "File upload servlet -- used to receive files";
	}

	protected Hashtable parseMulti(String boundary, ServletInputStream in)
			throws IOException {
		return parseMulti(boundary, in, ".");
	}

	/**
	 * The meat of the servlet. This method parses the input, and returns a
	 * hashtable of either String[] values (for parameters) or Hashtable values
	 * (for files uploaded). The values of the entries in the hashtable are
	 * name, filename, Content-Type, and Contents. Note that uploads should be
	 * capped in size by the calling method, since otherwise a denial of service
	 * attack on server memory becomes trivial.
	 */
	protected Hashtable parseMulti(String boundary, ServletInputStream in,
			String tmpDir) throws IOException {

		int buffSize = 1024 * 8; // 8K
		Hashtable hash = new Hashtable();
		int result;
		String line;
		String lowerline;
		String boundaryStr = "--" + boundary;
		OutputStream content;
		String filename;
		String contentType;
		String name;
		String value;
		// uploaded file written by this method if writeInFile is true
		String ufn = null;

		byte[] b = new byte[buffSize];

		result = in.readLine(b, 0, b.length);
		// failure.
		if (result == -1)
			throw new IllegalArgumentException("InputStream truncated");
		line = new String(b, 0, result, "ISO8859_1");
		// failure.
		if (!line.startsWith(boundaryStr))
			throw new IllegalArgumentException("MIME boundary missing: " + line);
		while (true) {
			// Some initialization
			filename = null;
			contentType = null;
			name = null;

			// get next line (should be content disposition)
			result = in.readLine(b, 0, b.length);
			if (result == -1)
				return hash;
			line = new String(b, 0, result - 2, "ISO8859_1");
			lowerline = line.toLowerCase();
			if (!lowerline.startsWith("content-disposition"))
				// don't know what to do, so we'll keep looking...
				continue;
			// determine what the disposition is
			int ind = lowerline.indexOf("content-disposition: ");
			int ind2 = lowerline.indexOf(";");
			if (ind == -1 || ind2 == -1)
				throw new IllegalArgumentException(
						"Content Disposition line misformatted: " + line);
			String disposition = lowerline.substring(ind + 21, ind2);
			if (!disposition.equals("form-data"))
				throw new IllegalArgumentException("Content Disposition of "
						+ disposition + " is not supported");
			// determine what the name is
			int ind3 = lowerline.indexOf("name=\"", ind2);
			int ind4 = lowerline.indexOf("\"", ind3 + 7);
			if (ind3 == -1 || ind4 == -1)
				throw new IllegalArgumentException(
						"Content Disposition line misformatted: " + line);
			name = line.substring(ind3 + 6, ind4);
			// determine filename, if any
			int ind5 = lowerline.indexOf("filename=\"", ind4 + 2);
			int ind6 = lowerline.indexOf("\"", ind5 + 10);
			if (ind5 != -1 && ind6 != -1)
				filename = line.substring(ind5 + 10, ind6);

			if (writeInFile && filename != null) {
				if (!tmpDir.endsWith(File.separator)) {
					tmpDir += File.separator;
				}
				ufn = tmpDir + (new File(filename)).getName();
				content = new FileOutputStream(ufn);
				// System.out.println("parseMulti:FileOutputStream=" + ufn);
			} else
				content = new ByteArrayOutputStream();

			// Whew! We now move onto the next line, which
			// will either be blank, or Content-Type, followed by blank.
			result = in.readLine(b, 0, b.length);
			if (result == -1)
				return hash;
			line = new String(b, 0, result - 2, "ISO8859_1"); // -2 to remove
			// \r\n
			lowerline = line.toLowerCase();
			if (lowerline.startsWith("content-type")) {
				int ind7 = lowerline.indexOf(" ");
				if (ind7 == -1)
					throw new IllegalArgumentException(
							"Content-Type line misformatted: " + line);
				contentType = lowerline.substring(ind7 + 1);
				// read blank header line
				result = in.readLine(b, 0, b.length);
				if (result == -1)
					return hash;
				line = new String(b, 0, result - 2, "ISO8859_1"); // -2 to
				// remove
				// \r\n
				if (line.length() != 0) {
					throw new IllegalArgumentException(
							"Unexpected line in MIMEpart header: " + line);
				}
			} else if (line.length() != 0) {
				throw new IllegalArgumentException(
						"Misformatted line following disposition: " + line);
			}

			// read content, implement readahead by one line
			boolean readingContent = true;
			boolean firstLine = true;
			byte[] buffbytes = new byte[buffSize];
			int buffnum = 0;

			result = in.readLine(b, 0, b.length);
			if (result == -1)
				return hash;
			line = new String(b, 0, result, "ISO8859_1");
			if (!line.startsWith(boundaryStr)) {
				System.arraycopy(b, 0, buffbytes, 0, result);
				buffnum = result;
				result = in.readLine(b, 0, b.length);
				if (result == -1)
					return hash;
				line = new String(b, 0, result, "ISO8859_1");
				firstLine = false;
				if (line.startsWith(boundaryStr)) {
					readingContent = false;
				}
			} else {
				readingContent = false;
			}
			while (readingContent) {
				content.write(buffbytes, 0, buffnum);
				System.arraycopy(b, 0, buffbytes, 0, result);
				buffnum = result;
				result = in.readLine(b, 0, b.length);
				if (result == -1)
					return hash;
				line = new String(b, 0, result, "ISO8859_1");
				if (line.startsWith(boundaryStr))
					readingContent = false;
			}

			if (!firstLine) {
				// -2 to trim \r\n
				if (buffnum > 2)
					content.write(buffbytes, 0, buffnum - 2);
			}

			// now set appropriate variable, populate hashtable
			if (filename == null) {
				if (hash.get(name) == null) {
					String[] values = new String[1];
					values[0] = content.toString();
					hash.put(name, values);
				} else {
					Object prevobj = hash.get(name);
					if (prevobj instanceof String[]) {
						String[] prev = (String[]) prevobj;
						String[] newStr = new String[prev.length + 1];
						System.arraycopy(prev, 0, newStr, 0, prev.length);
						newStr[prev.length] = content.toString();
						hash.put(name, newStr);
					} else {
						// now what? I think this breaks the standard.
						throw new IllegalArgumentException(
								"failure in parseMulti hashtable building code");
					}
				}
			} else {
				// Yes, we don't return Hashtable[] for multiple files of same
				// name. AFAIK, that's not allowed.
				Hashtable filehash = new Hashtable(4);
				filehash.put("name", name);
				filehash.put("filename", filename);
				if (contentType == null)
					contentType = "application/octet-stream";
				filehash.put("content-type", contentType);
				if (writeInFile)
					filehash.put("content", ufn);
				else
					filehash.put("content", ((ByteArrayOutputStream) content)
							.toByteArray());
				hash.put(name, filehash);
				if (writeInFile) {
					content.flush();
					content.close();
					// System.out.println("parseMulti:written:=" + ufn);
				}
			}
		}
	}

	private void sendFailure(HttpServletResponse res, String reason)
			throws IOException {

		ServletOutputStream out = res.getOutputStream();

		out.println("<HTML><HEAD>Upload Failure</HEAD><BODY>");
		out.println("<h2>The upload failed, due to:</h2>");
		out.println(reason);
		out.println("<BR>You may wish to inform the system administrator.");
		out.println("</BODY></HTML>");
	}
}
