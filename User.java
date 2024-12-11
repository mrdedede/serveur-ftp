import java.net.Socket;

/**
 * Ce classe réprésente le processus de login d'un serveur FTP
 * 
 * 
 * 
 */
public class User {
    String username;
    String password;
    boolean loginStatus;
    Socket userSocket;


    

    void setUserSocket(Socket socket) {
        this.userSocket = socket;
    }

    void setUsername(String username) {
        this.username = username;
        this.updateLoginStatus();
    }

    void setPassword(String password) {
        this.password = password;
        this.updateLoginStatus();
    }

    void updateLoginStatus() {
        if(this.username != null && this.password != null) {
            this.loginStatus = true;
        } else {
            this.loginStatus = false;
        }
    }

    boolean getLoginStatus() {
        return loginStatus;
    }

}