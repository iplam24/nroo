package nro.models.minigame;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import nro.models.item.Item;
import nro.models.network.MySession;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.database.PlayerDAO;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 * Tài Xỉu — hỗ trợ cược Thỏi vàng & VND (gộp cùng phiên).
 * - Tick mỗi giây, tự chốt phiên, auto-refresh UI.
 * - Thỏi vàng trả vào item 457, VND cộng vào session.vnd và lưu DB qua PlayerDAO.saveVnd().
 * - Bộ ba: nhà cái ăn. Thuế áp riêng theo quỹ thua từng loại tiền (tuỳ chỉnh).
 */
public class TaiXiuService {

    // ========= Singleton =========
    private static final TaiXiuService I = new TaiXiuService();
    public static TaiXiuService gI() { return I; }

    // ========= Config =========
    private static final int ROUND_SECONDS = 30;
    private static final int HOUSE_TAX_PERCENT_GOLD = 0;  // thuế quỹ thua (thỏi)
    private static final int HOUSE_TAX_PERCENT_VND  = 0;  // thuế quỹ thua (VND)
    private static final short GOLD_BAR_ID = 457;         // thỏi vàng
    public static final int MENU_TX_MAIN = 100;

    // ========= State =========
    public long roundStart = System.currentTimeMillis();
    public long roundEnd   = roundStart + ROUND_SECONDS * 1000L;

    // Pool THỎI
    public int poolTai     = 0;
    public int poolXiu     = 0;

    // Pool VND
    public int poolTaiVnd  = 0;
    public int poolXiuVnd  = 0;

    // Cược theo userId (session) — THỎI
    private final Map<Integer, Integer> betTai    = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> betXiu    = new ConcurrentHashMap<>();

    // Cược theo userId (session) — VND
    private final Map<Integer, Integer> betTaiVnd = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> betXiuVnd = new ConcurrentHashMap<>();

    // Giữ tham chiếu online để trả thưởng
    private final Map<Integer, Player> onlineByUserId = new ConcurrentHashMap<>();

    // Người đang xem màn TX để auto-refresh
    private final Map<Integer, Npc> watching = new ConcurrentHashMap<>();

    // Log kết quả gần nhất
    public int lastD1 = 0, lastD2 = 0, lastD3 = 0;
    public String lastResultText = "Chưa có";

    // ========= Ticker nền =========
    private ScheduledExecutorService exec;
    private final AtomicBoolean started = new AtomicBoolean(false);

    private TaiXiuService() {}

