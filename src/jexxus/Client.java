package jexxus;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.Consumer;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Client {

  private final String ip;
  private int port = Server.DEFAULT_PORT;
  private boolean ssl = false, compressed = false;
  private Connection conn;
  private Consumer<byte[]> messageCallback = (byte[] data) -> {
  };

  public Client(String ip) {
    this.ip = ip;
  }

  public Client port(int port) {
    this.port = port;
    return this;
  }

  public Client useSSL() {
    ssl = true;
    return this;
  }

  public Client compress() {
    compressed = true;
    return this;
  }

  public Client onMessage(Consumer<byte[]> callback) {
    messageCallback = callback;
    if (conn != null) {
      conn.onMessage(messageCallback);
    }
    return this;
  }

  public Client send(byte[] data) {
    conn.send(data);
    return this;
  }

  public Client connect() {
    return connect(1000);
  }

  public Client connect(int timeoutMillis) {
    try {
      SocketFactory socketFactory =
          ssl ? SSLSocketFactory.getDefault() : SocketFactory.getDefault();
      Socket tcpSocket = socketFactory.createSocket();

      if (ssl) {
        final String[] enabledCipherSuites = { "SSL_DH_anon_WITH_RC4_128_MD5" };
        ((SSLSocket) tcpSocket).setEnabledCipherSuites(enabledCipherSuites);
      }

      tcpSocket.connect(new InetSocketAddress(ip, port), timeoutMillis);

      conn = new Connection(tcpSocket, compressed);
      conn.onMessage(messageCallback);
      conn.listen();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return this;
  }

}
