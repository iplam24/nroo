package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.consts.ConstPlayer;
import nro.models.database.PlayerDAO;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.PetService;
import nro.models.services.Service;
import nro.models.services.PlayerService;
import nro.models.services.TaskService;
import nro.models.utils.Util;
import nro.models.map.service.ChangeMapService;

public class Bardock extends Npc {

    // ====== GIÁ PET (VND) — tùy anh chỉnh ======
    private static final int PRICE_NORMAL = 20_000;   // Đệ tử thường
    private static final int PRICE_MABU   = 50_000;   // Mabu
    private static final int PRICE_UUB    = 100_000;  // Uub
    private static final int PRICE_KBEER  = 150_000;  // Kid Beer
    private static final int PRICE_JIREN  = 200_000;  // Kid Jiren

    public Bardock(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

   @Override
public void openBaseMenu(Player player) {
    if (canOpenNpc(player)) {
        if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {

            // Lấy số dư VND từ MySession
            long vnd = (player.session != null) ? player.session.vnd : 0L;

            // Ghép message bằng String thường (không dùng text block)
            String msg =
                    "Chào anh trai đẹp 😘~\n" +
                    "Ở đây bán các loại đệ tử (pet) bằng VND.\n" +
                    "Số dư của con là: " + Util.mumberToLouis(vnd) + "\n" +
                    "Chọn loại muốn mua nhé:";

            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                    msg,
                    // "Đệ tử\n(" + formatVND(PRICE_NORMAL) + ")",
                    "Mabư\n(" + formatVND(PRICE_MABU) + ")",
                    "Uub\n(" + formatVND(PRICE_UUB) + ")",
                    "Kid Beer\n(" + formatVND(PRICE_KBEER) + ")",
                     "Kid Jiren\n(" + formatVND(PRICE_JIREN) + ")",
                    "Đóng");
        }
    }
}


    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) return;
        if (!player.idMark.isBaseMenu()) return;

        switch (select) {
           // case 0 -> buyNormal(player);
            case 0 -> buyMabu(player);
            case 1 -> buyUub(player);
            case 2 -> buyKidBeer(player);
            case 3 -> buyJiren(player);
            default -> { /* Đóng */ }
        }
    }

    // ========= Các handler mua pet =========
    private void buyNormal(Player p) {
        if (!chargeVND(p, PRICE_NORMAL)) return;
        replaceOrCreatePet(p, PetType.NORMAL);
        Service.gI().sendThongBao(p, "|7|Mua thành công Đệ tử thường! 💖");
    }

    private void buyMabu(Player p) {
        if (!chargeVND(p, PRICE_MABU)) return;
        replaceOrCreatePet(p, PetType.MABU);
        Service.gI().sendThongBao(p, "|7|Mua thành công Mabư! 🍼🥚");
    }

    private void buyUub(Player p) {
        if (!chargeVND(p, PRICE_UUB)) return;
        replaceOrCreatePet(p, PetType.UUB);
        Service.gI().sendThongBao(p, "|7|Mua thành công Uub! 💥");
    }

    private void buyKidBeer(Player p) {
        if (!chargeVND(p, PRICE_KBEER)) return;
        replaceOrCreatePet(p, PetType.KID_BEER);
        Service.gI().sendThongBao(p, "|7|Mua thành công Kid Beer! 🍺😼");
    }

    private void buyJiren(Player p) {
        if (!chargeVND(p, PRICE_JIREN)) return;
        replaceOrCreatePet(p, PetType.JIREN);
        Service.gI().sendThongBao(p, "|7|Mua thành công Kid Jiren! 💪");
    }

    // ========= Core logic =========

   // Trừ VND – lấy/ghi ở MySession (player.session)
private boolean chargeVND(Player p, int price) {
    try {
        if (p == null || p.session == null) {
            Service.gI().sendThongBao(p, "Không tìm thấy phiên đăng nhập (session)!");
            return false;
        }

        synchronized (p.session) { // tránh race condition
            int current = p.session.vnd;   // <<-- tiền ở MySession
            if (current < price) {
                Service.gI().sendThongBao(p, "Bạn không đủ VND. Cần " + formatVND(price));
                return false;
            }
            p.session.vnd = current - price; // trừ tiền ngay trên session
            PlayerDAO.saveVnd(p);
        }

        // Cập nhật UI nếu hệ thống đang hiển thị VND chung với tab tiền
        try { 
            PlayerService.gI().sendInfoHpMpMoney(p); 
        } catch (Exception ignore) {}

        return true;
    } catch (Exception e) {
        Service.gI().sendThongBao(p, "Không thể trừ VND. Liên hệ admin!");
        return false;
    }
}


    private void replaceOrCreatePet(Player p, PetType type) {
        try {
            // nếu đang hợp thể thì huỷ
            if (p.fusion.typeFusion != ConstPlayer.NON_FUSION) {
                p.pet.unFusion();
            }
            // nếu đã có pet thì remove sạch sẽ
            if (p.pet != null) {
                try { ChangeMapService.gI().exitMap(p.pet); } catch (Exception ignore) {}
                p.pet.dispose();
                p.pet = null;
            }

            switch (type) {
                case NORMAL -> PetService.gI().createNormalPet(p);                 // random gender
                case MABU   -> PetService.gI().createMabuPet(p);
                case UUB    -> PetService.gI().createUubPet(p);
                case KID_BEER -> PetService.gI().createKidBeerPet(p);
                case JIREN  -> PetService.gI().createJirenPet(p);
            }
        } catch (Exception e) {
            Service.gI().sendThongBao(p, "Lỗi tạo đệ tử, liên hệ admin!");
        }
    }

    private enum PetType { NORMAL, MABU, UUB, KID_BEER, JIREN }

    private static String formatVND(long v) {
        // format đơn giản 1,000,000đ
        String s = String.format("%,d", v).replace(',', '.');
        return s + "đ";
    }
}
