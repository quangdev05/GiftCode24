![GitHub release (latest by date)](https://img.shields.io/github/v/release/quangdev05/GiftCode24)
![GitHub license](https://img.shields.io/github/license/quangdev05/GiftCode24)
![Supported server version](https://img.shields.io/badge/Minecraft-1.13x%20--_1.21x-green)
[![Discord](https://img.shields.io/discord/1247029974154612828.svg?label=&logo=discord&logoColor=ffffff&color=7389D8&labelColor=6A7EC2)](https://discord.gg/HsSUVGSc3c)

# GiftCode24 Plugin
**GiftCode24** là plugin quản lý mã quà tặng mạnh mẽ dành cho Minecraft. Nó cho phép quản trị viên tạo, quản lý và sử dụng mã quà tặng với các tùy chọn có thể định cấu hình như mức sử dụng tối đa, thời hạn sử dụng và giới hạn dành riêng cho người chơi. Các tính năng bao gồm tạo, xóa, bật, tắt, liệt kê và gán mã quà tặng. GiftCode24 giúp việc quản lý mã quà tặng trở nên đơn giản và hiệu quả.

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
#  ██████╗ ██╗███████╗████████╗ ██████╗ ██████╗ ██████╗ ███████╗██████╗ ██╗  ██╗
# ██╔════╝ ██║██╔════╝╚══██╔══╝██╔════╝██╔═══██╗██╔══██╗██╔════╝╚════██╗██║  ██║
# ██║  ███╗██║█████╗     ██║   ██║     ██║   ██║██║  ██║█████╗   █████╔╝███████║
# ██║   ██║██║██╔══╝     ██║   ██║     ██║   ██║██║  ██║██╔══╝  ██╔═══╝ ╚════██║
# ╚██████╔╝██║██║        ██║   ╚██████╗╚██████╔╝██████╔╝███████╗███████╗     ██║
#  ╚═════╝ ╚═╝╚═╝        ╚═╝    ╚═════╝ ╚═════╝ ╚═════╝ ╚══════╝╚══════╝     ╚═╝
# Plugin made by QuangDev05.
# Plugin made in Vietnam.
# Facebook: https://www.facebook.com/quangdev05
# Discord: quangdev05.
# Discord Community: https://discord.gg/MdgvJnegbM
# Github: https://github.com/QuangDev05/GiftCode24
# Spigot: https://www.spigotmc.org/resources/giftcode24.117453/
# BuiltByBit: https://builtbybit.com/resources/giftcode24.46671/

check-update: true # false để tắt.

messages:
  invalid-code: "Mã quà tặng bạn đã nhập không hợp lệ."
  code-expired: "Mã quà tặng bạn nhập đã hết hạn."
  max-uses-reached: "Mã quà tặng đã hết lượt sử dụng."
  max-uses-perip: "Mã quà tặng này đã được sử dụng quá số lần cho phép từ địa chỉ IP của bạn."
  code-disabled: "Mã quà tặng bạn nhập hiện đã bị vô hiệu hóa."
  code-redeemed: "Bạn đã đổi mã quà tặng thành công!"
  code-already-redeemed: "Bạn đã nhập mã này quá số lần quy định."
```
### giftcode.yml 
```yaml
# File cấu hình Gift Code.
# Tệp này xác định cài đặt để quản lý mã quà tặng trong plugin.

# Code ví dụ:
samplecode:
# Lệnh quà tặng khi đổi mã.
  commands:
    - give %player% diamond 1
# Thông báo hiển thị cho người chơi khi đổi mã.
  message: "You have received 1 diamond!"
# Tổng số lần tối đa mà mã này có thể được đổi. Đặt thành 999999999 để không giới hạn số lần sử dụng mã.
  max-uses: 10
# Ngày và giờ hết hạn của mã (định dạng ISO 8601).
  expiry: "2029-12-31T23:59:59"
# true để bật, false để tắt.
  enabled: true
# Số lần tối đa mỗi người chơi có thể đổi mã này. Đặt thành -1 để sử dụng không giới hạn.
  player-max-uses: 1
# Giới hạn IP cho mã quà tặng. Đặt thành 0 để tắt tính năng, 1 là giới hạn 1 lần trên mỗi IP, 2 là 2 lần, 3 là 3 lần, ...
  player-max-uses-perip: 1
```
### dataplayer.yml 
```yaml
# File dữ liệu người chơi.
# Dữ liệu ví dụ:
players:
  59daa05c-3d1e-425c-8428-67606beaff0e:
    ip: 121.22.153.71
    usedCodes:
      - samplecode
      - samplecode2
      - samplecode3
```

## Contact
- **Tác giả:** QuangDev05 [GnauQ]
- **Facebook:** [Phạm Quang](https://www.facebook.com/quangdev05)
- **Discord:** quangdev05
- **Discord cộng đồng:** [QuangDev05 | Community](https://discord.gg/HsSUVGSc3c)
- **Github:** [QuangDev05/GiftCode](https://github.com/QuangDev05/GiftCode)
- **Spigot:** [GiftCode24 trên Github](https://www.spigotmc.org/resources/giftcode24.117453/)
- **BuiltByBit:** [GiftCode24 trên BuiltByBit](https://builtbybit.com/resources/giftcode24.46671/)