    /** Gọi khi server khởi động */
    public void start() {
        if (started.compareAndSet(false, true)) {
            exec = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "TaiXiu-Ticker");
                t.setDaemon(true);
                return t;
            });
            exec.scheduleAtFixedRate(() -> {
                try { update(); } catch (Throwable ignored) {}
            }, 1000, 1000, TimeUnit.MILLISECONDS);
        }
    }

    /** (tuỳ chọn) gọi khi server tắt */
    public void stop() {
        if (exec != null) {
            exec.shutdownNow();
            exec = null;
            started.set(false);
        }
    }

    // ====== API auto-refresh UI ======
    public void startWatching(Player p, Npc npc) {
        if (p == null || p.getSession() == null || npc == null) return;
        watching.put(p.getSession().userId, npc);
    }
    public void stopWatching(Player p) {
        if (p == null || p.getSession() == null) return;
        watching.remove(p.getSession().userId);
    }

    // ====== Game loop mỗi giây ======
    public void update() {
        long now = System.currentTimeMillis();
        if (now >= roundEnd) {
            resolveRound();
            startNewRound();
        }
        refreshUiTick();
    }

    private void startNewRound() {
        // reset pool + cược THỎI
        poolTai = 0; poolXiu = 0;
        betTai.clear(); betXiu.clear();

        // reset pool + cược VND
        poolTaiVnd = 0; poolXiuVnd = 0;
        betTaiVnd.clear(); betXiuVnd.clear();

        // dọn rác map online
        onlineByUserId.entrySet().removeIf(e -> e.getValue() == null || e.getValue().getSession() == null);

        roundStart = System.currentTimeMillis();
        roundEnd   = roundStart + ROUND_SECONDS * 1000L;
        lastD1 = lastD2 = lastD3 = 0;
        lastResultText = "Đang mở phiên mới...";
    }

    private void resolveRound() {
        lastD1 = Util.nextInt(1, 6);
        lastD2 = Util.nextInt(1, 6);
        lastD3 = Util.nextInt(1, 6);
        int sum = lastD1 + lastD2 + lastD3;

        boolean triple = (lastD1 == lastD2 && lastD2 == lastD3);
        int winner; // -1: nhà cái ăn, 1: TÀI, 2: XỈU

        if (triple) {
            winner = -1;
            lastResultText = "Bộ ba (" + lastD1 + "-" + lastD2 + "-" + lastD3 + "), nhà cái thắng!";
        } else if (sum >= 11) {
            winner = 1;
            lastResultText = "TÀI thắng (" + lastD1 + "-" + lastD2 + "-" + lastD3 + " = " + sum + ")";
        } else {
            winner = 2;
            lastResultText = "XỈU thắng (" + lastD1 + "-" + lastD2 + "-" + lastD3 + " = " + sum + ")";
        }

        // ===== Trả thưởng THỎI =====
        if (winner == 1 && poolTai > 0) {
            payoutGold(betTai, poolTai, poolXiu);
        } else if (winner == 2 && poolXiu > 0) {
            payoutGold(betXiu, poolXiu, poolTai);
        }

        // ===== Trả thưởng VND =====
        if (winner == 1 && poolTaiVnd > 0) {
            payoutVnd(betTaiVnd, poolTaiVnd, poolXiuVnd);
        } else if (winner == 2 && poolXiuVnd > 0) {
            payoutVnd(betXiuVnd, poolXiuVnd, poolTaiVnd);
        }

        // Thông báo kết quả cho người đã tham gia
        announceRoundResult(winner);
    }

    /**
     * Trả thưởng THỎI: hoàn cược + lời (mặc định 2x). Nếu muốn chia thêm quỹ thua theo tỉ lệ,
     * thay prize = bet * 2; bằng prize = bet + bet + share;
     */
    private void payoutGold(Map<Integer, Integer> winners, int sumWinPool, int sumLosePool) {
        int tax = (sumLosePool * HOUSE_TAX_PERCENT_GOLD) / 100;
        int distributable = Math.max(0, sumLosePool - tax);

        for (Map.Entry<Integer, Integer> e : winners.entrySet()) {
            int userId = e.getKey();
            int bet = e.getValue();
            int share = (int) Math.floor((bet * 1.0 / sumWinPool) * distributable);
            int prize = bet * 2; // hoặc: bet + bet + share

            Player p = getOnlineByUserId(userId);
            if (p == null || p.getSession() == null) {
                continue;
            }

            Item stack = InventoryService.gI().findItemBag(p, GOLD_BAR_ID);
            if (stack != null && stack.isNotNullItem()) {
                stack.quantity += prize;
                InventoryService.gI().sendItemBags(p);
            } else if (InventoryService.gI().getCountEmptyBag(p) >= 1) {
                Item bar = ItemService.gI().createNewItem(GOLD_BAR_ID, prize);
                InventoryService.gI().addItemBag(p, bar);
                InventoryService.gI().sendItemBags(p);
            } else {
                Service.gI().sendThongBao(p,
                        "Hành trang không đủ chỗ (cần 1 ô trống) để nhận thưởng Tài Xỉu: "
                                + prize + " thỏi. Chừa chỗ giúp em ở phiên sau nha~");
                continue;
            }

            Service.gI().sendThongBao(p,
                    "Tài Xỉu trả thưởng (THỎI): +" + prize + " thỏi (thuế " + HOUSE_TAX_PERCENT_GOLD + "% quỹ thua)");
        }
    }

    /**
     * Trả thưởng VND: hoàn cược + lời (mặc định 2x). Nếu muốn chia thêm quỹ thua theo tỉ lệ,
     * thay prize = bet * 2; bằng prize = bet + bet + share.
     * Cộng thẳng vào session.vnd và lưu DB qua PlayerDAO.saveVnd().
     */
    private void payoutVnd(Map<Integer, Integer> winners, int sumWinPool, int sumLosePool) {
        int tax = (sumLosePool * HOUSE_TAX_PERCENT_VND) / 100;
        int distributable = Math.max(0, sumLosePool - tax);

        for (Map.Entry<Integer, Integer> e : winners.entrySet()) {
            int userId = e.getKey();
            int bet = e.getValue();
            int share = (int) Math.floor((bet * 1.0 / sumWinPool) * distributable);
            long prize = (long) bet * 2L; // hoặc: bet + bet + share

            Player p = getOnlineByUserId(userId);
            if (p == null || p.getSession() == null) continue;

            addVnd(p, prize); // <-- cộng & save DB
            Service.gI().sendThongBao(p,
                    "Tài Xỉu trả thưởng (VND): +" + formatVnd(prize) + " (thuế " + HOUSE_TAX_PERCENT_VND + "% quỹ thua)");
        }
    }

    /**
     * Đặt cược THỎI.
     */
    public boolean bet(Player p, boolean tai, int amount) {
        Item bag457 = InventoryService.gI().findItemBag(p, GOLD_BAR_ID);
        if (bag457 == null || !bag457.isNotNullItem() || bag457.quantity < amount) {
            Service.gI().sendThongBao(p, "Bạn không đủ thỏi vàng để đặt.");
            return false;
        }
        int uid = p.getSession().userId;

        // Không cho cược 2 cửa trong cùng 1 phiên (THỎI)
        if (tai && betXiu.containsKey(uid)) {
            Service.gI().sendThongBao(p, "Bạn đã cược XỈU rồi, không thể cược TÀI trong phiên này!");
            return false;
        }
        if (!tai && betTai.containsKey(uid)) {
            Service.gI().sendThongBao(p, "Bạn đã cược TÀI rồi, không thể cược XỈU trong phiên này!");
            return false;
        }

        // đảm bảo lúc trả thưởng có chỗ (stack 457 hoặc 1 ô trống)
        boolean hasStack457 = bag457.isNotNullItem();
        boolean hasEmptySlot = InventoryService.gI().getCountEmptyBag(p) >= 1;
        if (!hasStack457 && !hasEmptySlot) {
            Service.gI().sendThongBao(p, "Cần ít nhất 1 ô trống để nhận thưởng.");
            return false;
        }

        // trừ thỏi vàng
        InventoryService.gI().subQuantityItemsBag(p, bag457, amount);
        InventoryService.gI().sendItemBags(p);

        // ghi nhận online để payout
        onlineByUserId.put(uid, p);

        if (tai) {
            betTai.merge(uid, amount, Integer::sum);
            poolTai += amount;
        } else {
            betXiu.merge(uid, amount, Integer::sum);
            poolXiu += amount;
        }

        Service.gI().sendThongBao(p, (tai ? "Đã cược TÀI " : "Đã cược XỈU ") + amount + " thỏi.");
        return true;
    }

    /**
     * Đặt cược VND (dùng session.vnd + PlayerDAO.saveVnd()).
     */
    public boolean betVnd(Player p, boolean tai, int amountVnd) {
        if (p == null || p.getSession() == null) return false;
        if (amountVnd <= 0) {
            Service.gI().sendThongBao(p, "Số tiền VND không hợp lệ.");
            return false;
        }
        int uid = p.getSession().userId;

        // Không cho cược 2 cửa trong cùng 1 phiên (VND)
        if (tai && betXiuVnd.containsKey(uid)) {
            Service.gI().sendThongBao(p, "Bạn đã cược XỈU (VND) rồi, không thể cược TÀI (VND) trong phiên này!");
            return false;
        }
        if (!tai && betTaiVnd.containsKey(uid)) {
            Service.gI().sendThongBao(p, "Bạn đã cược TÀI (VND) rồi, không thể cược XỈU (VND) trong phiên này!");
            return false;
        }

        // trừ VND (session & DB)
        if (getVnd(p) < amountVnd) {
            Service.gI().sendThongBao(p, "Số dư VND không đủ để đặt.");
            return false;
        }
        if (!subVnd(p, amountVnd)) {
            Service.gI().sendThongBao(p, "Không thể trừ VND. Vui lòng thử lại.");
            return false;
        }

        // ghi nhận online để payout
        onlineByUserId.put(uid, p);

        if (tai) {
            betTaiVnd.merge(uid, amountVnd, Integer::sum);
            poolTaiVnd += amountVnd;
        } else {
            betXiuVnd.merge(uid, amountVnd, Integer::sum);
            poolXiuVnd += amountVnd;
        }

        Service.gI().sendThongBao(p, (tai ? "Đã cược TÀI " : "Đã cược XỈU ") + formatVnd(amountVnd) + ".");
        return true;
    }

    public String infoFor(Player p) {
    int uid = p.getSession().userId;
    int myTai   = betTai.getOrDefault(uid, 0);
    int myXiu   = betXiu.getOrDefault(uid, 0);
    int myTaiV  = betTaiVnd.getOrDefault(uid, 0);
    int myXiuV  = betXiuVnd.getOrDefault(uid, 0);
    long remain = Math.max(0, (roundEnd - System.currentTimeMillis()) / 1000);

    // Lấy thỏi trong túi
    Item goldBar = InventoryService.gI().findItemBag(p, GOLD_BAR_ID);
    int goldQty = (goldBar != null && goldBar.isNotNullItem()) ? goldBar.quantity : 0;

    // Lấy VND của chính player hiện tại
    long vndBal = getVnd(p);

    return "Bạn có: " + goldQty + " thỏi vàng | " + formatVnd(vndBal) + "\n"
         + "— THỎI VÀNG —\n"
         + "Tổng TÀI: " + poolTai + " thỏi | Tổng XỈU: " + poolXiu + " thỏi\n"
         + "Bạn đã cược: Tài " + myTai + " | Xỉu " + myXiu + " (thỏi)\n\n"
         + "— VND —\n"
         + "Tổng TÀI: " + formatVnd(poolTaiVnd) + " | Tổng XỈU: " + formatVnd(poolXiuVnd) + "\n"
         + "Bạn đã cược: Tài " + formatVnd(myTaiV) + " | Xỉu " + formatVnd(myXiuV) + "\n\n"
         + "Còn lại: " + remain + " giây\n"
         + "KQ gần nhất: " + lastResultText + "\n"
         + "(Thuế thỏi: " + HOUSE_TAX_PERCENT_GOLD + "% | Thuế VND: " + HOUSE_TAX_PERCENT_VND + "% quỹ thua)\n"
         + "Ấn đóng để thoát đếm thời gian!\n"
         + "Thoát game sẽ mất toàn bộ số vnd và thỏi vàng đang cược!";
}


    // Lấy player online theo userId từ map nội bộ
    private Player getOnlineByUserId(int uid) {
        Player pl = onlineByUserId.get(uid);
        if (pl != null && pl.getSession() != null) return pl;
        onlineByUserId.remove(uid);
        return null;
    }

    // Gửi thông báo kết quả cho người đã tham gia phiên vừa rồi
    private void announceRoundResult(int winner) {
        String dice = lastD1 + "-" + lastD2 + "-" + lastD3;
        String winStr = (winner == 1) ? "TÀI" : (winner == 2) ? "XỈU" : "NHÀ CÁI";

        Set<Integer> participants = new HashSet<>();
        participants.addAll(betTai.keySet());
        participants.addAll(betXiu.keySet());
        participants.addAll(betTaiVnd.keySet());
        participants.addAll(betXiuVnd.keySet());

        for (int uid : participants) {
            Player p = getOnlineByUserId(uid);
            if (p == null || p.getSession() == null) continue;

            int myTai = betTai.getOrDefault(uid, 0);
            int myXiu = betXiu.getOrDefault(uid, 0);
            int myTaiV = betTaiVnd.getOrDefault(uid, 0);
            int myXiuV = betXiuVnd.getOrDefault(uid, 0);
            boolean win = (winner == 1 && (myTai > 0 || myTaiV > 0))
                       || (winner == 2 && (myXiu > 0 || myXiuV > 0));

            String msg = "Tài Xỉu — Kết quả: " + dice + " → " + winStr
                    + "\nBạn đã " + (win ? "THẮNG (đã cộng thưởng)" : "THUA")
                    + "\n(THỎI: Tài " + myTai + " | Xỉu " + myXiu + ")"
                    + "\n(VND : Tài " + formatVnd(myTaiV) + " | Xỉu " + formatVnd(myXiuV) + ")";
            Service.gI().sendThongBao(p, msg);
        }
    }

    private void refreshUiTick() {
        try {
            var it = watching.entrySet().iterator();
            while (it.hasNext()) {
                var e = it.next();
                int uid = e.getKey();
                Npc npc = e.getValue();
                Player pl = getOnlineByUserId(uid);

                if (pl == null || pl.getSession() == null) {
                    it.remove();
                    continue;
                }
                if (pl.idMark.getIndexMenu() != MENU_TX_MAIN) {
                    it.remove();
                    continue;
                }

                String inf = infoFor(pl);
                // Giữ nguyên chữ ký createOtherMenu của project anh
                npc.createOtherMenu(pl, MENU_TX_MAIN, inf /*, ...buttons nếu cần */);
            }
        } catch (Exception ignored) {}
    }

    // ========= VND helpers (session.vnd + PlayerDAO.saveVnd) =========
    public long getVnd(Player p) {
        return (p != null && p.getSession() != null) ? p.getSession().vnd : 0L;
    }

    private void addVnd(Player p, long amount) {
        if (p == null || p.getSession() == null) return;
        if (amount <= 0) return;
        p.getSession().vnd += amount;
        PlayerDAO.saveVnd(p); 
    }

    private boolean subVnd(Player p, long amount) {
        if (p == null || p.getSession() == null) return false;
        if (amount <= 0) return true;
        if (p.getSession().vnd < amount) return false;
        p.getSession().vnd -= amount;
        PlayerDAO.saveVnd(p); 
        return true;
    }

    private String formatVnd(long v) {
        return String.format("%,d VND", v).replace(',', '.');
    }
}
