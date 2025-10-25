package nro.models.npc_list;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nro.models.item.Item;
import nro.models.minigame.TaiXiuService;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.Service;

public class LyTieuNuong extends Npc {

    // ===== Menu IDs =====
    private static final int MENU_PICK_CURRENCY = 90; // chọn đơn vị cược

    // Gold (Thỏi)
    private static final int MENU_TX_TAI_GOLD = 101;
    private static final int MENU_TX_XIU_GOLD = 102;

    // VND
    private static final int MENU_TX_TAI_VND  = 201;
    private static final int MENU_TX_XIU_VND  = 202;

    // Màn chính dùng cho auto-refresh
    private static final int MENU_TX_MAIN = TaiXiuService.MENU_TX_MAIN;

    // ===== Steps =====
    private static final int[] BET_STEPS_GOLD = {10, 20, 50, 100, 200, 500};                     // thỏi vàng
    private static final int[] BET_STEPS_VND  = {1_000, 5_000, 10_000, 20_000, 50_000, 100_000}; // VND

    // Lưu đơn vị đang chọn theo userId: "GOLD" | "VND"
    private final Map<Integer, String> currencyByUid = new ConcurrentHashMap<>();

    public LyTieuNuong(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) return;
        this.createOtherMenu(player, 0,
                "Trò chơi Chọn ai & Tài Xỉu đang diễn ra!\nChọn đơn vị cược trước nha~",
                "Vào Tài Xỉu");
    }

    @Override
    public void confirmMenu(Player pl, int select) {
        if (!canOpenNpc(pl)) return;

        int uid = pl.getSession().userId;

        // ===== MENU GỐC =====
        if (pl.idMark.getIndexMenu() == 0) {
            if (select == 0) {
                this.createOtherMenu(pl, MENU_PICK_CURRENCY,
                        "Chọn đơn vị để đặt cược:",
                        "Thỏi vàng", "VND", "Đóng");
            }
            return;
        }

        // ===== CHỌN ĐƠN VỊ CƯỢC =====
        if (pl.idMark.getIndexMenu() == MENU_PICK_CURRENCY) {
            switch (select) {
                case 0 -> { // GOLD
                    currencyByUid.put(uid, "GOLD");
                    String inf = TaiXiuService.gI().infoFor(pl);
                    this.createOtherMenu(pl, MENU_TX_MAIN,
                            decorateTitle("THỎI VÀNG", inf),
                            "Cập nhật", "Cược TÀI", "Cược XỈU", "Quay lại");
                    TaiXiuService.gI().startWatching(pl, this);
                }
                case 1 -> { // VND
                    currencyByUid.put(uid, "VND");
                    String inf = TaiXiuService.gI().infoFor(pl);
                    this.createOtherMenu(pl, MENU_TX_MAIN,
                            decorateTitle("VND", inf),
                            "Cập nhật", "Cược TÀI", "Cược XỈU", "Quay lại");
                    TaiXiuService.gI().startWatching(pl, this);
                }
                default -> { /* Đóng */ }
            }
            return;
        }

        // ===== MÀN CHÍNH (auto-refresh) =====
        if (pl.idMark.getIndexMenu() == MENU_TX_MAIN) {
            long remain = (TaiXiuService.gI().roundEnd - System.currentTimeMillis()) / 1000;
            if (remain <= 0) { Service.gI().sendThongBao(pl, "Đang chốt phiên, chờ phiên mới..."); return; }

            String cur = currencyByUid.getOrDefault(uid, "GOLD"); // mặc định GOLD nếu chưa có
            switch (select) {
                case 0 -> { // Cập nhật
                    String inf = TaiXiuService.gI().infoFor(pl);
                    this.createOtherMenu(pl, MENU_TX_MAIN,
                            decorateTitle(cur.equals("GOLD") ? "THỎI VÀNG" : "VND", inf),
                            "Cập nhật", "Cược TÀI", "Cược XỈU", "Quay lại");
                    TaiXiuService.gI().startWatching(pl, this);
                }
                case 1 -> { // Cược TÀI theo đơn vị đã chọn
                    TaiXiuService.gI().stopWatching(pl);
                    if (cur.equals("GOLD")) openGoldTai(pl); else openVndTai(pl);
                }
                case 2 -> { // Cược XỈU theo đơn vị đã chọn
                    TaiXiuService.gI().stopWatching(pl);
                    if (cur.equals("GOLD")) openGoldXiu(pl); else openVndXiu(pl);
                }
                default -> { // Quay lại
                    TaiXiuService.gI().stopWatching(pl);
                    this.createOtherMenu(pl, 0,
                            "Trò chơi Chọn ai & Tài Xỉu đang diễn ra!\nChọn đơn vị cược trước nha~",
                            "Vào Tài Xỉu");
                }
            }
            return;
        }

        // ====== MENU MỐC CƯỢC ======
        if (pl.idMark.getIndexMenu() == MENU_TX_TAI_GOLD) { handleGoldMenu(pl, true, select); return; }
        if (pl.idMark.getIndexMenu() == MENU_TX_XIU_GOLD) { handleGoldMenu(pl, false, select); return; }
        if (pl.idMark.getIndexMenu() == MENU_TX_TAI_VND ) { handleVndMenu (pl, true, select); return; }
        if (pl.idMark.getIndexMenu() == MENU_TX_XIU_VND ) { handleVndMenu (pl, false, select); }
    }

    // =================== Helpers ===================

    private String decorateTitle(String currency, String inf) {
        return "Đơn vị: " + currency + "\n" + inf;
    }

    private String titleSideGold(Player pl, boolean isTai) {
        long remain = Math.max(0, (TaiXiuService.gI().roundEnd - System.currentTimeMillis()) / 1000);
        Item goldBar = InventoryService.gI().findItemBag(pl, (short) 457);
        int goldQty = (goldBar != null && goldBar.isNotNullItem()) ? goldBar.quantity : 0;
        return (isTai ? "TÀI (Thỏi vàng)\n" : "XỈU (Thỏi vàng)\n")
                + "• Thỏi trong túi: " + goldQty + "\n"
                + "• Còn lại: " + remain + " giây";
    }

    private String titleSideVnd(Player pl, boolean isTai) {
        long remain = Math.max(0, (TaiXiuService.gI().roundEnd - System.currentTimeMillis()) / 1000);
        long vnd = TaiXiuService.gI().getVnd(pl);
        return (isTai ? "TÀI (VND)\n" : "XỈU (VND)\n")
                + "• Số dư VND: " + formatVnd(vnd) + "\n"
                + "• Còn lại: " + remain + " giây";
    }

    private String[] buildOptionsGold() {
        String[] opts = new String[BET_STEPS_GOLD.length + 2];
        int i = 0;
        for (int v : BET_STEPS_GOLD) opts[i++] = "Đặt " + v + " thỏi";
        opts[i++] = "ALL-IN Thỏi 💥";
        opts[i]   = "Quay lại";
        return opts;
    }

    private String[] buildOptionsVnd() {
        String[] opts = new String[BET_STEPS_VND.length + 2];
        int i = 0;
        for (int v : BET_STEPS_VND) opts[i++] = "Đặt " + formatVnd(v);
        opts[i++] = "ALL-IN VND 💥";
        opts[i]   = "Quay lại";
        return opts;
    }

    // ===== GOLD flow =====
    private void openGoldMain(Player pl) {
        String inf = TaiXiuService.gI().infoFor(pl);
        this.createOtherMenu(pl, MENU_TX_MAIN,
                decorateTitle("THỎI VÀNG", inf),
                "Cập nhật", "Cược TÀI", "Cược XỈU", "Quay lại");
        TaiXiuService.gI().startWatching(pl, this);
    }

    private void handleGoldMenu(Player pl, boolean isTai, int select) {
        long remain = (TaiXiuService.gI().roundEnd - System.currentTimeMillis()) / 1000;
        if (remain <= 0) { Service.gI().sendThongBao(pl, "Đang chốt phiên, chờ phiên mới..."); return; }

        int idxAllIn = BET_STEPS_GOLD.length;
        int idxBack  = BET_STEPS_GOLD.length + 1;

        if (select == idxBack) { openGoldMain(pl); return; }

        if (select == idxAllIn) {
            Item it = InventoryService.gI().findItemBag(pl, (short) 457);
            int amount = (it != null && it.isNotNullItem()) ? it.quantity : 0;
            if (amount <= 0) Service.gI().sendThongBao(pl, "Bạn không còn thỏi vàng để ALL-IN.");
            else if (TaiXiuService.gI().bet(pl, isTai, amount)) {
                Service.gI().sendThongBao(pl, (isTai ? "ALL-IN TÀI " : "ALL-IN XỈU ") + amount + " thỏi 💥");
            }
            openGoldMain(pl);
            return;
        }

        if (select >= 0 && select < BET_STEPS_GOLD.length) {
            int amount = BET_STEPS_GOLD[select];
            boolean ok = TaiXiuService.gI().bet(pl, isTai, amount);
            if (ok) Service.gI().sendThongBao(pl, (isTai ? "Đã cược TÀI " : "Đã cược XỈU ") + amount + " thỏi.");
            openGoldMain(pl);
        }
    }

    private void openGoldTai(Player pl) {
        this.createOtherMenu(pl, MENU_TX_TAI_GOLD, titleSideGold(pl, true), buildOptionsGold());
    }
    private void openGoldXiu(Player pl) {
        this.createOtherMenu(pl, MENU_TX_XIU_GOLD, titleSideGold(pl, false), buildOptionsGold());
    }

    // ===== VND flow =====
    private void openVndMain(Player pl) {
        String inf = TaiXiuService.gI().infoFor(pl);
        this.createOtherMenu(pl, MENU_TX_MAIN,
                decorateTitle("VND", inf),
                "Cập nhật", "Cược TÀI", "Cược XỈU", "Quay lại");
        TaiXiuService.gI().startWatching(pl, this);
    }

    private void handleVndMenu(Player pl, boolean isTai, int select) {
        long remain = (TaiXiuService.gI().roundEnd - System.currentTimeMillis()) / 1000;
        if (remain <= 0) { Service.gI().sendThongBao(pl, "Đang chốt phiên, chờ phiên mới..."); return; }

        int idxAllIn = BET_STEPS_VND.length;
        int idxBack  = BET_STEPS_VND.length + 1;

        if (select == idxBack) { openVndMain(pl); return; }

        if (select == idxAllIn) {
            long vnd = TaiXiuService.gI().getVnd(pl);
            if (vnd <= 0) Service.gI().sendThongBao(pl, "Bạn không còn VND để ALL-IN.");
            else if (TaiXiuService.gI().betVnd(pl, isTai, (int) Math.min(vnd, Integer.MAX_VALUE))) {
                Service.gI().sendThongBao(pl, (isTai ? "ALL-IN TÀI " : "ALL-IN XỈU ") + formatVnd(vnd) + " 💥");
            }
            openVndMain(pl);
            return;
        }

        if (select >= 0 && select < BET_STEPS_VND.length) {
            int amountVnd = BET_STEPS_VND[select];
            boolean ok = TaiXiuService.gI().betVnd(pl, isTai, amountVnd);
            if (ok) Service.gI().sendThongBao(pl, (isTai ? "Đã cược TÀI " : "Đã cược XỈU ") + formatVnd(amountVnd) + ".");
            openVndMain(pl);
        }
    }

    private void openVndTai(Player pl) {
        this.createOtherMenu(pl, MENU_TX_TAI_VND, titleSideVnd(pl, true), buildOptionsVnd());
    }
    private void openVndXiu(Player pl) {
        this.createOtherMenu(pl, MENU_TX_XIU_VND, titleSideVnd(pl, false), buildOptionsVnd());
    }

    private String formatVnd(long v) {
        return String.format("%,d VND", v).replace(',', '.');
    }
}
