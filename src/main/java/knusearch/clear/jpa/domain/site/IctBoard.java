package knusearch.clear.jpa.domain.site;

public enum IctBoard implements Board {
    NOTICE("공지사항", "e408e5e7c9f27b8c0d5eeb9e68528b48.do"),
    EVENT_GUIDANCE("학부행사", "e38fb5074d558dd5c570c62c9f36fdce.do"),
    GRADUATION_PRODUCTS("졸업작품", "4560109aa534f393e3f41711841b2e6b.do");

    private final String name;
    private final String encryptedName;

    IctBoard(String name, String encryptedName) {
        this.name = name;
        this.encryptedName = encryptedName;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getEncryptedName() {
        return null;
    }
}
