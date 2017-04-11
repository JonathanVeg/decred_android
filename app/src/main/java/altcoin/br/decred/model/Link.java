package altcoin.br.decred.model;

public class Link {
    private String label;
    private String url;

    public Link(String label, String url) {
        setLabel(label);
        setUrl(url);
    }

    public String getLabel() {
        return label;
    }

    private void setLabel(String label) {
        this.label = label;
    }

    public String getUrl() {
        return url;
    }

    private void setUrl(String url) {
        this.url = url;
    }
}
