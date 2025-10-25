package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Mở chỉ số Bông tai Porata cấp 2 -> 5
 * Thanh toán bằng Thỏi vàng (ID 457) + 99 Mảnh hồn (934) + 1 Đá xanh lam (935)
 * Số chỉ số theo cấp: +2:1, +3:2, +4:3, +5:4
 */
public class NangChiSoBongTai {

    // IDs earring
    private static final int ITEM_BT_2 = 921;   // +2
    private static final int ITEM_BT_3 = 1815;  // +3
    private static final int ITEM_BT_4 = 1816;  // +4
    private static final int ITEM_BT_5 = 1817;  // +5

    // Materials
    private static final int ITEM_MANH_HON = 934;   // 99/click
    private static final int ITEM_DA_XANH = 935;    // 1/click
    private static final int ITEM_THOI_VANG = 457;  // thanh toán

    // Ratio
    private static final int RATIO_NANG_CHI_SO = 45; // %
    // Số thỏi vàng cần theo cấp hiện tại (index: level-2) => +2,+3,+4,+5
    private static final int[] BARS_NEED_OPEN = {10, 20, 30, 40};

    // Pool option chỉ số ngẫu nhiên (giữ đúng format cũ)
    private static final byte[] OPTION_POOL = {77, 80, 81, 103, 50, 94, 5};

    // Số chỉ số theo cấp
    private static int statsCountForLevel(int level) {
        // +2:1, +3:2, +4:3, +5:4
        return Math.max(0, level - 1);
    }

    private static int getLevelById(int id) {
        if (id == ITEM_BT_2) return 2;
        if (id == ITEM_BT_3) return 3;
        if (id == ITEM_BT_4) return 4;
        if (id == ITEM_BT_5) return 5;
        return -1;
    }

