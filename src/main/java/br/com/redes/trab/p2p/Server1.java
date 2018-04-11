package br.com.redes.trab.p2p;

import java.net.Socket;
import java.awt.List;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.ArrayList;

/**
 *
 * @author RenatoYuzo
 */
public class Server1 implements Runnable {

    private Socket client;
    private ServerSocket serverSocket;
    private List textArea;
    private List textError;
    private List listFiles;
    private String serverMsg;
    private String clientMsg;
    //private final String path = "D:\\Desktop\\Shared Files";
    private String path;
    private PrintWriter out;
    private BufferedReader in;
    private BufferedInputStream fileReader;
    private BufferedOutputStream outByte;
    
    public Server1(ServerSocket serverSocket, Socket client, List textArea, List textError, List listFiles, String path) {
        this.serverSocket = serverSocket;
        this.client = client;
        this.textArea = textArea;
        this.textError = textError;
        this.listFiles = listFiles;
        this.path = path;
    }
    

    @Override
    public void run() {
        textArea.add("New connection with Client " + client.getPort() + " " + client.getInetAddress().getHostAddress());

        try {
            out = new PrintWriter(client.getOutputStream(), true);
            //serverMsg = "Hello from Server!";
            //out.println(serverMsg);

            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            clientMsg = in.readLine();

            if (clientMsg.equals("Get Available Files")) {
                ArrayList<String> list;
                list = getFiles();
                
                out.println(Integer.toString(list.size())); // Enviando a quantidade de arquivos para o Client
                
                for (int i=0; i<list.size(); i++) {
                    //System.out.println("list["+i+"]: " + list.get(i));
                    out.println(list.get(i));
                }
                
                closeConnection();
            } 
            else if (clientMsg.equals("Download File")) {
                serverMsg = "Which file do you want to download?";
                out.println(serverMsg);

                clientMsg = in.readLine();
                File file = new File(path + "\\" + clientMsg);
                outByte = new BufferedOutputStream(client.getOutputStream());

                if (!file.exists()) {
                    outByte.write((byte) 0);
                    closeConnection();
                } else {
                    outByte.write((byte) 1);
                    fileReader = new BufferedInputStream(new FileInputStream(file));
                    byte[] buffer = new byte[1024];
                    int bytesRead = 0;
                    while ((bytesRead = fileReader.read(buffer)) != -1) {
                        outByte.write(buffer, 0, bytesRead);
                        outByte.flush();
                    }

                    closeConnection();
                }

                serverMsg = "File " + clientMsg + " has been successfully downloaded.";
                out.println(serverMsg);
            } else if (clientMsg.equals("Exit")) {
                serverMsg = "Ok, Bye!";
                out.println(serverMsg);
                closeConnection();
            }

        } catch (IOException e) {
            textError.add("Error: " + e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            if (client != null) {
                client.close();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (fileReader != null) {
                fileReader.close();
            }
            if (outByte != null) {
                outByte.close();
            }
            textArea.add("Client " + client.getPort() + " has disconnected");
        } catch (IOException e) {
            textError.add("Error: " + e.getMessage());
        }
    }

    public ArrayList<String> getFiles() {

        try {
            File file = new File(path);
            File[] listOfFiles = file.listFiles();
            ArrayList<String> listOfNameFiles = new ArrayList();

            for (File listOfFile : listOfFiles) {
                if (listOfFile.isFile()) {
                    listOfNameFiles.add(serverSocket.getInetAddress().getHostAddress() + " " + listOfFile.getName());
                }                
                
                /*if (listOfFile.isFile()) {
                    listFiles.add(serverSocket.getInetAddress().getHostAddress() + " " + listOfFile.getName());
                }*/
            }
            return listOfNameFiles;
        } catch (Exception e) {
            textError.add("Error: " + e.getMessage());
        }
        return null;

    }

}