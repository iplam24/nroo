package nro.models.server;

import nro.models.services.PlayerService;
import nro.models.boss.Boss;
import nro.models.boss.Boss_Manager.BrolyManager;
import nro.models.consts.ConstNpc;
import nro.models.managers.GiftCodeManager;
import nro.models.item.Item;
import java.util.ArrayList;
import nro.models.player.Pet;
import nro.models.player.Player;
import nro.models.network.SessionManager;
import nro.models.services.ItemService;
import nro.models.services.PetService;
import nro.models.services.Service;
import nro.models.services_func.Input;
import nro.models.map.service.ChangeMapService;
import nro.models.map.service.NpcService;
import nro.models.services.InventoryService;
import nro.models.utils.SystemMetrics;
import nro.models.utils.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import nro.models.Bot.BotAttackplayer;
import nro.models.Bot.BotManager;
import nro.models.consts.ConstPlayer;
import nro.models.data.LocalManager;
import nro.models.services.TaskService;

/**
 *
 * @author By Mr Blue
 *
 */
public class Command {

    private static Command instance;

    private final Map<String, Consumer<Player>> adminCommands = new HashMap<>();
    private final Map<String, BiConsumer<Player, String>> parameterizedCommands = new HashMap<>();

    public static Command gI() {
        if (instance == null) {
            instance = new Command();
        }
        return instance;
    }

    private Command() {
        initAdminCommands();
        initParameterizedCommands();
    }

