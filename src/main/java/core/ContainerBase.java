package core;

import core.lifecycle.LifecycleBase;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public abstract class ContainerBase extends LifecycleBase implements Container {

    private Container parent;

    private List<Container> children = new ArrayList<>();

    @Override
    public void addChild(Container child) {
        this.children.add(child);
    }

    @Override
    public void removeChild(Container child) {
        this.children.remove(child);
    }

}
