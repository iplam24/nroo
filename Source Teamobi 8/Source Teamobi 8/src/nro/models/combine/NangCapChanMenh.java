package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;

public class NangCapChanMenh {
    // ====== Config nhanh ======
private static final int CM_MIN_ID = 1845; // CM cấp 1
private static final int CM_MAX_ID = 1853; // CM cấp tối đa
private static final int GOLD_BAR_COST = 50;
// Hỗ trợ cả 2 ID thỏi vàng tuỳ source (63 hoặc 457)
private static final int[] GOLD_BAR_IDS = {457};

public static void showInfoCombineChanMenh(Player player) {
    if (InventoryService.gI().getCountEmptyBag(player) == 0) {
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                "Hành trang cần ít nhất 1 ô trống", "Đóng");
        return;
    }
    // cần đúng 1 món
    if (player.combineNew.itemsCombine == null || player.combineNew.itemsCombine.size() != 1) {
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                "Hãy đặt đúng 1 món Chân Mệnh", "Đóng");
        return;
    }
    Item it = player.combineNew.itemsCombine.get(0);
    if (it == null || !it.isNotNullItem()) {
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                "Không có vật phẩm hợp lệ", "Đóng");
        return;
    }
    if (it.template.id < CM_MIN_ID || it.template.id > CM_MAX_ID) {
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                "Vật phẩm này không phải Chân Mệnh", "Đóng");
        return;
    }
    if (it.template.id >= CM_MAX_ID) {
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                "Chân Mệnh đã cấp tối đa", "Đóng");
        return;
    }
    // đếm thỏi vàng
    int goldBarCount = 0;
    for (Item b : player.inventory.itemsBag) {
        if (b != null && b.isNotNullItem()) {
            for (int id : GOLD_BAR_IDS) if (b.template.id == id) goldBarCount += b.quantity;
        }
    }
    if (goldBarCount < GOLD_BAR_COST) {
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                "Cần 50 thỏi vàng", "Đóng");
        return;
    }

    String npcSay = "|2|Con muốn nâng cấp Chân Mệnh " + it.template.name + " chứ?\n"
                  + "|7|Tỉ lệ thành công: 50%\n"
                  + "|5|Thành công: +1 cấp\n"
                  + "|1|Thất bại: mất 50 thỏi vàng, giữ Chân Mệnh\n"
                  + "|7|Cần: 50 thỏi vàng";
    // dùng MENU_START_COMBINE sẵn có để confirm
    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
            npcSay, "Nâng cấp", "Từ chối");
}

// Đếm tổng thỏi vàng trong túi
public static int countGoldBars(Player p) {
    int total = 0;
    if (p == null || p.inventory == null || p.inventory.itemsBag == null) return 0;
    for (Item it : p.inventory.itemsBag) {
        if (it != null && it.isNotNullItem()) {
            for (int goldId : GOLD_BAR_IDS) {
                if (it.template.id == goldId) {
                    total += it.quantity;
                    break;
                }
            }
        }
    }
    return total;
}

// Trừ thỏi vàng trong túi theo thứ tự xuất hiện
public static boolean consumeGoldBars(Player p, int need) {
    if (countGoldBars(p) < need) return false;
    int remain = need;
    for (Item it : p.inventory.itemsBag) {
        if (it != null && it.isNotNullItem()) {
            for (int goldId : GOLD_BAR_IDS) {
                if (it.template.id == goldId) {
                    int take = Math.min(it.quantity, remain);
                    it.quantity -= take;
                    remain -= take;
                    if (it.quantity <= 0) {
                        InventoryService.gI().removeItemBag(p, it);
                    }
                    if (remain == 0) {
                        InventoryService.gI().sendItemBags(p);
                        return true;
                    }
                    break;
                }
            }
        }
    }
    InventoryService.gI().sendItemBags(p);
    return remain == 0;
}

public static void nangCapChanMenh(Player player) {
    try {
        if (player.combineNew.itemsCombine == null 
                || player.combineNew.itemsCombine.stream().filter(i -> i != null && i.isNotNullItem()).count() != 1) {
            Service.gI().sendThongBao(player, "Hãy đặt đúng 1 món Chân Mệnh vào ô ghép");
            return;
        }

        Item chanMenh = null;
        for (Item it : player.combineNew.itemsCombine) {
            if (it != null && it.isNotNullItem()) {
                chanMenh = it;
                break;
            }
        }

        if (chanMenh == null || chanMenh.template.id < CM_MIN_ID || chanMenh.template.id > CM_MAX_ID) {
            Service.gI().sendThongBao(player, "Không phải vật phẩm Chân Mệnh");
            return;
        }

        if (chanMenh.template.id >= CM_MAX_ID) {
            Service.gI().sendThongBao(player, "Chân Mệnh đã đạt cấp tối đa!");
            CombineService.gI().reOpenItemCombine(player);
            return;
        }

        // Kiểm tra đủ vàng
        if (countGoldBars(player) < GOLD_BAR_COST) {
            Service.gI().sendThongBao(player, "Cần 50 thỏi vàng");
            return;
        }
        if (!consumeGoldBars(player, GOLD_BAR_COST)) {
            Service.gI().sendThongBao(player, "Không thể trừ thỏi vàng");
            return;
        }

        boolean success;
        try {
            success = nro.models.utils.Util.isTrue(50, 100);
        } catch (Throwable ignore) {
            success = new java.util.Random().nextBoolean();
        }

        if (success) {
    // Tạo item mới +1 cấp
    Item chanMenhMoi = ItemService.gI().createNewItem((short) (chanMenh.template.id + 1));
    int cap = chanMenhMoi.template.id - CM_MIN_ID + 1;
    int percent = 2 * cap;
    chanMenhMoi.itemOptions.add(new ItemOption(50,  percent));
    chanMenhMoi.itemOptions.add(new ItemOption(77,  percent));
    chanMenhMoi.itemOptions.add(new ItemOption(103, percent));

    // 1) Xóa item cũ khỏi túi
    InventoryService.gI().subQuantityItemsBag(player, chanMenh, 1);

    // 2) Thêm item mới vào túi
    InventoryService.gI().addItemBag(player, chanMenhMoi);

    // 3) Lấy reference đúng của item mới trong túi và gán vào khung
    int newIdx = InventoryService.gI().getIndexItemBag(player, chanMenhMoi);
    if (newIdx >= 0) {
        // itemsCombine có 1 ô duy nhất ở index 0 theo logic check ở trên
        player.combineNew.itemsCombine.set(0, player.inventory.itemsBag.get(newIdx));
    }

    CombineService.gI().sendEffectSuccessCombine(player);
    Service.gI().sendThongBao(player, "|7|Nâng cấp thành công! +" + percent + "% SD/HP/KI");

    InventoryService.gI().sendItemBags(player);
    Service.gI().sendMoney(player);
    CombineService.gI().reOpenItemCombine(player); // khung sẽ thấy item mới vì ta đã thay reference
}
 else {
            CombineService.gI().sendEffectFailCombine(player);
            Service.gI().sendThongBao(player, "|2|Nâng cấp thất bại! Đã mất 50 thỏi vàng.");
        }

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        CombineService.gI().reOpenItemCombine(player);

    } catch (Exception e) {
        e.printStackTrace();
        Service.gI().sendThongBao(player, "Có lỗi khi nâng cấp Chân Mệnh");
    }
}

    
}
