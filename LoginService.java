public class LoginService {
    String user, pass;

    public void setUser(String user) {
        this.user = user;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public boolean testLogin() {
        if(this.user.equals("MIAGE") && this.pass.equals("CAR")) {
            return true;
        }
        return false;
    }
}
