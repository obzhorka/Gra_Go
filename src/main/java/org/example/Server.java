package org.example;

import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) throws IOException {
        int port = 1234; // Port, na którym serwer będzie nasłuchiwał
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Serwer uruchomiony na porcie " + port);

        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                    String request;
                    while ((request = in.readLine()) != null) {
                        System.out.println("Otrzymano: " + request);
                        out.println("Echo: " + request);
                        if ("koniec".equalsIgnoreCase(request)) {
                            break;
                        }
                    }
                } finally {
                    clientSocket.close();
                }
            }
        } finally {
            serverSocket.close();
        }
    }
}