/**
 * Ce classe réprésente un serveur FTP en plein fonctionnement
 * 
 * Ce classe réprésente le fonctionnement du serveur FTP en place, où chaque command active une classe differente pour continuer le cycle de vie 
 * 
 */

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Server {

    public static void main(String[] args) throws Exception {
        try (ServerSocket serv = new ServerSocket(21)){
            System.out.println("SERVEUR SOCKET ÉCOUTE SUR LE PORT 21");

            while (true) {
                Socket socket = serv.accept();
                User curUser = new User();
                curUser.setUserSocket(socket);
                System.out.println("CLIENT CONNECTÉ SUR LE PORT "+socket.getPort());
            
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();

                out.write("220 SERVICE PRÊT\r\n".getBytes());

                Scanner scanner = new Scanner(in);
                String option = scanner.nextLine();

                String answer;
                System.out.println(option);
                switch (option.split(" ")[0]) {
                    case "USER":
                        answer = "331 USER NAME OK\r\n";
                        out.write(answer.getBytes());
                        break;
                    case "PASS":
                        answer = "230 USER LOGGED IN\r\n";
                        out.write(answer.getBytes());
                        break;
                    case "QUIT":
                        answer = "bye\r\n";
                        out.write(answer.getBytes());
                        socket.close();
                        break;
                    default:
                        answer = "Bienvenu sur le MIAGE FTP\r\n";
                        out.write(answer.getBytes());
                        break;
                }
            }
        } finally {

        }
    }
}
