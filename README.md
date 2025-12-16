# Sekwang Minecraft Server

마인크래프트 베드락 전용 서버 + 이벤트 로깅 시스템

## 📦 구성 요소

- **Bedrock Dedicated Server (BDS)** - 공식 마인크래프트 베드락 서버
- **Event Logger Addon** - 채팅, 사망, 리스폰 이벤트를 서버 로그에 기록
- **Log Monitor** - 서버 로그를 감시하여 백엔드 API로 이벤트 전송

## 🚀 빠른 설치

```bash
# 저장소 클론
git clone https://github.com/2020114025-CPF21A/Sekwang_Minecraft.git
cd Sekwang_Minecraft

# 설치 스크립트 실행 (Ubuntu 22.04/24.04)
chmod +x setup.sh
./setup.sh
```

## 📁 폴더 구조

```
Sekwang_Minecraft/
├── event_logger_addon/      # 베드락 스크립트 API 애드온
│   ├── manifest.json
│   └── scripts/main.js
├── log_monitor.sh           # 로그 모니터링 스크립트
├── world_behavior_packs.json # 월드 애드온 설정
├── enable_beta_apis.py      # Beta API 활성화 스크립트
├── setup.sh                 # 자동 설치 스크립트
└── README.md
```

## ⚙️ 설정

### 백엔드 URL 변경

`log_monitor.sh` 파일에서 백엔드 URL을 변경하세요:

```bash
BACKEND_URL="http://YOUR_BACKEND_IP:8080/api/minecraft"
```

### 포트 설정

- **19132** - 마인크래프트 기본 포트 (UDP)
- **19133** - IPv6 포트 (UDP)

방화벽에서 UDP 19132 포트를 열어주세요.

## 🎮 기능

### 캡처되는 이벤트

| 이벤트 | 설명 |
|--------|------|
| CHAT | 플레이어 채팅 메시지 |
| DEATH | 플레이어 사망 (원인 포함) |
| RESPAWN | 플레이어 리스폰 |
| SPAWN | 플레이어 첫 스폰 (접속) |
| JOIN | 서버 접속 |
| LEAVE | 서버 퇴장 |

### Beta APIs

채팅 이벤트를 캡처하려면 월드에서 **Beta APIs 실험**이 활성화되어야 합니다.
`enable_beta_apis.py` 스크립트로 기존 월드에 Beta APIs를 활성화할 수 있습니다.

## 📄 라이선스

MIT License
