# Sekwang Minecraft Server

마인크래프트 베드락 서버 + 이벤트 로깅 시스템

## 🎮 기능

- **채팅 로깅** - 플레이어 채팅 메시지 캡처
- **사망 이벤트** - 죽음 원인과 함께 기록
- **접속/퇴장** - 플레이어 입퇴장 기록
- **리스폰** - 죽고 다시 살아날 때 기록

## 📁 구조

```
Sekwang_Minecraft/
├── event_logger_addon/     # 마인크래프트 스크립트 API 애드온
│   ├── manifest.json       # 애드온 설정 (Beta APIs 2.5.0-beta)
│   └── scripts/main.js     # 이벤트 핸들러
├── log_monitor.sh          # 로그 모니터링 스크립트
├── enable_beta_apis.py     # level.dat에 Beta APIs 활성화
├── setup.sh                # 서버 설치 스크립트
└── world_behavior_packs.json # 월드 behavior pack 설정
```

## 🚀 설치 방법

### 1. 서버에서 실행

```bash
git clone https://github.com/2020114025-CPF21A/Sekwang_Minecraft.git
cd Sekwang_Minecraft
chmod +x setup.sh
./setup.sh
```

### 2. 수동 설치

1. 마인크래프트 베드락 서버 다운로드 및 설치
2. `event_logger_addon` 폴더를 `behavior_packs/`에 복사
3. `world_behavior_packs.json`을 월드 폴더에 복사
4. `enable_beta_apis.py` 실행하여 Beta APIs 활성화
5. `log_monitor.sh`를 systemd 서비스로 등록

## ⚙️ 환경 변수

`log_monitor.sh`에서 백엔드 URL 수정:
```bash
BACKEND_URL="http://YOUR_BACKEND_IP:8080/api/minecraft"
```

## 📡 API 엔드포인트

로그 모니터가 호출하는 API:
- `GET /api/minecraft/log/join?player={name}` - 접속
- `GET /api/minecraft/log/leave?player={name}` - 퇴장
- `GET /api/minecraft/event?type={type}&player={name}&message={msg}` - 이벤트

## 🔧 요구사항

- Ubuntu 22.04+ (또는 호환 Linux)
- Python 3.x (URL 인코딩용)
- curl
- 마인크래프트 베드락 서버 1.21.x

## 📝 라이선스

MIT License
