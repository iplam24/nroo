package nro.models.boss.Broly;

import nro.models.boss.Boss;
import nro.models.boss.Boss_Manager.BrolyManager;
import nro.models.boss.BossData;
import nro.models.boss.BossID;
import nro.models.consts.BossStatus;
import static nro.models.consts.BossType.BROLY;
import nro.models.consts.ConstPlayer;
import nro.models.item.Item;
import nro.models.map.ItemMap;
import nro.models.map.Zone;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.PetService;
import nro.models.services.SkillService;
import nro.models.map.service.ChangeMapService;
import nro.models.skill.Skill;
import nro.models.services.PlayerService;
import nro.models.services.Service;
import nro.models.utils.SkillUtil;
import nro.models.utils.Util;

public class SuperBroly extends Boss {

    public SuperBroly(Zone zone, int x, int y) throws Exception {

        super(BROLY, BossID.SUPER_BROLY, false, false, new BossData(
                "Super Broly", //name
                ConstPlayer.XAYDA, //gender
                new short[]{294, 295, 296, -1, -1, -1}, //outfit {head, body, leg, bag, aura, eff}
                100, //dame
                new int[]{1000}, //hp
                new int[]{5, 13, 20, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38}, //map join
                new int[][]{
                    {Skill.TAI_TAO_NANG_LUONG, 1, 1000}, {Skill.TAI_TAO_NANG_LUONG, 2, 1000}, {Skill.TAI_TAO_NANG_LUONG, 3, 1000}, {Skill.TAI_TAO_NANG_LUONG, 4, 1000}, {Skill.TAI_TAO_NANG_LUONG, 5, 1000}, {Skill.TAI_TAO_NANG_LUONG, 6, 1000}, {Skill.TAI_TAO_NANG_LUONG, 7, 1000},
                    {Skill.DRAGON, 1, 1000}, {Skill.DRAGON, 2, 1000}, {Skill.DRAGON, 3, 1000}, {Skill.DRAGON, 4, 1000}, {Skill.DRAGON, 5, 1000}, {Skill.DRAGON, 6, 1000}, {Skill.DRAGON, 7, 1000},
                    {Skill.DEMON, 1, 1000}, {Skill.DEMON, 2, 1000}, {Skill.DEMON, 3, 1000}, {Skill.DEMON, 4, 1000}, {Skill.DEMON, 5, 1000}, {Skill.DEMON, 6, 1000}, {Skill.DEMON, 7, 1000},
                    {Skill.GALICK, 1, 1000}, {Skill.GALICK, 2, 1000}, {Skill.GALICK, 3, 1000}, {Skill.GALICK, 4, 1000}, {Skill.GALICK, 5, 1000}, {Skill.GALICK, 6, 1000}, {Skill.GALICK, 7, 1000},
                    {Skill.KAMEJOKO, 1, 1000}, {Skill.KAMEJOKO, 2, 1000}, {Skill.KAMEJOKO, 3, 1000}, {Skill.KAMEJOKO, 4, 1000}, {Skill.KAMEJOKO, 5, 1000}, {Skill.KAMEJOKO, 6, 1000}, {Skill.KAMEJOKO, 7, 1000},
                    {Skill.MASENKO, 1, 1000}, {Skill.MASENKO, 2, 1000}, {Skill.MASENKO, 3, 1000}, {Skill.MASENKO, 4, 1000}, {Skill.MASENKO, 5, 1000}, {Skill.MASENKO, 6, 1000}, {Skill.MASENKO, 7, 1000},
                    {Skill.ANTOMIC, 1, 1000}, {Skill.ANTOMIC, 2, 1000}, {Skill.ANTOMIC, 3, 1000}, {Skill.ANTOMIC, 4, 1000}, {Skill.ANTOMIC, 5, 1000}, {Skill.ANTOMIC, 6, 1000}, {Skill.ANTOMIC, 7, 1000},}, //skill
                new String[]{}, //text chat 1
                new String[]{"|-1|Haha! ta sẽ giết hết các ngươi",
                    "|-1|Sức mạnh của ta là tuyệt đối",
                    "|-1|Vào hết đây!!!",}, //text chat 2
                new String[]{"|-1|Các ngươi giỏi lắm. Ta sẽ quay lại."}, //text chat 3
                1//type appear
        ));
        this.zone = zone;
        this.location.x = x;
        this.location.y = y;
    }

