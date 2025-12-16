#!/bin/bash
# Sekwang Minecraft Server 설치 스크립트
# Ubuntu 22.04 / 24.04 지원

set -e

echo "========================================"
echo "  Sekwang Minecraft Server 설치 스크립트"
echo "========================================"

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 변수 설정
MINECRAFT_DIR="$HOME/minecraft"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BDS_VERSION="1.21.51.02"  # 필요시 버전 업데이트

echo -e "${YELLOW}[1/6] 시스템 패키지 업데이트...${NC}"
sudo apt-get update -y
sudo apt-get install -y curl wget unzip screen python3

echo -e "${YELLOW}[2/6] 마인크래프트 베드락 서버 다운로드...${NC}"
mkdir -p "$MINECRAFT_DIR"
cd "$MINECRAFT_DIR"

if [ ! -f "bedrock_server" ]; then
    echo "베드락 서버 다운로드 중..."
    wget -q "https://minecraft.azureedge.net/bin-linux/bedrock-server-${BDS_VERSION}.zip" -O bedrock-server.zip
    unzip -o bedrock-server.zip
    rm bedrock-server.zip
    chmod +x bedrock_server
    echo -e "${GREEN}베드락 서버 다운로드 완료!${NC}"
else
    echo "베드락 서버가 이미 존재합니다. 스킵..."
fi

echo -e "${YELLOW}[3/6] Event Logger 애드온 설치...${NC}"
mkdir -p "$MINECRAFT_DIR/behavior_packs/event_logger_addon"
cp -r "$SCRIPT_DIR/event_logger_addon/"* "$MINECRAFT_DIR/behavior_packs/event_logger_addon/"
echo -e "${GREEN}애드온 설치 완료!${NC}"

echo -e "${YELLOW}[4/6] 월드 설정...${NC}"
mkdir -p "$MINECRAFT_DIR/worlds/Bedrock level"
cp "$SCRIPT_DIR/world_behavior_packs.json" "$MINECRAFT_DIR/worlds/Bedrock level/"

# Beta APIs 활성화 (기존 level.dat이 있으면)
if [ -f "$MINECRAFT_DIR/worlds/Bedrock level/level.dat" ]; then
    echo "기존 월드에 Beta APIs 활성화 중..."
    python3 "$SCRIPT_DIR/enable_beta_apis.py" || true
fi
echo -e "${GREEN}월드 설정 완료!${NC}"

echo -e "${YELLOW}[5/6] 로그 모니터 설치...${NC}"
cp "$SCRIPT_DIR/log_monitor.sh" "$MINECRAFT_DIR/"
chmod +x "$MINECRAFT_DIR/log_monitor.sh"

# systemd 서비스 생성 - 마인크래프트
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

# systemd 서비스 생성 - 로그 모니터
sudo tee /etc/systemd/system/mc-monitor.service > /dev/null << EOF
[Unit]
Description=Minecraft Log Monitor
After=minecraft.service
Requires=minecraft.service

[Service]
Type=simple
User=$USER
WorkingDirectory=$MINECRAFT_DIR
ExecStart=/bin/bash $MINECRAFT_DIR/log_monitor.sh
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
echo -e "${GREEN}서비스 설치 완료!${NC}"

echo -e "${YELLOW}[6/6] 방화벽 설정...${NC}"
sudo ufw allow 19132/udp 2>/dev/null || true
sudo ufw allow 19133/udp 2>/dev/null || true
echo -e "${GREEN}방화벽 설정 완료!${NC}"

echo ""
echo -e "${GREEN}========================================"
echo "  설치 완료!"
echo "========================================${NC}"
echo ""
echo "서버 시작:    sudo systemctl start minecraft"
echo "서버 중지:    sudo systemctl stop minecraft"
echo "서버 로그:    sudo journalctl -u minecraft -f"
echo ""
echo "로그 모니터 시작: sudo systemctl start mc-monitor"
echo "로그 모니터 로그: sudo journalctl -u mc-monitor -f"
echo ""
echo -e "${YELLOW}⚠️  백엔드 URL을 설정하세요:${NC}"
echo "    nano $MINECRAFT_DIR/log_monitor.sh"
echo "    BACKEND_URL 변수를 수정하세요"
echo ""
echo -e "${YELLOW}⚠️  서비스 자동 시작 활성화:${NC}"
echo "    sudo systemctl enable minecraft"
echo "    sudo systemctl enable mc-monitor"
