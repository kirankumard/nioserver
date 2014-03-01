
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author kdevaras
 */
public class SimpleClient {

    public static void main(String[] args) throws IOException {
        Socket client = new Socket("127.0.0.1", 9006);
        InputStream in = client.getInputStream();
        OutputStream out = client.getOutputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in), 2048);

        BufferedReader scanner = new BufferedReader((new InputStreamReader(System.in)));
        byte[] outData = null;
        char[] cbuf = null;

        long t1, t2;

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(SimpleClient.class.getName()).log(Level.SEVERE, null, ex);
            }
            String line = "sending test input to server.\n"; //scanner.readLine();
            System.out.println("User input: " + line);
            outData = line.getBytes();
            t1 = System.nanoTime();
            out.write(outData);
            System.out.println("Server Response: ");
//            int data = in.read();
//            while (!(in.available() > 0)) {
//                try {
//                    Thread.sleep(5);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(SimpleClient.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//            while (in.available() > 0) {
//                System.out.print(in.read());
////                System.out.println("in.available(): " + in.available());
//            }
//            System.out.println("");
            
            System.out.println(reader.readLine());

            t2 = System.nanoTime();
            System.out.println("time taken in nanos = " + (t2 - t1) + ", in millis = " + (t2 - t1) / 1000000L);

            if (line.equals("Q")) {
                break;
            }
        }

        out.close();
        scanner.close();
        in.close();

    }
}
