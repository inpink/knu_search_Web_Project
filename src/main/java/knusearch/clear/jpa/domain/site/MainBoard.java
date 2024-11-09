package knusearch.clear.jpa.domain.site;

public enum MainBoard implements Board {
    NOTICE("공지사항", "f19069e6134f8f8aa7f689a4a675e66f.do"),
    EVENT_GUIDANCE("행사/안내", "e4058249224f49ab163131ce104214fb.do");

    private final String name;
    private final String encryptedName;

    MainBoard(String name, String encryptedName) {
        this.name = name;
        this.encryptedName = encryptedName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getEncryptedName() {
        return encryptedName;
    }
}
