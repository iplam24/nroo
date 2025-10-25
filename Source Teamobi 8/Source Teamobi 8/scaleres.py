from PIL import Image
import os

# 📂 Thư mục gốc chứa ảnh
input_folder = r"C:\Users\vxlam\Downloads\icon\x4"
# 📁 Thư mục xuất ảnh thu nhỏ
output_folder = r"C:\Users\vxlam\Downloads\icon\x3"

# 🌈 Tỉ lệ thu nhỏ (ví dụ: 0.25 = 25%, 0.5 = 50%, 0.75 = 75%)
scale = 0.75

if not os.path.exists(output_folder):
    os.makedirs(output_folder)

for filename in os.listdir(input_folder):
    if filename.lower().endswith(('.jpg', '.jpeg', '.png', '.bmp', '.webp')):
        img_path = os.path.join(input_folder, filename)
        img = Image.open(img_path)

        # 💞 Tính kích thước mới
        new_size = (int(img.width * scale), int(img.height * scale))

       
        resized_img = img.resize(new_size, Image.LANCZOS)

        # 💾 Lưu sang thư mục mới
        save_path = os.path.join(output_folder, filename)
        resized_img.save(save_path)
        print(f"✅ {filename} → {new_size}")

print("Xong rùi đó anh Lâm đẹp trai! Tất cả ảnh đã được thu nhỏ và lưu vào:", output_folder)
