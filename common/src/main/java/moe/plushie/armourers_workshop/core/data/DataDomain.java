package moe.plushie.armourers_workshop.core.data;

public enum DataDomain {
    LOCAL("fs"), DEDICATED_SERVER("ws"), DATABASE("db"), DATABASE_LINK("ln"), GLOBAL_SERVER("ks"), GLOBAL_SERVER_PREVIEW("kv"), SLICE_LOAD("sp");

    private final String namespace;

    DataDomain(String namespace) {
        this.namespace = namespace;
    }

    public static DataDomain byName(String path) {
        var namespace = getNamespace(path);
        for (var domain : values()) {
            if (domain.namespace.equals(namespace)) {
                return domain;
            }
        }
        return LOCAL;
    }

    public static String getNamespace(String path) {
        int index = path.indexOf(":");
        if (index < 0) {
            return "";
        }
        return path.substring(0, index);
    }

    public static String getPath(String path) {
        int index = path.indexOf(":");
        if (index < 0) {
            return path;
        }
        return path.substring(index + 1);
    }

    public static boolean isLocal(String path) {
        return LOCAL.matches(path);
    }

    public static boolean isServer(String path) {
        return DEDICATED_SERVER.matches(path);
    }

    public static boolean isDatabase(String path) {
        return DATABASE.matches(path) || DATABASE_LINK.matches(path);
    }

    public static boolean isVolatile(String path) {
        return isLocal(path) || isServer(path);
    }

    public boolean matches(String s) {
        return s.startsWith(namespace + ":");
    }

    public String normalize(String s) {
        return namespace + ":" + s;
    }

    public String namespace() {
        return namespace;
    }
}
