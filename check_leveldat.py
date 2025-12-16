#!/usr/bin/env python3
# level.dat 파일에 Beta APIs 실험 활성화

import struct
import sys

# level.dat 파일 읽기
with open('/home/ubuntu/minecraft/worlds/Bedrock level/level.dat', 'rb') as f:
    data = f.read()

# 백업
with open('/home/ubuntu/minecraft/worlds/Bedrock level/level.dat.backup', 'wb') as f:
    f.write(data)

# experiments_ever_used 찾아서 1로 변경
# 베드락 level.dat은 앞 8바이트가 헤더
header = data[:8]
nbt_data = data[8:]

# 간단한 방법: 텍스트로 검색해서 수정
# gametest 실험을 활성화하는 바이트 패턴 추가

# 현재 가지고 있는 데이터 출력
print(f"File size: {len(data)} bytes")
print("Header:", header.hex())

# experiments 관련 문자열 검색
if b'experiments' in nbt_data:
    print("Found 'experiments' in level.dat")
    idx = nbt_data.find(b'experiments')
    print(f"Position: {idx}")
    print(f"Context: {nbt_data[max(0,idx-20):idx+50]}")
else:
    print("'experiments' not found")

if b'gametest' in nbt_data:
    print("Found 'gametest' - Beta APIs may already be configured")
else:
    print("'gametest' not found - need to add Beta APIs experiment")
