package nro.models.consts;

public final class FusionBonus {
    // % sức đánh cộng khi hợp thể theo loại pet
    public static final int NORMAL   = 10; // Đệ tử thường
    public static final int MABU     = 15; // Mabư
    public static final int UUB      = 25; // Uub
    public static final int KID_BEER = 30; // Kid Beer
    public static final int JIREN    = 35; // Kid Jiren

    public static int forPetType(byte typePet) {
        // 0: normal, 1: mabu, 2: uub, 3: kid beer, 4: jiren (theo PetService của anh)
        return switch (typePet) {
            case 1 -> MABU;
            case 2 -> UUB;
            case 3 -> KID_BEER;
            case 4 -> JIREN;
            default -> NORMAL;
        };
    }

    private FusionBonus() {}
}
