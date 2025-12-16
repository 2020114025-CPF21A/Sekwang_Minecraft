#!/usr/bin/env python3
"""
level.dat 파일에 Beta APIs (gametest) 실험 활성화
베드락 NBT 형식: Little Endian
"""

import struct

input_file = '/home/ubuntu/minecraft/worlds/Bedrock level/level.dat'
output_file = '/home/ubuntu/minecraft/worlds/Bedrock level/level.dat'
backup_file = '/home/ubuntu/minecraft/worlds/Bedrock level/level.dat.backup2'

with open(input_file, 'rb') as f:
    data = bytearray(f.read())

# 백업
with open(backup_file, 'wb') as f:
    f.write(data)

print(f"Original size: {len(data)} bytes")

# experiments 섹션 찾기
experiments_pos = data.find(b'experiments')
if experiments_pos == -1:
    print("experiments not found!")
    exit(1)

print(f"experiments at position: {experiments_pos}")

# NBT 구조:
# TAG_Compound (0x0A) + name_length (2 bytes LE) + name + ... entries ... + TAG_End (0x00)
# TAG_Byte (0x01) + name_length (2 bytes LE) + name + value (1 byte)

# experiments 뒤에 gametest 바이트 추가
# TAG_Byte = 0x01
# name_length = 8 (little endian: 0x08, 0x00)  
# name = "gametest"
# value = 1

gametest_entry = bytes([
    0x01,       # TAG_Byte
    0x08, 0x00, # name length = 8 (LE)
]) + b'gametest' + bytes([0x01])  # value = 1

# experiments_ever_used 찾기
ever_used_pos = data.find(b'experiments_ever_used')
if ever_used_pos == -1:
    print("experiments_ever_used not found!")
    exit(1)

print(f"experiments_ever_used at position: {ever_used_pos}")

# experiments_ever_used 값을 1로 변경
# TAG_Byte (0x01) + name_len (2b) + name (21b) + value (1b)
# 값은 이름 바로 뒤에 있음
value_pos = ever_used_pos + 21  # "experiments_ever_used" = 21 chars
print(f"experiments_ever_used value at: {value_pos}, current: {data[value_pos]}")
data[value_pos] = 1

# saved_with_toggled_experiments 찾기
saved_pos = data.find(b'saved_with_toggled_experiments')
if saved_pos != -1:
    saved_value_pos = saved_pos + 30  # "saved_with_toggled_experiments" = 30 chars
    print(f"saved_with_toggled_experiments at: {saved_pos}, value at: {saved_value_pos}, current: {data[saved_value_pos]}")
    data[saved_value_pos] = 1

# gametest 엔트리를 experiments compound 안에 삽입
# experiments compound의 끝(TAG_End = 0x00) 앞에 삽입해야 함
# experiments_ever_used 앞에 삽입

# experiments 섹션 찾기: "experiments" 뒤에 compound가 시작됨
# experiments compound의 시작 위치 계산
# experiments 검색 결과에서 compound 살펴보기

# 간단한 방법: experiments_ever_used 바로 앞에 gametest 삽입
insert_pos = ever_used_pos - 3  # TAG_Byte(1) + name_len(2) 앞

print(f"Inserting gametest entry at position: {insert_pos}")
print(f"Entry bytes: {gametest_entry.hex()}")

# 데이터 삽입
new_data = data[:insert_pos] + gametest_entry + data[insert_pos:]

# 헤더의 길이 필드 업데이트 (bytes 4-7, little endian int32)
old_len = struct.unpack('<I', data[4:8])[0]
new_len = old_len + len(gametest_entry)
print(f"Updating length: {old_len} -> {new_len}")
new_data[4:8] = struct.pack('<I', new_len)

print(f"New size: {len(new_data)} bytes")

with open(output_file, 'wb') as f:
    f.write(new_data)

print("Done! level.dat updated with Beta APIs experiment enabled.")
