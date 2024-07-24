package core.startup;


import lombok.Getter;
import lombok.Setter;

public class MiniTomcat {

    @Setter @Getter
    ClassLoader parentClassLoader;

    Object Server;

    public MiniTomcat() {
        System.out.println("[MiniTomcat] mini tomcat loaded");
    }
}