 @Override
public void reward(Player plKill) {
    final short EGG_ID = 568; // Quả trứng Mabu
    try {
        // Xác suất 50% trứng thối
        boolean isRotten = Util.isTrue(1, 2); // 1/2 = 50%

        if (isRotten) {
            // Thông báo trứng bị thối, không rơi item
            Service.gI().sendThongBao(plKill, "|7|Ôi không! Quả trứng này bị thối, không thể sử dụng 🥚💀");
            return;
        }

        // Nếu không bị thối -> tạo item bình thường
        Item egg = ItemService.gI().createNewItem(EGG_ID);
        egg.quantity = 1;

        // Thêm vào túi hoặc rơi ra map
        if (InventoryService.gI().getCountEmptyBag(plKill) > 0) {
            InventoryService.gI().addItemBag(plKill, egg);
            InventoryService.gI().sendItemBags(plKill);
            Service.gI().sendThongBao(plKill, "|7|Bạn nhận được Quả trứng Mabu! 🥚✨");
        } else if (this.zone != null) {
            ItemMap drop = new ItemMap(this.zone, EGG_ID, 1, this.location.x, this.location.y, plKill.id);
            this.zone.addItem(drop);
            Service.gI().sendThongBao(plKill, "Hành trang đầy, trứng rơi ra đất!");
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}


    @Override
    public void active() {
        super.active();
    }
    //@Override
// public void joinMap() {
//     this.name = "Super Broly " + Util.nextInt(10, 100);
//     this.nPoint.hpMax = Util.nextInt(1_500_000, 16_070_777);
//     this.nPoint.hp = this.nPoint.hpMax;
//     this.nPoint.dame = Math.max(this.nPoint.hpMax / 60, 15_000); // đầm tay hơn chút
//     this.nPoint.crit = Util.nextInt(50);
//     this.joinMap2(); // ⬅️ để framework boss mặc định điều phối zone
//     st = System.currentTimeMillis();
// }


    @Override
    public void joinMap() {
        this.name = "Super Broly " + Util.nextInt(10, 100);
        this.nPoint.hpMax = Util.nextInt(10_500_000, 60_070_777);
        this.nPoint.hp = this.nPoint.hpMax;
        this.nPoint.dame = this.nPoint.hpMax / 100;
        this.nPoint.crit = Util.nextInt(50);
        if (this.zone != null) {
            ChangeMapService.gI().changeMap(this, this.zone, this.location.x, this.location.y);
            this.changeStatus(BossStatus.CHAT_S);
            this.notifyJoinMap();
        } else {
            super.joinMap();
        }
        // PetService.gI().createNormalPet(this);
         st = System.currentTimeMillis();
    }
    public void joinMap2() {
    if (this.zone == null) {
        if (this.parentBoss != null) this.zone = parentBoss.zone;
        else if (this.lastZone == null) this.zone = getMapJoin();
        else this.zone = this.lastZone;
    }
    if (this.zone != null) {
        try {
            int zoneId = Util.nextInt(2, this.zone.map.zones.size() - 1);
            while (zoneId < this.zone.map.zones.size() && this.zone.map.zones.get(zoneId).getBosses().size() > 0) {
                zoneId++;
            }
            if (zoneId < this.zone.map.zones.size()) {
                this.zone = this.zone.map.zones.get(zoneId);
            } else {
                this.zone = this.zone.map.zones.get(Util.nextInt(2, this.zone.map.zones.size() - 1));
            }
            if (this.zone.zoneId < 2) {
                // tránh 2 khu đầu nếu anh đang dùng convention này
                this.changeStatus(BossStatus.REST);
                return;
            }
            ChangeMapService.gI().changeMap(this, this.zone, -1, -1);
            this.changeStatus(BossStatus.CHAT_S);
        } catch (Exception e) {
            this.changeStatus(BossStatus.REST);
        }
    } else {
        this.changeStatus(BossStatus.RESPAWN);
    }
}


    private long st;

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, 300_000)) {
            this.leaveMap();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }
    }

    // @Override
    // public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
    //     if (!this.isDie()) {
    //         if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
    //             this.chat("Xí hụt");
    //             return 0;
    //         }
    //         if (Util.isTrue(1, 30)) {
    //             this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, 6));
    //             this.tangChiSo();
    //             SkillService.gI().useSkill(this, null, null, -1, null);
    //         }
    //         damage = this.nPoint.subDameInjureWithDeff(damage);
    //         if (!piercing && plAtt.playerSkill.skillSelect.template.id != Skill.TU_SAT && damage > this.nPoint.hpMax / 100) {
    //             damage = this.nPoint.hpMax / 100;
    //         }
    //         this.nPoint.subHP(damage);
    //         if (isDie()) {
    //             this.setDie(plAtt);
    //             die(plAtt);
    //         }
    //         return (int) damage;
    //     } else {
    //         return 0;
    //     }
    // }
