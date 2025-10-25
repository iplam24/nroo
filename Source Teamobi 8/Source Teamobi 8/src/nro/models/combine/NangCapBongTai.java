package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;
import java.util.Arrays;

/**
 * Nâng cấp Bông tai Porata +1 -> +5
 * Thanh toán chỉ bằng Thỏi vàng (ID 457) + Mảnh vỡ bông tai (ID 933)
 * @author rew by ChatGPT
 */
public class NangCapBongTai {

    // IDs vật phẩm
    private static final int ITEM_BONG_TAI_1 = 454;   // +1
    private static final int ITEM_BONG_TAI_2 = 921;   // +2
    private static final int ITEM_BONG_TAI_3 = 1815;  // +3
    private static final int ITEM_BONG_TAI_4 = 1816;  // +4
    private static final int ITEM_BONG_TAI_5 = 1817;  // +5 (đích)
    private static final int ITEM_MANH_VO   = 933;    // Mảnh vỡ bông tai
    private static final int ITEM_THOI_VANG = 457;    // Thỏi vàng (thanh toán)

    // Cấu hình
    private static final int RATIO_BONG_TAI = 50;     // % tỉ lệ thành công cho mọi cấp
    private static final int MANH_VO_NEED   = 9999;   // mảnh cần khi thành công
    private static final int MANH_VO_FAIL   = 99;     // mảnh trừ khi thất bại

    // Số thỏi vàng cần cho từng lần nâng: index = cấp hiện tại - 1
    // +1->+2, +2->+3, +3->+4, +4->+5
    private static final int[] BARS_NEED = {20, 40, 60, 80};

