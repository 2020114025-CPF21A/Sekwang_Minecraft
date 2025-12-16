import { world, system } from "@minecraft/server";

// ===== 이벤트 로거 애드온 (Beta APIs) =====
const PREFIX = "[EventLogger]";

console.log(`${PREFIX} Event Logger 애드온 초기화 중... (Beta APIs)`);

// ===== 채팅 이벤트 (Beta API) =====
try {
    if (world.afterEvents.chatSend) {
        world.afterEvents.chatSend.subscribe((event) => {
            try {
                const playerName = event.sender.name;
                const message = event.message;
                if (message.startsWith('/')) return;
                console.log(`${PREFIX} CHAT|${playerName}|${message}`);
            } catch (e) {
                console.log(`${PREFIX} ERROR|CHAT|${e}`);
            }
        });
        console.log(`${PREFIX} chatSend 이벤트 등록됨!`);
    } else {
        console.log(`${PREFIX} chatSend 이벤트를 사용할 수 없습니다`);
    }
} catch (e) {
    console.log(`${PREFIX} chatSend 등록 실패: ${e}`);
}

// ===== 플레이어 사망 이벤트 =====
try {
    if (world.afterEvents.entityDie) {
        world.afterEvents.entityDie.subscribe((event) => {
            try {
                if (event.deadEntity.typeId !== "minecraft:player") return;
                const playerName = event.deadEntity.name;
                let deathMessage = `${playerName}이(가) 사망했습니다`;

                if (event.damageSource) {
                    const cause = event.damageSource.cause;
                    const damagingEntity = event.damageSource.damagingEntity;
                    if (damagingEntity) {
                        const killerName = damagingEntity.name || damagingEntity.typeId.replace("minecraft:", "");
                        deathMessage = `${playerName}이(가) ${killerName}에게 살해당했습니다`;
                    } else if (cause) {
                        deathMessage = `${playerName}이(가) ${cause}(으)로 사망했습니다`;
                    }
                }
                console.log(`${PREFIX} DEATH|${playerName}|${deathMessage}`);
            } catch (e) {
                console.log(`${PREFIX} ERROR|DEATH|${e}`);
            }
        });
        console.log(`${PREFIX} entityDie 이벤트 등록됨`);
    }
} catch (e) {
    console.log(`${PREFIX} entityDie 등록 실패: ${e}`);
}

// ===== 플레이어 스폰 이벤트 =====
try {
    if (world.afterEvents.playerSpawn) {
        world.afterEvents.playerSpawn.subscribe((event) => {
            try {
                const playerName = event.player.name;
                const isInitialSpawn = event.initialSpawn;
                if (isInitialSpawn) {
                    console.log(`${PREFIX} JOIN_SPAWN|${playerName}|${playerName}이(가) 월드에 처음 스폰되었습니다`);
                } else {
                    console.log(`${PREFIX} RESPAWN|${playerName}|${playerName}이(가) 리스폰했습니다`);
                }
            } catch (e) {
                console.log(`${PREFIX} ERROR|SPAWN|${e}`);
            }
        });
        console.log(`${PREFIX} playerSpawn 이벤트 등록됨`);
    }
} catch (e) {
    console.log(`${PREFIX} playerSpawn 등록 실패: ${e}`);
}

// ===== 플레이어 퇴장 이벤트 =====
try {
    if (world.beforeEvents && world.beforeEvents.playerLeave) {
        world.beforeEvents.playerLeave.subscribe((event) => {
            try {
                const playerName = event.player.name;
                console.log(`${PREFIX} LEAVE|${playerName}|${playerName}이(가) 서버를 떠났습니다`);
            } catch (e) {
                console.log(`${PREFIX} ERROR|LEAVE|${e}`);
            }
        });
        console.log(`${PREFIX} playerLeave 이벤트 등록됨`);
    }
} catch (e) {
    console.log(`${PREFIX} playerLeave 등록 실패: ${e}`);
}

console.log(`${PREFIX} 초기화 완료!`);