@Override
public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
    if (this.isDie()) return 0;

    // Né đòn như mặc định (giữ)
    if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
        this.chat("Xí hụt");
        return 0;
    }

    // Tính sát thương sau thủ/giáp
    long realDmg = this.nPoint.subDameInjureWithDeff(damage);

    // ❌ BỎ hết các giới hạn/điều kiện đặc biệt:
    // - Không còn cap damage = hpMax/100
    // - Không ép đổi skill hay buff chỉ số ở đây

    this.nPoint.subHP(realDmg);

    if (this.isDie()) {
        this.setDie(plAtt);
        // gọi die ở lớp cha để chạy reward/leaveMap đúng pipeline
        super.die(plAtt);
    }

    return (int) Math.min(realDmg, Integer.MAX_VALUE);
}

    private long lastTimeAttack;

    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 100) && this.typePk == ConstPlayer.PK_ALL) {
            this.lastTimeAttack = System.currentTimeMillis();
            try {
                Player pl = getPlayerAttack();
                if (pl == null || pl.isDie()) {
                    return;
                }
                this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(7, this.playerSkill.skills.size() - 1));
                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(5, 20)) {
                        if (SkillUtil.isUseSkillChuong(this)) {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 200)),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 70));
                        } else {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 40)),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 50));
                        }
                    }
                    if (Util.isTrue(1, 100)) {
                        this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, 6));
                        this.tangChiSo();
                    }

                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    checkPlayerDie(pl);
                } else {
                    if (Util.isTrue(1, 2)) {
                        this.moveToPlayer(pl);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void tangChiSo() {
        int hpMax = this.nPoint.hpMax;
        int rand = Util.nextInt(80, 100);
        hpMax = hpMax + hpMax / rand < 16_070_777 ? hpMax + hpMax / rand : 16_070_777;
        this.nPoint.hpMax = hpMax;
        this.nPoint.dame = hpMax / 10;
    }

    // @Override
    // public void leaveMap() {
    //     ChangeMapService.gI().exitMap(this);
    //     if (this.pet != null) {
    //         ChangeMapService.gI().exitMap(this.pet);
    //     }
    //     this.lastZone = null;
    //     this.lastTimeRest = System.currentTimeMillis();
    //     this.changeStatus(BossStatus.REST);
    //     BrolyManager.gI().removeBoss(this);
    //     this.dispose();
    // }

    @Override
public void die(Player plKill) {
    // phần thưởng (rơi trứng) của anh vẫn giữ nguyên ở reward()
    this.changeStatus(BossStatus.DIE);
    // chuyển về REST và set mốc nghỉ
    this.changeStatus(BossStatus.REST);
    this.lastTimeRest = System.currentTimeMillis();
}

    @Override
public void leaveMap() {
    ChangeMapService.gI().exitMap(this);
    if (this.pet != null) {
        ChangeMapService.gI().exitMap(this.pet);
    }
    this.lastZone = null;
    // KHÔNG removeBoss()/dispose() nữa, để manager mặc định còn “nắm” boss mà respawn
    this.changeStatus(BossStatus.REST);
    this.lastTimeRest = System.currentTimeMillis();
}
}