    private static final int[] EARRING_IDS = {
            ITEM_BONG_TAI_1, ITEM_BONG_TAI_2, ITEM_BONG_TAI_3, ITEM_BONG_TAI_4, ITEM_BONG_TAI_5
    };

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() != 2) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Cần 1 Bông tai Porata (cấp 1-4) và " + MANH_VO_NEED + " Mảnh vỡ bông tai",
                    "Đóng");
            return;
        }

        Item earring = null;
        Item manhVo = null;

        for (Item item : player.combineNew.itemsCombine) {
            if (item == null || item.template == null) continue;
            if (isEarring(item.template.id)) {
                earring = item;
            } else if (item.template.id == ITEM_MANH_VO) {
                manhVo = item;
            }
        }

        // Validate
        if (earring == null || manhVo == null) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Cần 1 Bông tai Porata cấp 1-4 và " + MANH_VO_NEED + " Mảnh vỡ bông tai",
                    "Đóng");
            return;
        }

        int curLevel = getEarringLevel(earring.template.id);
        if (curLevel <= 0 || curLevel >= 5) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Chỉ nâng cấp từ [+1] → [+5].\nBông tai hiện tại không hợp lệ.",
                    "Đóng");
            return;
        }

        // Nếu đã có bông tai cấp kế tiếp trong hành trang/trang bị thì chặn
        int nextId = getNextEarringId(earring.template.id);
        if (nextId == -1) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Bông tai đã ở cấp tối đa [+5].", "Đóng");
            return;
        }
        if (InventoryService.gI().findItemBag(player, nextId) != null) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Ngươi đã có Bông tai Porata cấp kế tiếp trong hành trang rồi.",
                    "Đóng");
            return;
        }

        int haveManh = countItemInBag(player, ITEM_MANH_VO);
        int needBars = BARS_NEED[curLevel - 1];
        int haveBars = countItemInBag(player, ITEM_THOI_VANG);

        String npcSay = "|2|Nâng cấp Bông tai Porata [+" + curLevel + "] → [+" + (curLevel + 1) + "]\n\n"
                + "|2|Tỉ lệ thành công: " + RATIO_BONG_TAI + "%\n"
                + "|2|Cần: " + MANH_VO_NEED + " " + manhVo.template.name + "\n"
                + "|2|Cần: " + needBars + " Thỏi vàng\n"
                + "|7|Thất bại: -" + MANH_VO_FAIL + " " + manhVo.template.name + "\n";

        if (haveManh < MANH_VO_NEED) {
            npcSay += "Còn thiếu " + (MANH_VO_NEED - haveManh) + " " + manhVo.template.name;
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
            return;
        }

        if (haveBars < needBars) {
            npcSay += "Còn thiếu " + (needBars - haveBars) + " Thỏi vàng";
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
            return;
        }

        // set thông tin combine (không dùng vàng/ngọc nữa)
        player.combineNew.goldCombine = 0;
        player.combineNew.gemCombine = 0;
        player.combineNew.ratioCombine = RATIO_BONG_TAI;

        CombineService.gI().baHatMit.createOtherMenu(
                player,
                ConstNpc.MENU_START_COMBINE,
                npcSay,
                "Nâng cấp\n" + needBars + " Thỏi vàng",
                "Từ chối"
        );
    }

    public static void nangCapBongTai(Player player) {
        if (player.combineNew.itemsCombine.size() != 2) return;

        Item earring = null;
        Item manhVo = null;

        for (Item item : player.combineNew.itemsCombine) {
            if (item == null || item.template == null) continue;
            if (isEarring(item.template.id)) earring = item;
            else if (item.template.id == ITEM_MANH_VO) manhVo = item;
        }
        if (earring == null || manhVo == null) return;

        int curLevel = getEarringLevel(earring.template.id);
        if (curLevel <= 0 || curLevel >= 5) return;

        int needBars = BARS_NEED[curLevel - 1];

        // Kiểm tra đủ mảnh & thỏi vàng
        if (countItemInBag(player, ITEM_MANH_VO) < MANH_VO_NEED) {
            Service.gI().sendThongBao(player, "Không đủ Mảnh vỡ bông tai để nâng cấp");
            return;
        }
        if (countItemInBag(player, ITEM_THOI_VANG) < needBars) {
            Service.gI().sendThongBao(player, "Không đủ Thỏi vàng để nâng cấp");
            return;
        }

        // Trừ thỏi vàng trước (phí nâng)
        removeItemFromBag(player, ITEM_THOI_VANG, needBars);

        boolean success = Util.isTrue(RATIO_BONG_TAI, 100);
        if (success) {
            // Thành công: trừ 9999 mảnh, nâng cấp item
            removeItemFromBag(player, ITEM_MANH_VO, MANH_VO_NEED);

            int nextId = getNextEarringId(earring.template.id);
            earring.template = ItemService.gI().getTemplate(nextId);
            earring.itemOptions.clear();
            // Option 72: + cấp (giữ phong cách cũ: cấp tương ứng)
            earring.itemOptions.add(new Item.ItemOption(72, curLevel + 1));

            CombineService.gI().sendEffectSuccessCombine(player);
        } else {
            // Thất bại: trừ 99 mảnh
            removeItemFromBag(player, ITEM_MANH_VO, MANH_VO_FAIL);
            CombineService.gI().sendEffectFailCombine(player);
        }

        // Cập nhật túi
        InventoryService.gI().sendItemBags(player);
        // Không sendMoney vì không dùng vàng/ngọc nữa, nhưng vẫn reopen UI
        CombineService.gI().reOpenItemCombine(player);
    }

    // ==== Helpers ====

    private static boolean isEarring(int id) {
        return Arrays.stream(EARRING_IDS).anyMatch(x -> x == id);
    }

    private static int getEarringLevel(int id) {
        if (id == ITEM_BONG_TAI_1) return 1;
        if (id == ITEM_BONG_TAI_2) return 2;
        if (id == ITEM_BONG_TAI_3) return 3;
        if (id == ITEM_BONG_TAI_4) return 4;
        if (id == ITEM_BONG_TAI_5) return 5;
        return -1;
    }

    private static int getNextEarringId(int id) {
        if (id == ITEM_BONG_TAI_1) return ITEM_BONG_TAI_2;
        if (id == ITEM_BONG_TAI_2) return ITEM_BONG_TAI_3;
        if (id == ITEM_BONG_TAI_3) return ITEM_BONG_TAI_4;
        if (id == ITEM_BONG_TAI_4) return ITEM_BONG_TAI_5;
        return -1; // đã tối đa
    }

    private static int countItemInBag(Player player, int itemId) {
        int count = 0;
        for (Item item : player.inventory.itemsBag) {
            if (item != null && item.template != null && item.template.id == itemId) {
                count += item.quantity;
            }
        }
        return count;
    }

    public static void removeItemFromBag(Player player, int itemId, int amount) {
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
