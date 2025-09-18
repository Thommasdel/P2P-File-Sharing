package eachare; 

public enum PeerStatus {
    ONLINE("O peer está online e disponível."),
    OFFLINE("O peer está offline e indisponível.");

    private final String description;

    private PeerStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name();
    }
}