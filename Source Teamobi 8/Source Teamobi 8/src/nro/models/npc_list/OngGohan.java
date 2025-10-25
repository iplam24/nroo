package nro.models.npc_list;

import java.util.ArrayList;
import java.util.List;

import nro.models.consts.ConstDailyGift;
import nro.models.consts.ConstNpc;
import nro.models.consts.ConstPlayer;
import nro.models.consts.ConstTask;
import nro.models.consts.ConstTaskBadges;
import nro.models.daily_Giftcode.DailyGiftService;
import nro.models.database.PlayerDAO;
import nro.models.item.Item;
import nro.models.map.service.NpcService;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.server.Maintenance;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.PetService;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.services_func.Input;
import nro.models.shop.ShopService;
import nro.models.task.BadgesTaskService;
import nro.models.utils.Util;

public class OngGohan extends Npc {

    public OngGohan(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                "Cố Gắng Có Làm Mới Có Ăn Con, đừng lo lắng cho ta.\n"
                                        .replaceAll("%1", player.gender == ConstPlayer.TRAI_DAT ? "Quy lão Kamê"
                                                : player.gender == ConstPlayer.NAMEC ? "Trưởng lão Guru" : "Vua Vegeta") + "Ta đang giữ tiền tiết kiệm của con\n hiện tại con đang có: " + player.getSession().goldBar + " thỏi vàng",
                                "Đổi thỏi\nvàng","Điểm danh\nhàng ngày","Kích hoạt\n miễn phí", "Nhận 200k ngọc xanh", "Nhận đệ tử", "Nhận vàng\nhành trang", "Giftcode");

            }
        }
    }
           @Override
