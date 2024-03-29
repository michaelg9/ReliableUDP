import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.UnknownHostException;

/*
 * Michael Michaelides s1447836
 */

public class Sender1a {
    
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java Sender1a <RemoteHost> <RemotePort> <Filename>");
            return;
        }
        try {
            int port = Integer.parseInt(args[1]);
            NaiveClient client = new NaiveClient(args[0], port);
            client.sendFile(args[2]);
        } catch (NumberFormatException e) {
            System.err.println("Port should be a number");
        }  catch (PortUnreachableException e) {
            System.err.println("Port "+ args[1] + " unreachable on destination host");
        } catch (UnknownHostException e) {
            System.err.println("Unknown host " +args[0]);
        } catch (SocketException e) {
            System.err.println("Unable to create a udp socket: " + e.toString());
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + args[2]);
        } catch (IOException e) {
            System.err.println("I/O Exception: " + e.toString());
        }  catch (Exception e) {
            System.err.println("Error: " + e.toString());
        }
    }
}