    private void initAdminCommands() {
        adminCommands.put("item", player -> Input.gI().createFormGiveItem(player));
        adminCommands.put("brl", player -> BrolyManager.gI().showListBoss(player));
        adminCommands.put("getitem", player -> Input.gI().createFormGetItem(player));
        adminCommands.put("hs", player -> Service.gI().releaseCooldownSkill(player));
        adminCommands.put("d", player -> Service.gI().setPos(player, player.location.x, player.location.y + 10));
        adminCommands.put("menu", player -> NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_ADMIN, -1,
                "|0|Time start: " + ServerManager.timeStart
                + "\nClients: " + Client.gI().getPlayers().size()
                + "\n Sessions: " + SessionManager.gI().getNumSession()
                + "\nThreads: " + Thread.activeCount()
                + " luồng" + "\n" + SystemMetrics.ToString(),
                "Ngọc rồng", "Đệ tử", "Bảo trì", "Tìm kiếm\nngười chơi", "Boss","EXP", "Đóng"));
    }

    private void initParameterizedCommands() {
        parameterizedCommands.put("m", (player, text) -> {
            try {
                int mapId = Integer.parseInt(text.replace("m", "").trim());
                ChangeMapService.gI().changeMapInYard(player, mapId, -1, -1);
            } catch (NumberFormatException e) {
                Service.gI().sendThongBao(player, "Sai định dạng map ID!");
            }
        });

        parameterizedCommands.put("toado", (player, text) -> {
            Service.gI().sendThongBaoOK(player, "x: " + player.location.x + " - y: " + player.location.y);
        });
parameterizedCommands.put("kill", (player, text) -> {
    try {
        Boss nearestBoss = null;
        double minDistance = Double.MAX_VALUE;

        for (Player pl : player.zone.getBosses()) {
            if (pl instanceof Boss) {
                Boss b = (Boss) pl;
                if (!b.isDie()) {
                    double d = Util.getDistance(player, b);
                    if (d < minDistance) {
                        minDistance = d;
                        nearestBoss = b;
                    }
                }
            }
        }

        if (nearestBoss != null && minDistance <= 300) {
            synchronized (nearestBoss) {
                if (!nearestBoss.isDie()) {
                    // 🔥 Gây sát thương xuyên giáp cực lớn để boss chết chuẩn pipeline
                    long dmg = nearestBoss.nPoint.hpMax + 1L;
                    nearestBoss.injured(player, dmg, /*piercing*/ true, /*isMobAttack*/ false);
                }
            }
            Service.gI().sendThongBao(player, "|7|Đã tiêu diệt Boss: " + nearestBoss.name + " 💀");
        } else {
            Service.gI().sendThongBao(player, "Không có boss nào gần bạn để kill 😢");
        }
    } catch (Exception e) {
        Service.gI().sendThongBao(player, "Lỗi khi kill boss: " + e.getMessage());
        e.printStackTrace();
    }
});





        parameterizedCommands.put("1", (player, text) -> {
            NpcService.gI().createMenuConMeo(player, 206783, 206783, "|7| Menu bot\n"
                    + "Player online : " + Client.gI().getPlayers().size() + "\n"
                    + "\b|1|Thread: " + Thread.activeCount() + "\n"
                    + "\n Sessions: " + SessionManager.gI().getNumSession() + "\n"
                    + "Bot online : " + BotManager.gI().bot.size(),
                    "Bot\nPem Quái", "Bot\nBán Item", "Bot\nSăn Boss", "Bot\nAttack Player");
            return;
        });

        parameterizedCommands.put("2", (player, text) -> {
            player.originalName = player.name;
            PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.PK_ALL);
            player.originalName = player.name;
            Service.gI().Send_Caitrang(player);
            BotAttackplayer bot = new BotAttackplayer((short) 1624, (short) 1628, (short) 1629, 1, "đánh nhau không?", (short) 0);
            bot.player = player;
            bot.zone = player.zone;
            bot.location.x = player.location.x;
            bot.location.y = player.location.y;

            player.zone.addPlayer(bot);
            BotManager.gI().bot.add(bot);

            for (Player p : player.zone.getPlayers()) {
                if (p.session != null) {
                    Service.gI().sendAppear(bot, p);
                    Service.gI().sendInfoCharMoiToMe(p, bot);
                }
            }

            if (player.session != null) {
                Service.gI().Send_Info_NV(player);
            }

            bot.update();

            ServerNotify.gI().notify("Đã gọi bot tấn công người chơi!");
        });

        parameterizedCommands.put("b", (player, text) -> {
            Input.gI().createFormSenditem1(player);
        });

        parameterizedCommands.put("n", (player, text) -> {
            try {
                int idTask = Integer.parseInt(text.replaceAll("n", "").trim());
                player.playerTask.taskMain.id = idTask - 1;
                player.playerTask.taskMain.index = 0;
                TaskService.gI().sendNextTaskMain(player);
            } catch (Exception e) {
                Service.gI().sendThongBao(player, "Sai định dạng task ID!");
            }
        });

        parameterizedCommands.put("i ", (player, text) -> {
            try {
                String[] split = text.split(" ");
                if (split.length < 2) {
                    Service.gI().sendThongBao(player, "Cú pháp: i <itemId> <số lượng> [option:value...]");
                    return;
                }

                int itemId = Integer.parseInt(split[1]);
                int quantity = split.length >= 3 ? Integer.parseInt(split[2]) : 1;

                List<Item.ItemOption> customOptions = new ArrayList<>();
                for (int i = 3; i < split.length; i++) {
                    if (split[i].contains(":")) {
                        String[] optSplit = split[i].split(":");
                        int optionId = Integer.parseInt(optSplit[0]);
                        int optionValue = Integer.parseInt(optSplit[1]);
                        customOptions.add(new Item.ItemOption(optionId, optionValue));
                    }
                }

                for (int i = 0; i < quantity; i++) {
                    Item item = ItemService.gI().createNewItem((short) itemId);
                    if (!customOptions.isEmpty()) {
                        item.itemOptions = new ArrayList<>(customOptions);
                    } else {
                        List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop((short) itemId);
                        if (!ops.isEmpty()) {
                            item.itemOptions = ops;
                        }
                    }
                    InventoryService.gI().addItemBag(player, item);
                }

                InventoryService.gI().sendItemBags(player);
                Service.gI().sendThongBao(player, "GET " + quantity + " x " + ItemService.gI().getTemplate(itemId).name + " [" + itemId + "] SUCCESS!");

            } catch (Exception e) {
                Service.gI().sendThongBao(player, "Lỗi cú pháp! Dùng: i <itemId> <số lượng> [optionId:value]");
            }
        });
    }

    public void chat(Player player, String text) {
        String cleanedText = text.trim();
        if (cleanedText.isEmpty()) {
            return;
        }
        if (!check(player, cleanedText)) {
            Service.gI().chat(player, cleanedText);
        }
    }

    public boolean check(Player player, String text) {
        if (player.isAdmin()) {
            if (adminCommands.containsKey(text)) {
                adminCommands.get(text).accept(player);
                return true;
            }

            for (Map.Entry<String, BiConsumer<Player, String>> entry : parameterizedCommands.entrySet()) {
                if (text.startsWith(entry.getKey())) {
                    entry.getValue().accept(player, text);
                    return true;
                }
            }
        }

        if (text.startsWith("ten con la ")) {
            PetService.gI().changeNamePet(player, text.replace("ten con la ", ""));
        }

        if (player.pet != null) {
            switch (text) {
                case "di theo", "follow" ->
                    player.pet.changeStatus(Pet.FOLLOW);
                case "bao ve", "protect" ->
                    player.pet.changeStatus(Pet.PROTECT);
                case "tan cong", "attack" ->
                    player.pet.changeStatus(Pet.ATTACK);
                case "ve nha", "go home" ->
                    player.pet.changeStatus(Pet.GOHOME);
                case "bien hinh" ->
                    player.pet.transform();
                case "sach tuyet ky" -> {
                    int typePet = player.pet.typePet;
                    if (typePet == 2 || typePet == 3 || typePet == 4) {
                        for (int i = 0; i < player.inventory.itemsBag.size(); i++) {
                            Item item = player.inventory.itemsBag.get(i);
                            if (item != null && item.isNotNullItem() && item.template.type == 25) {
                                if (player.pet.nPoint != null && player.pet.nPoint.power >= 1500000) {
                                    Item old = InventoryService.gI().putItemBody(player.pet, item);
                                    player.inventory.itemsBag.set(i, old);
                                    InventoryService.gI().sendItemBags(player);
                                    InventoryService.gI().sendItemBody(player);
                                    Service.gI().Send_Caitrang(player.pet);
                                    Service.gI().Send_Caitrang(player);
                                    Service.gI().sendThongBao(player, "Đã dùng " + item.template.name + " cho đệ tử");
                                } else {
                                    Service.gI().sendThongBaoOK(player, "Đệ tử cần đạt 1tr5 sức mạnh để trang bị.");
                                }
                                break;
                            }
                        }
                    } else {
                        Service.gI().sendThongBaoOK(player, "Chỉ đệ tử (Uub, Kid Beerus, Jiren) mới có thể dùng sách tuyệt kỹ.");
                    }
                }
            }
        }
        return false;
    }
}
