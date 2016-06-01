package xextension.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Handles one session, i.e. parses the HTTP request and returns the
 * response.
 */
public interface IHTTPSession {

  void execute() throws IOException;

  CookieHandler getCookies();

  Map<String, String> getHeaders();

  String getHeader(String name);

  InputStream getInputStream();

  Method getMethod();

  Map<String, String> getParms();

  String getParameter(String name);

  String getQueryParameterString();

  /**
   * @return the path part of the URL.
   */
  String getUri();

  String getProtocolVersion();

  /**
   * Adds the files in the request body to the files map.
   * 
   * @param files
   *            map to modify
   */
  void parseBody(Map<String, String> files) throws IOException, ResponseException;

  /**
   * Get the remote ip address of the requester.
   * 
   * @return the IP address.
   */
  String getRemoteIpAddress();

  /**
   * Get the remote hostname of the requester.
   * 
   * @return the hostname.
   */
  String getRemoteHostName();
}
