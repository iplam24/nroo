package nro.models.daily_Giftcode;

import nro.models.database.PlayerDAO;
import nro.models.player.Player;
import java.util.Map;
import java.util.HashMap;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
public class DailyGiftService {

    // Múi giờ VN để so sánh theo ngày
    private static final ZoneId ZONE_VN = ZoneId.of("Asia/Ho_Chi_Minh");

    // =============== Helpers ===============

    private static boolean isSameDay(long t1, long t2) {
        if (t1 <= 0 || t2 <= 0) return false;
        LocalDate d1 = Instant.ofEpochMilli(t1).atZone(ZONE_VN).toLocalDate();
        LocalDate d2 = Instant.ofEpochMilli(t2).atZone(ZONE_VN).toLocalDate();
        return d1.equals(d2);
    }

    // public wrapper (nếu muốn dùng ngoài Service cho debug)
    public static boolean isSameDayPublic(long t1, long t2) {
        return isSameDay(t1, t2);
    }

    /** Tìm hoặc tạo record theo id (KHÔNG đụng ID khác) */
    static DailyGiftData getOrCreate(Player p, byte id) {
        for (DailyGiftData d : p.dailyGiftData) {
            if (d.id == id) return d;
        }
        DailyGiftData nd = new DailyGiftData();
        nd.id = id;
        nd.daNhan = false;
        nd.lastTime = 0L;
        p.dailyGiftData.add(nd);
        return nd;
    }

    // =============== Lifecycle ===============

    /** Gọi khi login: nạp JSON từ DB -> RAM, đồng bộ daNhan theo lastTime (cùng ngày = đã nhận) */
    public static void loadFromDb(Player p) {
        Map<Byte, Long> dbMap = PlayerDAO.loadDailyGiftsJson(p);
        long now = System.currentTimeMillis();
        for (Map.Entry<Byte, Long> e : dbMap.entrySet()) {
            DailyGiftData d = getOrCreate(p, e.getKey());
            d.lastTime = e.getValue();
            d.daNhan   = isSameDay(d.lastTime, now); // hôm nay đã nhận rồi -> khóa
        }
    }

    /** Reset theo NGÀY cho toàn bộ record đang có (KHÔNG clear list) */
    public static void resetIfNewDay(Player player) {
        long now = System.currentTimeMillis();
        for (DailyGiftData d : player.dailyGiftData) {
            if (!isSameDay(d.lastTime, now)) {
                d.daNhan = false; // qua ngày mới -> mở khóa
            }
        }
    }

    /** Đảm bảo tồn tại các ID cần dùng (tạo nếu thiếu, không clear list) */
    public static void ensureIds(Player p, byte... ids) {
        if (ids == null) return;
        for (byte id : ids) getOrCreate(p, id);
    }

    // =============== API dùng trong menu ===============

    /** true = hôm nay CÒN được nhận; false = đã nhận hôm nay */
 public static boolean checkDailyGift(Player player, byte id) {
    long now = System.currentTimeMillis();

    // Đọc DB mỗi lần check (van an toàn)
    Map<Byte, Long> db = PlayerDAO.loadDailyGiftsJson(player);
    long last = 0L;
    if (db != null) {
        Long t = db.get(id);
        if (t != null) last = t;
    }

    // Quyết định theo ngày VN
    boolean claimedToday = isSameDay(last, now);

    // Đồng bộ RAM để lần sau dùng vẫn đúng
    DailyGiftData d = getOrCreate(player, id);
    d.lastTime = last;
    d.daNhan   = claimedToday;

   

    return !claimedToday;
}


    /** Đánh dấu đã nhận hôm nay + lưu DB */
    public static void updateDailyGift(Player player, byte id) {
        DailyGiftData data = getOrCreate(player, id);
        data.daNhan = true;
        data.lastTime = System.currentTimeMillis();

        // Persist toàn bộ list sang DB (JSON)
        Map<Byte, Long> map = new HashMap<>();
        for (DailyGiftData d : player.dailyGiftData) {
            map.put(d.id, d.lastTime);
        }
        PlayerDAO.saveDailyGiftsJson(player, map);
        
   
    }

    
    public static void addAndReset(Player player) {
        if (player == null) return;
        resetIfNewDay(player);
       
        ensureIds(player, (byte) 4 /* , (byte) 4 */);
    }
    // DailyGiftService.java
    public static void onPostLogin(Player p) {
        if (p == null) return;
        loadFromDb(p);          // nạp JSON từ DB -> RAM (lastTime)
        resetIfNewDay(p);       // nếu qua ngày thì mở khoá
        ensureIds(p, (byte)4);
    }

}