public void confirmMenu(Player player, int select) {
    if (canOpenNpc(player)) {
        // ====== BASE MENU ======
        if (player.idMark.isBaseMenu()) {
            switch (select) {
                case 0: {
                    String npcSay = "Số dư của con là: " + Util.mumberToLouis(player.getSession().vnd)
                            + " VND dùng để nạp qua đơn vị khác\n"
                            + "Ta đang giữ giúp con " + Util.mumberToLouis(player.getSession().goldBar)
                            + " thỏi vàng";
                    createOtherMenu(player, ConstNpc.DOI_TIEN_O_NHA, npcSay,
                            "Nạp vàng",
                            "Nhận\nThỏi vàng",
                            "Đóng");
                    break;
                }
                case 1: { // điểm danh VND mỗi ngày
                    if (player.getSession() == null) {
                        Service.gI().sendThongBao(player, "Không tìm thấy phiên đăng nhập!");
                        break;
                    }
                    if (!DailyGiftService.checkDailyGift(player, ConstDailyGift.NHAN_VND_MIEN_PHI)) {
                        Service.gI().sendThongBao(player, "Hôm nay bạn đã nhận VND miễn phí rồi, quay lại vào ngày mai nhé!");
                        break;
                    }
                    int soTienVND = 10_000;
                    player.getSession().vnd += soTienVND;
                    PlayerDAO.saveVnd(player);
                    DailyGiftService.updateDailyGift(player, ConstDailyGift.NHAN_VND_MIEN_PHI);
                    Service.gI().sendMoney(player);
                    Service.gI().sendThongBao(player, "Bạn vừa nhận " + Util.numberFormatLouis(soTienVND) + " VND vào số dư");
                    break;
                }
                case 2:
                    if (!player.getSession().actived) {
                        boolean ok = PlayerDAO.updateActivated(player, true);
                        if (ok) {
                            player.getSession().actived = true;        
                            Service.gI().sendMoney(player);
                            Service.gI().sendThongBao(player, "|7|Kích hoạt thành công");
                        } else {
                            this.npcChat(player, "Lỗi lưu dữ liệu, thử lại sau nhé!");
                        }
                    } else {
                        this.npcChat(player, "Bạn đã mở thành viên rồi!");
                    }
                    break;

                case 3:
                                if (player.inventory.gem == 200000) {
                                    this.npcChat(player, "Tham Lam");
                                    break;
                                }
                                player.inventory.gem = 200000;
                                Service.gI().sendMoney(player);
                                Service.gI().sendThongBao(player, "Bạn vừa nhận được 200K ngọc xanh");
                                break;
                case 4:
                                if (player.pet == null) {
                                    PetService.gI().createNormalPet(player);
                                    Service.gI().sendThongBao(player, "Bạn vừa nhận được đệ tử");
                                } else {
                                    this.npcChat(player, "Bạn đã có đệ tử rồi");
                                }
                                break;
                case 5:
                                // if (Maintenance.isRunning) {
                                //     break;
                                // }
                                // if (player.getSession().goldBar > 0) {
                                //     if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                                //         int quantity = player.getSession().goldBar;
                                //         if (PlayerDAO.subGoldBar(player, player.getSession().goldBar)) {
                                //             Item goldBar = ItemService.gI().createNewItem((short) 457, quantity);
                                //             InventoryService.gI().addItemBag(player, goldBar);
                                //             InventoryService.gI().sendItemBags(player);
                                //             this.npcChat(player, "Ta đã gửi " + quantity + " thỏi vàng vào hành trang của con\n con hãy kiểm tra ");
                                //         } else {
                                //             this.npcChat(player, "Lỗi vui lòng báo admin...");
                                //         }
                                //     } else {
                                //         this.npcChat(player, "Hãy chừa cho ta 1 ô trống");
                                //     }
                                // } else {
                                //     this.npcChat(player, "Con đang không có thỏi vàng hãy ib ông trùm để nạp thẻ tự động nhanh gọn không chiết khấu, giá vàng cực rẻ");
                                // }
                                // break;

                                  if (player.inventory.gold == 20000000000L) {
                                    this.npcChat(player, "Tham Lam");
                                    break;
                                }
                                player.inventory.gold = 2000000000;
                                Service.gI().sendMoney(player);
                                Service.gI().sendThongBao(player, "Bạn vừa nhận được 2 tỷ vàng");
                                break;
                case 6:
                                Input.gI().createFormGiftCode(player);
                                break;
            }
        }

        // ====== ĐỔI TIỀN Ở NHÀ ======
        else if (player.idMark.getIndexMenu() == ConstNpc.DOI_TIEN_O_NHA) {
            final int costNapVang = 1;
            final int[][] napVang = {
                {20000, 40}, {50000, 105}, {100000, 250},
                {500000, 1500}, {1000000, 3100}, {2000000, 6500}, {5000000, 17000}
            };
            switch (select) {
                case 0: { // mở danh sách mốc nạp
                    java.util.List<String> menu = new java.util.ArrayList<>();
                    for (int i = 0; i < napVang.length; i++) {
                        menu.add(
                            Util.numberFormatLouis(napVang[i][0]) + " VND\n" +
                            Util.numberFormatLouis(napVang[i][1] * costNapVang) + " thỏi vàng"
                        );
                    }
                    menu.add("◀ Quay lại");
                    createOtherMenu(player, ConstNpc.NAP_VANG_MENU, "Chọn mốc nạp nhé cưng~ 💛",
                            menu.toArray(new String[0]));
                    break;
                }
                case 1: { // nhận thỏi vàng từ kho vào hành trang
                    if (Maintenance.isRunning) break;
                    if (player.getSession().goldBar <= 0) {
                        this.npcChat(player, "Con không có thỏi vàng nào trong kho để nhận cả~");
                        break;
                    }
                    if (InventoryService.gI().getCountEmptyBag(player) < 1) {
                        this.npcChat(player, "Hãy chừa cho ta 1 ô trống trong hành trang nha~");
                        break;
                    }
                    int quantity = player.getSession().goldBar;
                    Item goldBarItem = ItemService.gI().createNewItem((short) 457, quantity);
                    InventoryService.gI().addItemBag(player, goldBarItem);
                    InventoryService.gI().sendItemBags(player);
                    if (PlayerDAO.subGoldBar(player, quantity)) {
                        this.npcChat(player, "Ta đã gửi " + quantity + " thỏi vàng vào hành trang của con rồi đó~");
                    } else {
                        this.npcChat(player, "Có lỗi khi cập nhật kho thỏi vàng, báo admin nhé!");
                    }
                    break;
                }
                case 2: { // Đóng
                    break;
                }
            }
        }

        // ====== SUBMENU NẠP VÀNG ======
        else if (player.idMark.getIndexMenu() == ConstNpc.NAP_VANG_MENU) {
            final int costNapVang = 1;
            final int[][] napVang = {
                {20000, 40}, {50000, 105}, {100000, 250},
                {500000, 1500}, {1000000, 3100}, {2000000, 6500}, {5000000, 17000}
            };

            int backIndex = napVang.length; // nút "◀ Quay lại"
            if (select == backIndex) {
                String npcSay = "Số dư của con là: " + Util.numberFormatLouis(player.getSession().vnd)
                        + " VND dùng để nạp qua đơn vị khác\n"
                        + "Ta đang giữ giúp con " + Util.numberFormatLouis(player.getSession().goldBar)
                        + " thỏi vàng";
                createOtherMenu(player, ConstNpc.DOI_TIEN_O_NHA, npcSay, "Nạp vàng", "Nhận\nThỏi vàng", "Đóng");
                return;
            }
            if (select < 0 || select >= napVang.length) return;

            int vndCost  = napVang[select][0];
            int goldBars = napVang[select][1] * costNapVang;

            if (player.getSession().vnd < vndCost) {
                Service.gI().sendThongBao(player, "Số dư không đủ. Cần " + Util.numberFormatLouis(vndCost) + " VND nha~");
                return;
            }

            // trừ VND + lưu DB
            player.getSession().vnd -= vndCost;
            PlayerDAO.saveVnd(player);

            // cộng thỏi vàng vào kho
            if (!PlayerDAO.addGoldBar(player, goldBars)) {
                // rollback nếu lỗi
                player.getSession().vnd += vndCost;
                PlayerDAO.saveVnd(player);
                Service.gI().sendThongBao(player, "Có lỗi khi cộng thỏi vàng, giao dịch đã hoàn lại!");
                return;
            }

            Service.gI().sendMoney(player);
            Service.gI().sendThongBao(player,
                "Nạp " + Util.numberFormatLouis(vndCost) + " VND thành công → nhận "
              + Util.numberFormatLouis(goldBars) + " thỏi vàng 💛");
        }

        // ====== QUÀ TÂN THỦ ======
        else if (player.idMark.getIndexMenu() == ConstNpc.QUA_TAN_THU) {
            // ... phần của anh ở đây, giữ nguyên ...
        }

        // ====== CÁC MENU KHÁC ======
        else if (player.idMark.getIndexMenu() == ConstNpc.NAP_THE) {
            Input.gI().createFormNapThe(player, (byte) select);
        }
    }
}

}
    
  
