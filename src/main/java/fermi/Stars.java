package fermi;

public enum Stars {
    j0218("_FL8YJ0218.1+4232"), j1939("_FL8YJ1939.6+2134"),
    b1821("_FL8YJ1824.6-2452");

    Stars(String name) {
        this.name = name;
    }

    String name;

    public String getName() {
        return name == null ? "_FL8YJ1939.6+2134" : name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
