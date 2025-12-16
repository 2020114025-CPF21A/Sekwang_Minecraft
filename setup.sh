#!/bin/bash
# Sekwang Minecraft Server 자동 설치 스크립트

set -e

echo "=========================================="
echo "  Sekwang Minecraft Server 설치 스크립트"
echo "=========================================="

# 변수 설정
MINECRAFT_DIR="$HOME/minecraft"
BEDROCK_VERSION="1.21.50.07"
BEDROCK_URL="https://www.minecraft.net/bedrockdedicatedserver/bin-linux/bedrock-server-${BEDROCK_VERSION}.zip"
BACKEND_URL="${BACKEND_URL:-http://43.200.61.18:8080/api/minecraft}"

# 1. 필수 패키지 설치
echo "[1/7] 필수 패키지 설치 중..."
sudo apt-get update
sudo apt-get install -y unzip curl python3 libcurl4

# 2. 마인크래프트 서버 디렉토리 생성
echo "[2/7] 디렉토리 생성 중..."
mkdir -p "$MINECRAFT_DIR"
cd "$MINECRAFT_DIR"

# 3. 베드락 서버 다운로드 (이미 있으면 스킵)
if [ ! -f "$MINECRAFT_DIR/bedrock_server" ]; then
    echo "[3/7] 베드락 서버 다운로드 중..."
    curl -o bedrock-server.zip "$BEDROCK_URL" || {
        echo "다운로드 실패. 수동으로 https://www.minecraft.net/en-us/download/server/bedrock 에서 다운로드하세요."
        exit 1
    }
    unzip -o bedrock-server.zip
    rm bedrock-server.zip
    chmod +x bedrock_server
else
    echo "[3/7] 베드락 서버가 이미 설치되어 있습니다. 스킵..."
fi

# 4. 이벤트 로거 애드온 설치
echo "[4/7] 이벤트 로거 애드온 설치 중..."
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cp -r "$SCRIPT_DIR/event_logger_addon" "$MINECRAFT_DIR/behavior_packs/"

# 5. 로그 모니터 스크립트 복사 및 백엔드 URL 설정
echo "[5/7] 로그 모니터 설치 중..."
sed "s|http://43.200.61.18:8080/api/minecraft|$BACKEND_URL|g" "$SCRIPT_DIR/log_monitor.sh" > "$MINECRAFT_DIR/log_monitor.sh"
chmod +x "$MINECRAFT_DIR/log_monitor.sh"

# 6. systemd 서비스 파일 생성
echo "[6/7] systemd 서비스 설정 중..."

# 마인크래프트 서버 서비스
sudo tee /etc/systemd/system/minecraft.service > /dev/null << EOF
[Unit]
Description=Minecraft Bedrock Server
After=network.target

[Service]
Type=simple
User=$USER
WorkingDirectory=$MINECRAFT_DIR
ExecStart=$MINECRAFT_DIR/bedrock_server
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF

# 로그 모니터 서비스
sudo tee /etc/systemd/system/mc-monitor.service > /dev/null << EOF
[Unit]
Description=Minecraft Log Monitor
After=minecraft.service
Requires=minecraft.service

[Service]
Type=simple
User=$USER
WorkingDirectory=$MINECRAFT_DIR
ExecStart=$MINECRAFT_DIR/log_monitor.sh
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload

# 7. 월드 설정 (첫 실행 후에만)
echo "[7/7] 서버 설정 완료!"

echo ""
echo "=========================================="
echo "  설치 완료!"
echo "=========================================="
echo ""
echo "다음 단계:"
echo "1. 서버 시작: sudo systemctl start minecraft"
echo "2. 서버가 월드를 생성할 때까지 대기 (약 10초)"
echo "3. 서버 중지: sudo systemctl stop minecraft"
echo "4. Beta APIs 활성화:"
echo "   python3 $SCRIPT_DIR/enable_beta_apis.py"
echo "5. behavior pack 적용:"
echo "   cp $SCRIPT_DIR/world_behavior_packs.json '$MINECRAFT_DIR/worlds/Bedrock level/'"
echo "6. 서버 및 모니터 시작:"
echo "   sudo systemctl enable --now minecraft"
echo "   sudo systemctl enable --now mc-monitor"
echo ""
echo "백엔드 URL: $BACKEND_URL"
echo "포트: 19132 (UDP)"
