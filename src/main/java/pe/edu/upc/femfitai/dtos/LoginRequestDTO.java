package pe.edu.upc.femfitai.dtos;

public class LoginRequestDTO {
    private String correo;
    private String password;

    public LoginRequestDTO() {
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
