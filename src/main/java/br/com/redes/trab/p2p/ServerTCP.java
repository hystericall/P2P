package br.com.redes.trab.p2p;

import java.awt.List;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author RenatoYuzo
 */
public class ServerTCP implements Runnable {

    private final String path;
    private final List listFiles;
    private String ipAddress;
    private final int port=4545;
    private final List textArea;
    private final List textError;
    private String serverMsg;
    private String clientMsg;
    private PrintWriter out;
    private BufferedReader in;
    private BufferedInputStream fileReader;
    private BufferedOutputStream outByte;
    private ServerSocket serverSocket;
    private Socket client;
    private String fileName;

    public ServerTCP(List textArea, List textError, List listFiles, String path, String ipAddress) {
        this.textArea = textArea;
        this.textError = textError;
        this.path = path;
        this.ipAddress = ipAddress;
        this.listFiles = listFiles;
    }

    public void open() {
        try {
            // Abrindo um socket no ip e na porta fornecidos pelo ServerUDP
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(ipAddress, port));
            System.out.println("Server IpAddress: " + serverSocket.getInetAddress().getHostAddress());
            System.out.println("Server port: " + serverSocket.getLocalPort());

            // Esperando por uma conexao com algum ClientTCP
            System.out.println("Server criado, esperando conexão.");
            client = serverSocket.accept();
            System.out.println("Server Conectado!");

            // Variaveis para troca de mensagens entre ClientTCP e ServerTCP
            out = new PrintWriter(client.getOutputStream(), true);
            //serverMsg = "Hello from Server!";
            //out.println(serverMsg);

            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            //clientMsg = in.readLine();
            
            fileName = in.readLine();
            
            File file = new File(path + "\\" + fileName);
            System.out.println(path + "\\" + fileName);
            outByte = new BufferedOutputStream(client.getOutputStream());

            if (!file.exists()) {
                outByte.write((byte) 0); // Se arquivo nao existe na pasta, manda um Byte 0
            } else {
                outByte.write((byte) 1); // Se arquivo encontrado na pasta, manda um Byte 1
                fileReader = new BufferedInputStream(new FileInputStream(file));
                byte[] buffer = new byte[1024];
                int bytesRead = 0;
                while ((bytesRead = fileReader.read(buffer)) != -1) {
                    outByte.write(buffer, 0, bytesRead);
                    outByte.flush();
                }

            }

            //serverMsg = "File " + clientMsg + " has been successfully downloaded.";
            closeConnection();

        } catch (IOException ex) {
            ex.printStackTrace();
            closeConnection();
        }

    }

    public void closeConnection() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
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
    
    public String getSelectedFile() {
        String[] separated;

        for (int i = 0; i < listFiles.getItemCount(); i++) {
            if (listFiles.isIndexSelected(i)) {
                separated = listFiles.getItem(i).split(" ");
                return separated[1];
            }
        }
        return null;
    }

    @Override
    public void run() {
        open();
    }

}