    private static boolean isAcceptedEarring(int id) {
        return id == ITEM_BT_2 || id == ITEM_BT_3 || id == ITEM_BT_4 || id == ITEM_BT_5;
    }

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() != 3) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Cần 1 Bông tai Porata cấp 2-5, x99 Mảnh hồn bông tai và 1 Đá xanh lam", "Đóng");
            return;
        }

        Item earring = null, manhHon = null, daXanh = null;
        for (Item it : player.combineNew.itemsCombine) {
            if (it == null || !it.isNotNullItem()) continue;
            int id = it.template.id;
            if (isAcceptedEarring(id)) earring = it;
            else if (id == ITEM_MANH_HON) manhHon = it;
            else if (id == ITEM_DA_XANH) daXanh = it;
        }

        if (earring == null || manhHon == null || daXanh == null) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Cần 1 Bông tai Porata cấp 2-5, x99 Mảnh hồn bông tai và 1 Đá xanh lam", "Đóng");
            return;
        }

        int level = getLevelById(earring.template.id);
        if (level < 2 || level > 5) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Bông tai không hợp lệ. Chỉ hỗ trợ mở chỉ số cho [+2 → +5].", "Đóng");
            return;
        }

        int needBars = BARS_NEED_OPEN[level - 2];
        int haveBars = countItemInBag(player, ITEM_THOI_VANG);

        // Không dùng gold/gem hệ thống nữa
        player.combineNew.goldCombine = 0;
        player.combineNew.gemCombine = 0;
        player.combineNew.ratioCombine = RATIO_NANG_CHI_SO;

        String npcSay = "|2|Bông tai Porata [+" + level + "]\n\n"
                + "|2|Tỉ lệ thành công: " + RATIO_NANG_CHI_SO + "%\n"
                + "|2|Khi thành công: +" + statsCountForLevel(level) + " chỉ số ngẫu nhiên\n"
                + "|2|Cần: 99 " + manhHon.template.name + "\n"
                + "|2|Cần: 1 " + daXanh.template.name + "\n"
                + "|2|Cần: " + needBars + " Thỏi vàng\n";

        if (daXanh.quantity < 1) {
            npcSay += "|7|Còn thiếu " + (1 - daXanh.quantity) + " " + daXanh.template.name;
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
            return;
        }
        if (manhHon.quantity < 99) {
            npcSay += "|7|Còn thiếu " + (99 - manhHon.quantity) + " " + manhHon.template.name;
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
            return;
        }
        if (haveBars < needBars) {
            npcSay += "|7|Còn thiếu " + (needBars - haveBars) + " Thỏi vàng";
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
            return;
        }

        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                "Mở chỉ số\n" + needBars + " Thỏi vàng", "Từ chối");
    }

    public static void nangChiSoBongTai(Player player) {
        try {
            if (player.combineNew.itemsCombine.size() != 3) return;

            Item earring = null, manhHon = null, daXanh = null;
            for (Item it : player.combineNew.itemsCombine) {
                if (it == null || !it.isNotNullItem()) continue;
                int id = it.template.id;
                if (isAcceptedEarring(id)) earring = it;
                else if (id == ITEM_MANH_HON) manhHon = it;
                else if (id == ITEM_DA_XANH) daXanh = it;
            }
            if (earring == null || manhHon == null || daXanh == null) return;

            int level = getLevelById(earring.template.id);
            if (level < 2 || level > 5) return;

            int needBars = BARS_NEED_OPEN[level - 2];

            // Kiểm tra tài nguyên
            if (countItemInBag(player, ITEM_THOI_VANG) < needBars) {
                Service.gI().sendThongBao(player, "Không đủ Thỏi vàng để mở chỉ số");
                return;
            }
            if (manhHon.quantity < 99 || daXanh.quantity < 1) {
                Service.gI().sendThongBao(player, "Thiếu nguyên liệu mở chỉ số");
                return;
            }

            // Trừ phí: thỏi vàng + nguyên liệu (dù thành công hay thất bại)
            removeItemFromBag(player, ITEM_THOI_VANG, needBars);

            boolean success = Util.isTrue(RATIO_NANG_CHI_SO, 100);
            if (success) {
                // Thành công: trừ materials
                InventoryService.gI().subQuantityItemsBag(player, manhHon, 99);
                InventoryService.gI().subQuantityItemsBag(player, daXanh, 1);

                // Gán số chỉ số theo cấp
                int statCount = statsCountForLevel(level);
                earring.itemOptions.clear();

                // tạo statCount option ngẫu nhiên, không trùng ID
                Set<Byte> picked = new HashSet<>();
                for (int i = 0; i < statCount; i++) {
                    byte optId;
                    int guard = 0;
                    do {
                        optId = OPTION_POOL[Util.nextInt(0, OPTION_POOL.length - 1)];
                        guard++;
                    } while (picked.contains(optId) && guard < 20);
                    picked.add(optId);

                    byte param = (byte) Util.nextInt(5, 15);
                    earring.itemOptions.add(new Item.ItemOption(optId, param));
                }

                // giữ option 38, 72 như thiết kế cũ
                earring.itemOptions.add(new Item.ItemOption(38, 0));
                earring.itemOptions.add(new Item.ItemOption(72, level));

                CombineService.gI().sendEffectSuccessCombine(player);
                CombineService.gI().baHatMit.npcChat(player, "Chúc mừng con nhé");
            } else {
                // Thất bại: vẫn trừ materials
                InventoryService.gI().subQuantityItemsBag(player, manhHon, 99);
                InventoryService.gI().subQuantityItemsBag(player, daXanh, 1);

                CombineService.gI().sendEffectFailCombine(player);

                String[] failMessages = {
                        "Ủa? Tưởng lần này lên chứ!",
                        "Vàng bạc mày to nhờ!",
                        "Vàng bạc mày nhiều nhờ!",
                        "Hôm nay không hợp để nâng đâu!",
                        "Lại tạch, thôi đừng khóc!",
                        "Làm vài lần nữa là lên!",
                        "Còn nhiều đá mà, đập tiếp đi!",
                        "Hên xui thôi mà!",
                        "Chơi đồ ảo, nhân phẩm thật!"
                };
                String msg = failMessages[Util.nextInt(failMessages.length)];
                CombineService.gI().baHatMit.npcChat(player, msg);
            }

            InventoryService.gI().sendItemBags(player);
            CombineService.gI().reOpenItemCombine(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== Helpers =====
    private static int countItemInBag(Player player, int itemId) {
        int count = 0;
        for (Item item : player.inventory.itemsBag) {
            if (item != null && item.template != null && item.template.id == itemId) {
                count += item.quantity;
            }
        }
        return count;
    }

    private static void removeItemFromBag(Player player, int itemId, int amount) {
        for (int i = 0; i < player.inventory.itemsBag.size(); i++) {
            Item item = player.inventory.itemsBag.get(i);
            if (item != null && item.template != null && item.template.id == itemId) {
                if (item.quantity > amount) {
                    item.quantity -= amount;
                    break;
                } else {
                    amount -= item.quantity;
                    player.inventory.itemsBag.remove(i);
                    i--;
                    if (amount <= 0) break;
                }
            }
        }
    }
}
