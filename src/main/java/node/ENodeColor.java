package node;

public enum ENodeColor {
    TRANSPORTING(0),
    RECEIVED(1);

    private final int value;

    ENodeColor(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static ENodeColor fromString(String text) {
        for (ENodeColor color : ENodeColor.values()) {
            if (color.name().equalsIgnoreCase(text.trim())) {
                return color;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
