> ⚠️ **OBSOLETE — kiến trúc "MySQL managed resource riêng" đã bị thay thế.** Quyết định deploy hiện
> hành (xem Validation Log của plan migrate storage) là chạy MySQL **trong cùng** `docker-compose.yml`
> với app (service `db`, image `mysql:8.4`), không tách thành Coolify managed resource nữa. Xem
> [`../../docs/deployment-guide.md`](../../docs/deployment-guide.md) cho hướng dẫn deploy hiện hành.
> Giữ file này lại chỉ để tham khảo lịch sử/nếu sau này quay lại kiến trúc managed-resource.

# MySQL Production Setup trên Coolify

Hướng dẫn tạo MySQL làm **managed resource riêng** trên Coolify (tách khỏi `docker-compose.yml`
của app, theo quyết định kiến trúc "Managed services riêng" — xem [`../docker-compose.yml`](../docker-compose.yml)).
App chỉ cần biết connection string qua env vars `DB_URL`/`DB_USERNAME`/`DB_PASSWORD`.

## 1. Tạo resource MySQL trên Coolify

1. Coolify Dashboard → project chứa app `vu-gia` → **+ New Resource** → **Databases** → **MySQL**.
2. Chọn version **8.4** (khớp `mysql:8.4` đang dùng ở compose dev — tránh lệch behavior/collation
   giữa dev và prod).
3. Điền:
   - **Database name**: ví dụ `vu_gia_prod` (không dùng `dev_db`/`db_vu_gia_fullstack` để tránh nhầm với local).
   - **Root password** / **User password**: để Coolify tự sinh (random, mạnh) hoặc tự đặt — **không**
     tái sử dụng password trong `docker-compose.yml` cũ (`rootpassword`).
4. Deploy resource. Coolify tự tạo volume persistent cho `/var/lib/mysql`.

## 2. Networking — App phải cùng Destination/Server với DB

Coolify chỉ cho container nói chuyện qua **internal Docker network** nếu chúng nằm cùng
**Destination** (cùng Docker server + cùng network Coolify quản lý). Kiểm tra:

- Deploy app resource và MySQL resource lên **cùng server** trong Coolify.
- Coolify tự inject internal hostname dạng `<resource-uuid>` hoặc tên service — xem ở tab
  **Configuration → Connection** của MySQL resource sau khi deploy, phần "Internal" hiển thị host:port
  dùng được từ container khác (không phải `localhost`).
- **Không** cần public port MySQL ra internet — chỉ app mới cần connect, giữ MySQL hoàn toàn nội bộ
  (không set FQDN cho DB, không public port `3306`).

## 3. Set biến môi trường cho app

Trong app resource → **Environment Variables**, set:

```bash
DB_URL=jdbc:mysql://<internal-host>:3306/vu_gia_prod?allowPublicKeyRetrieval=true&useSSL=false
DB_USERNAME=<user Coolify sinh ra>
DB_PASSWORD=<password Coolify sinh ra>
```

- `<internal-host>` lấy từ tab Connection của MySQL resource (mục 2).
- Không thêm `createDatabaseIfNotExist=true` ở prod — database đã được Coolify tạo sẵn lúc deploy resource,
  tránh app có quyền tự tạo DB ngoài ý muốn.
- `useSSL=false` giữ nguyên vì kết nối chạy trong mạng nội bộ Coolify (không qua internet); nếu MySQL
  resource public ra ngoài (không khuyến nghị) thì phải bật `useSSL=true` + cấu hình cert.

## 4. Schema & seed — Flyway tự chạy, không cần thao tác tay

Theo cơ chế đã mô tả ở [`RUN_AND_SEED.md`](./RUN_AND_SEED.md#1-cơ-chế-tự-migrate--seed):

- Lần deploy đầu tiên, DB rỗng → Flyway chạy `V1__init_db.sql` (schema) rồi `V2__seed_db.sql` (seed mẫu)
  tự động khi app khởi động. **Không cần** chạy migration tay.
- Nếu bạn **không** muốn seed dữ liệu mẫu (catalog/news/banner demo) lên production — sửa
  `V2__seed_db.sql` thành no-op hoặc xoá seed trước lần deploy đầu, vì Flyway chỉ chạy 1 lần/checksum
  (sửa sau khi đã chạy sẽ bị "checksum mismatch", phải tạo `V3__...sql` mới để dọn dữ liệu seed cũ).
- `spring.jpa.hibernate.ddl-auto=validate` (đã cấu hình sẵn) → Hibernate **không** tự sửa schema ở
  prod, chỉ validate khớp entity. An toàn.

## 5. Backup (bắt buộc cho production)

1. MySQL resource → tab **Backups** → bật **Scheduled Backup**.
2. Cấu hình S3-compatible storage đích (có thể trỏ vào chính MinIO resource ở
   [`MINIO_DOCKER_PRODUCTION_SETUP.md`](./MINIO_DOCKER_PRODUCTION_SETUP.md), tạo riêng bucket
   `mysql-backups` — không dùng chung bucket `assets`/`products`).
3. Đặt lịch tối thiểu **daily**, retention theo nhu cầu (ví dụ giữ 14 bản gần nhất).
4. Test thử **Restore** 1 lần trên resource nháp trước khi tin tưởng hoàn toàn vào backup.

## 6. Checklist go-live

- [ ] MySQL resource deploy cùng server với app, version 8.4.
- [ ] Database/user/password **không** trùng giá trị dev (`rootpassword`, `db_vu_gia_fullstack`).
- [ ] `DB_URL/DB_USERNAME/DB_PASSWORD` set đúng ở app resource, trỏ internal host (không public port 3306).
- [ ] App deploy lần đầu → check log thấy Flyway "Successfully applied migrations" (`docker compose logs -f app`
  tương đương log của Coolify).
- [ ] Đã quyết định seed demo data hay không trước lần deploy đầu tiên (không sửa được sau khi đã chạy).
- [ ] Scheduled backup bật, đã test restore 1 lần.

## Câu hỏi chưa xử lý

- Có cần giữ seed data demo (`V2__seed_db.sql`) trên production hay xoá/thay bằng seed thật? Cần quyết
  định **trước** lần deploy app đầu tiên vì Flyway không cho sửa lại migration đã chạy.
