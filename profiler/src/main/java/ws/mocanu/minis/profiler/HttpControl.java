/*
Copyright 2019 Bogdan Mocanu (https://bogdan.mocanu.ws)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package ws.mocanu.minis.profiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * Provides a very simple view and control interface over HTTP for a particular profiler.
 *
 * Code modified from:
 * <a href="https://medium.com/@ssaurel/create-a-simple-http-web-server-in-java-3fc12b29d5fd">
 * Sylvain Saurel's post on how to: Create a simple HTTP Web Server in Java
 * </a>.
 */
public class HttpControl extends Thread {

    private Profiler controlledProfiler;
    private boolean keepRunning = true;
    private int listeningPort;

    public void init(int listeningPort, Profiler controlledProfiler) {
        this.setDaemon(true);
        this.listeningPort = listeningPort;
        this.controlledProfiler = controlledProfiler;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            HttpControl.this.prepareToStop();
            try {
                HttpControl.this.interrupt();
                HttpControl.this.join();
            } catch (InterruptedException ignored) {
                ignored.printStackTrace();
            }
        }));
    }

    public void prepareToStop() {
        keepRunning = false;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverConnect = new ServerSocket(listeningPort);
            // we listen until user halts server execution
            while (keepRunning) {
                Socket clientSocket = serverConnect.accept();
                handleClientConnection(clientSocket);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Profiler HTTP control error: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------------------------------------------

    private static final String STATUS_NOT_FOUND = "404 File Not Found";
    private static final String STATUS_OK = "200 OK";
    private static final String STATUS_REDIRECT = "302 Found";
    private static final String CT_TEXT_HTML = "text/html";
    private static final String CT_TEXT_PLAIN = "text/plain";
    private static final String BODY_REPORT_WITH_BUTTONS =
        "<html><body style='background-color: #AAA;'>"
        + "<textarea style='width: 100%; height: 600px; margin-bottom: 5px;'>{report}</textarea>"
        + "<form action='/reset' method='POST'><input type='submit' value='Reset profiler' style='border: solid 3px #FF9; background-color: #FF9; color: #000;'/></form>"
        + "</body></html>";

    private void handleClientConnection(Socket socket) {
        // we manage our particular client connection
        BufferedReader in = null;
        PrintWriter out = null;

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());

            String input = in.readLine();
            if (input == null) {
                return;
            }

            // we parse the request with a string tokenizer
            StringTokenizer parse = new StringTokenizer(input);
            String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
            // we get file requested
            String path = parse.nextToken().toLowerCase();
            if ("GET".equals(method) || "POST".equals(method)) {
                switch (path) {
                    case "/": {
                        handleDefaultCommand(out);
                        break;
                    }
                    case "/reset": {
                        handleResetCommand(out);
                        break;
                    }
                    case "/report": // fall through
                    case "/text": {
                        handleReportCommand(out);
                        break;
                    }
                    default: {
                        handleUnknownCommand(out);
                    }
                }
            } else {
                handleUnknownCommand(out);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Profiler HTTP control error: " + e.getMessage());
        } finally {
            try {
                in.close();
                out.close();
                socket.close(); // we close socket connection
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Profiler HTTP control error: " + e.getMessage());
            }
        }
    }

    private void streamResponse(String statusCode, String contentType, String bodyContent, String customLocation, PrintWriter out) {
        out.println("HTTP/1.1 " + statusCode);
        out.println("Server: Profiler HTTP Control");
        out.println("Date: " + new Date());
        if (contentType != null) {
            out.println("Content-type: " + contentType);
        }
        if (customLocation != null) {
            out.println("Location: " + customLocation);
        }
        if (bodyContent != null) {
            out.println("Content-length: " + bodyContent.length());
            out.println(); // blank line between headers and content, very important !
            out.flush(); // flush character output stream buffer
            out.println(bodyContent);
        }
        out.flush();
    }

    private void handleDefaultCommand(PrintWriter out) {
        String reportAsString = getProfilerReportAsString();
        streamResponse(STATUS_OK, CT_TEXT_HTML, BODY_REPORT_WITH_BUTTONS.replace("{report}", reportAsString), null, out);
    }

    private void handleReportCommand(PrintWriter out) {
        String reportAsString = getProfilerReportAsString();
        streamResponse(STATUS_OK, CT_TEXT_PLAIN, reportAsString, null, out);
    }

    private void handleResetCommand(PrintWriter out) {
        controlledProfiler.reset();
        streamResponse(STATUS_REDIRECT, null, null, "/", out);
    }

    private void handleUnknownCommand(PrintWriter out) {
        streamResponse(STATUS_NOT_FOUND, null, null, null, out);
    }

    private String getProfilerReportAsString() {
        try (final StringWriter stringWriter = new StringWriter()) {
            controlledProfiler.printReport((format, args) -> stringWriter.write(String.format(format, args) + "\n"));
            return stringWriter.toString();
        } catch (IOException e) {
            System.err.println("Profiler HTTP control error: " + e.getMessage());
            return null;
        }
    }

}