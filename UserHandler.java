import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class UserHandler {
    Socket userSocket;
    Socket transferSocket;

    UserHandler(Socket userSocket) {
        this.userSocket = userSocket;
    }

    public void handleClient() {
        try (
            InputStream in = this.userSocket.getInputStream();
            OutputStream out = this.userSocket.getOutputStream();
            Scanner scanner = new Scanner(in)
        ) {

            out.write("220 SERVICE PRÊT\r\n".getBytes());
            LoginService ls = new LoginService();

            while (true) {
                if (!scanner.hasNextLine()) {
                    break;
                }

                String commandLine = scanner.nextLine();
                System.out.println("Reçu: " + commandLine);

                String[] commandParts = commandLine.split(" ", 2);

                switch (commandParts[0]) {
                    case "USER":
                        ls.setUser(commandParts[1]);
                        out.write("331 USER NAME OK, INSEREZ LE MOT DE PASSE\r\n".getBytes());
                        break;

                    case "PASS":
                        ls.setPass(commandParts[1]);
                        if (ls.testLogin()) {
                            out.write("230 USER LOGGED IN\r\n".getBytes());
                        } else {
                            out.write("430 USER NAME OU MOT DE PASSE INVALIDE\r\n".getBytes());
                        }
                        break;

                    case "RETR":
                        this.handleRETRCommand(commandParts[1]);
                        transferSocket.close();
                        break;

                    case "EPRT":
                        this.handleEPRTCommand(commandParts[1]);
                        break;

                    case "LIST":
                        if (commandParts.length == 1) {
                            this.handleLISTCommand("user.dir");
                            break;
                        }
                        this.handleLISTCommand(commandParts[1]);
                        transferSocket.close();
                        break;
                    
                    case "CWD":
                        this.handleCWDCommand(commandParts[1]);
                        break;

                    case "QUIT":
                        out.write("221 GOODBYE\r\n".getBytes());
                        userSocket.close();
                        System.out.println("CLIENT DÉCONNECTÉ");
                        return;

                    default:
                        out.write(("200 COMMAND OK: " + commandParts[0] + "\r\n").getBytes());
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur avec le client: " + e.getMessage());
        }
    }

    private void handleRETRCommand(String file) {
        try {
            OutputStream out = this.userSocket.getOutputStream();
            File f = new File(System.getProperty("user.dir") + "/" + file);
            if (!f.exists() || !f.isFile()) {
                out.write("550 LE FICHIER N'EXISTE PAS\r\n".getBytes());
                return;
            }

            out.write("150 FICHIER OK - OUVRIR CONNEXION EN MODE DATA\r\n".getBytes());

            BufferedInputStream fileStream = new BufferedInputStream(new FileInputStream(f));
            BufferedOutputStream sendStream = new BufferedOutputStream(transferSocket.getOutputStream());

            byte[] buffer = new byte[2048];
            int bytesRead;
            while ((bytesRead = fileStream.read(buffer, 0, 2048)) != -1) {
                sendStream.write(buffer, 0, bytesRead);
            }
            sendStream.flush();
            fileStream.close();
            out.write("226 TRANSFERT CONCLU - FERMER CONNEXION EN MODE DATA\r\n".getBytes());
            transferSocket.close();
        } catch (IOException e) {
            System.out.println("Erreur lors de la configuration du flux: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleEPRTCommand(String port) {
        try {
            System.out.println("Client attend des données au  "+ port.split("\\|")[2] + ":" + port.split("\\|")[3]);
            transferSocket = new Socket(port.split("\\|")[2], Integer.parseInt(port.split("\\|")[3]));
            userSocket.getOutputStream().write("200 COMMAND OK: EPRT\r\n".getBytes());
        } catch (IOException e) {
            System.err.println("Erreur EPRT: " + e.getMessage());
        }
    }


    private void handleLISTCommand(String dir) {
        String curDir;
        if(dir != "user.dir") {
            curDir = System.getProperty("user.dir")+"/"+dir;
        } else {
            curDir = System.getProperty(dir);
        }

        File f = new File(curDir);
        
        System.out.println(curDir);
        if(f.exists() && f.isDirectory()) {
            for(int i = 0; i < f.list().length; i++) {
                handleLISTCommand(f.list()[i]);
            }
        } else if (f.exists() && f.isFile()) {
            try {
                transferSocket.getOutputStream().write(curDir.getBytes());
            } catch (Exception e) {
                System.out.println("Error! \n"+e.getMessage());
            }
        }
    }

    private void handleCWDCommand(String dir) {
        String root = System.getProperty("user.dir");
        String filename = System.getProperty("user.dir");

        // go one level up (cd ..)
        if (dir.equals("..")) {
          int ind = filename.lastIndexOf("/");
          if (ind > 0) {
            filename = filename.substring(0, ind);
          }
        }
    
        // if argument is anything else (cd . does nothing)
        else if ((dir != null) && (!dir.equals("."))) {
          filename = filename + "/" + dir;
        }
    
        // check if file exists, is directory and is not above root directory
        File f = new File(filename);
        try {
            if (f.exists() && f.isDirectory() && (filename.length() >= root.length())) {
                String filenameMsg = "250 The current directory has been changed to " + filename + "\r\n";
                transferSocket.getOutputStream().write(filenameMsg.getBytes());
            } else {
                transferSocket.getOutputStream().write("550 Requested action not taken. File unavailable. \r\n".getBytes());
            }
        }  catch (Exception e){
            System.out.println(e);
        }
    }
}