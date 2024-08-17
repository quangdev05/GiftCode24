![GitHub release (latest by date)](https://img.shields.io/github/v/release/QuangDev05/GiftCode24)
![GitHub license](https://img.shields.io/github/license/PhamQuang2008/GiftCode)
![Supported server version](https://img.shields.io/badge/minecraft-1.12x%20--_Latest-green)
[![Discord](https://img.shields.io/discord/1247029974154612828.svg?label=&logo=discord&logoColor=ffffff&color=7389D8&labelColor=6A7EC2)](https://discord.gg/HsSUVGSc3c)

# GiftCode24 Plugin
**GiftCode24** GiftCode24 là plugin quản lý mã quà tặng mạnh mẽ dành cho Minecraft. Nó cho phép quản trị viên tạo, quản lý và sử dụng mã quà tặng với các tùy chọn có thể định cấu hình như mức sử dụng tối đa, thời hạn sử dụng và giới hạn dành riêng cho người chơi. Các tính năng bao gồm tạo, xóa, bật, tắt, liệt kê và gán mã quà tặng. GiftCode24 giúp việc quản lý mã quà tặng trở nên đơn giản và hiệu quả.

## Hướng dẫn cài đặt
1. **Tải và cài đặt plugins**
   - Tải plugins trên các nền tảng chính thức.
   - Kéo file Plugins vào mục Plugins trong file server.
2. **Kích hoạt Plugins**
   - Chạy lại server hoặc sử dụng PlugMan để kích hoạt.

## Danh sách lệnh
### Danh sách lệnh cho Admin (/giftcode)
- `/gc create <code>`: Tạo mã quà tặng.
- `/gc create <name> random`: Tạo ngẫu nhiên 10 mã quà tặng.
- `/gc del <code>`: Xóa mã quà tặng.
- `/gc reload`: Tải lại Plugins.
- `/gc enable <code>`: Kích hoạt mã quà tặng.
- `/gc disable <code>`: Vô hiệu hóa mã quà tặng.
- `/gc list`: Danh sách mã quà tặng.
- `/gc assign <code> <player>`: Gán mã quà tặng cho người chơi.
### Dách sách lệnh cho người chơi (/code)
- `/code <code>`: Nhập mã quà tặng.

## Config mặc định
### config.yml
```yaml
check-update: true # false để tắt.

messages:
  invalid-code: Mã quà tặng bạn đã nhập không hợp lệ.
  code-expired: Mã quà tặng bạn nhập đã hết hạn.
  max-uses-reached: Mã quà tặng bạn nhập đã đạt số lần sử dụng tối đa.
  code-disabled: Mã quà tặng bạn nhập hiện đã bị vô hiệu hóa.
  code-redeemed: Bạn đã đổi mã quà tặng thành công!
  code-already-redeemed: Bạn đã đổi mã này.
```
### giftcode.yml 
```yaml
# File cấu hình Gift Code.
# Tệp này xác định cài đặt để quản lý mã quà tặng trong plugin.

# Code ví dụ:
samplecode:
  commands:
  - give %player% diamond 1
  message: You have received 1 diamond! # Thông báo hiển thị cho người chơi khi đổi mã.
  max-uses: 10 # Tổng số lần tối đa mà mã này có thể được đổi. Đặt thành 999999999 để không giới hạn số lần sử dụng mã.
  expiry: '2024-12-31T23:59:59' # Ngày và giờ hết hạn của mã (định dạng ISO 8601).
  enabled: true # true để bật, false để tắt.
  player-max-uses: 1 # Số lần tối đa mỗi người chơi có thể đổi mã này. Đặt thành -1 để sử dụng không giới hạn.
```
### dataplayer.yml 
```yaml
# File dữ liệu người chơi.
```

## Contact
- **Tác giả:** QuangDev05 [GnauQ]
- **Facebook:** [Phạm Quang](https://www.facebook.com/quangdev05)
- **Discord:** quangdev05
- **Discord cộng đồng:** [QuangDev05 | Community](https://discord.gg/HsSUVGSc3c)
- **Github:** [PhamQuang2008/GiftCode](https://github.com/QuangDev05/GiftCode)
- **Spigot:** [GiftCode24 trên Github](https://www.spigotmc.org/resources/giftcode24.117453/)
- **BuiltByBit:** [GiftCode24 trên BuiltByBit](https://builtbybit.com/resources/giftcode24.46671/)
