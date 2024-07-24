package core.container;

public interface Container {

    Container getParent();
    void setParent(Container parent);
    void addChild(Container child);
    void removeChild(Container child);
    void backgroundProcess();
}
