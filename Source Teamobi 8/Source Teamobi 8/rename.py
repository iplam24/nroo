import os

# 📁 Thư mục chứa ảnh
folder = r"C:\Users\vxlam\Downloads\icon\x4"

# 🔢 Bắt đầu từ số 1028
prefix = ""
start_num = 19018

# 📂 Lọc tất cả file ảnh (jpg, png, jpeg)
files = sorted([f for f in os.listdir(folder) if f.lower().endswith(('.jpg', '.png', '.jpeg'))])

# 🔁 Đổi tên lần lượt
for i, filename in enumerate(files):
    ext = os.path.splitext(filename)[1]  # phần đuôi .png hoặc .jpg
    new_name = f"{prefix}{start_num + i}{ext}"
    os.rename(os.path.join(folder, filename), os.path.join(folder, new_name))
    print(f"Đã đổi {filename} ➜ {new_name}")

print("✨💖 Đổi tên hoàn tất 💖✨")
