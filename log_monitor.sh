#!/bin/bash
# 마인크래프트 로그 모니터링 스크립트 (이벤트 애드온 지원)

BACKEND_URL="http://43.200.61.18:8080/api/minecraft"

echo "Starting Minecraft Log Monitor (with Event Logger Addon)..."

# URL 인코딩 함수
urlencode() {
    python3 -c "import urllib.parse; print(urllib.parse.quote('''$1''', safe=''))"
}

# journalctl -f 로 실시간 로그 감시
sudo journalctl -u minecraft -f --no-pager --output=cat | while read line; do

    # ===== 기존 Player connected/disconnected =====
    if echo "$line" | grep -q 'Player connected:'; then
        PLAYER=$(echo "$line" | sed 's/.*Player connected: \([^,]*\),.*/\1/')
        if [ -n "$PLAYER" ] && [ "$PLAYER" != "$line" ]; then
            ENCODED_PLAYER=$(urlencode "$PLAYER")
            curl -s "$BACKEND_URL/log/join?player=$ENCODED_PLAYER" > /dev/null &
            echo "$(date '+%Y-%m-%d %H:%M:%S') - JOIN: $PLAYER"
        fi
    fi
    
    if echo "$line" | grep -q 'Player disconnected:'; then
        PLAYER=$(echo "$line" | sed 's/.*Player disconnected: \([^,]*\),.*/\1/')
        if [ -n "$PLAYER" ] && [ "$PLAYER" != "$line" ]; then
            ENCODED_PLAYER=$(urlencode "$PLAYER")
            curl -s "$BACKEND_URL/log/leave?player=$ENCODED_PLAYER" > /dev/null &
            echo "$(date '+%Y-%m-%d %H:%M:%S') - LEAVE: $PLAYER"
        fi
    fi

    # ===== EventLogger 애드온 이벤트 처리 =====
    if echo "$line" | grep -q '\[EventLogger\]'; then
        
        # 채팅 이벤트: [EventLogger] CHAT|PlayerName|Message
        if echo "$line" | grep -q 'CHAT|'; then
            DATA=$(echo "$line" | sed 's/.*CHAT|\([^|]*\)|\(.*\)/\1|\2/')
            PLAYER=$(echo "$DATA" | cut -d'|' -f1)
            MSG=$(echo "$DATA" | cut -d'|' -f2-)
            
            ENCODED_PLAYER=$(urlencode "$PLAYER")
            ENCODED_MSG=$(urlencode "$MSG")
            
            curl -s "$BACKEND_URL/event?type=CHAT&player=$ENCODED_PLAYER&message=$ENCODED_MSG" > /dev/null &
            echo "$(date '+%Y-%m-%d %H:%M:%S') - CHAT: <$PLAYER> $MSG"
        fi
        
        # 죽음 이벤트: [EventLogger] DEATH|PlayerName|DeathMessage
        if echo "$line" | grep -q 'DEATH|'; then
            DATA=$(echo "$line" | sed 's/.*DEATH|\([^|]*\)|\(.*\)/\1|\2/')
            PLAYER=$(echo "$DATA" | cut -d'|' -f1)
            MSG=$(echo "$DATA" | cut -d'|' -f2-)
            
            ENCODED_PLAYER=$(urlencode "$PLAYER")
            ENCODED_MSG=$(urlencode "$MSG")
            
            curl -s "$BACKEND_URL/event?type=DEATH&player=$ENCODED_PLAYER&message=$ENCODED_MSG" > /dev/null &
            echo "$(date '+%Y-%m-%d %H:%M:%S') - DEATH: $MSG"
        fi
        
        # 리스폰 이벤트: [EventLogger] RESPAWN|PlayerName|Message
        if echo "$line" | grep -q 'RESPAWN|'; then
            DATA=$(echo "$line" | sed 's/.*RESPAWN|\([^|]*\)|\(.*\)/\1|\2/')
            PLAYER=$(echo "$DATA" | cut -d'|' -f1)
            
            ENCODED_PLAYER=$(urlencode "$PLAYER")
            
            curl -s "$BACKEND_URL/event?type=RESPAWN&player=$ENCODED_PLAYER" > /dev/null &
            echo "$(date '+%Y-%m-%d %H:%M:%S') - RESPAWN: $PLAYER"
        fi
        
        # 초기 스폰 이벤트: [EventLogger] JOIN_SPAWN|PlayerName|Message
        if echo "$line" | grep -q 'JOIN_SPAWN|'; then
            DATA=$(echo "$line" | sed 's/.*JOIN_SPAWN|\([^|]*\)|\(.*\)/\1|\2/')
            PLAYER=$(echo "$DATA" | cut -d'|' -f1)
            
            ENCODED_PLAYER=$(urlencode "$PLAYER")
            
            curl -s "$BACKEND_URL/event?type=SPAWN&player=$ENCODED_PLAYER" > /dev/null &
            echo "$(date '+%Y-%m-%d %H:%M:%S') - SPAWN: $PLAYER"
        fi
    fi

done
