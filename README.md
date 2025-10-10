# ERP Backend – Müşteri & Bonus Yönetimi

(Java 17+/22, Spring Boot 3, PostgreSQL, JPA, MapStruct, Lombok)

Bu repo; **müşteri**, **bonus** ve **fatura** süreçlerini kapsayan, modüler bir Java backend projesidir. Dokümanda istenenleri baz alıp; entity/line yapıları, bonus iş mantığı, REST API’ler, transaction yönetimi, DTO/Mapper ayrımı, generic response ve testleri birebir uyguladık.

---

## 1) Projenin Amacı (dokümantasyon ile birebir)

* “**Java + Spring Boot + PostgreSQL** kullanarak müşteriler, bonus puanları ve satış/iade faturaları ile çalışan bir backend.”
* Hedefler:

    * **Entity tasarımı, line tabloları** (InvoiceLine ve BonusTransaction)
    * **Bonus iş mantığı**: satışta harca, iadede iade et; manuel bonus eklenince bakiye güncelle, hareket yaz.
    * **DTO + MapStruct**: Controller’dan entity dönme yok.
    * **Generic Response**: `ApiResponse` ile tutarlı response gövdesi.
    * **Transaction**: kritik akışlar tek transaction.

---

## 2) Modüler Mimari (doküman beklentisi: en az iki modül)

Proje, **çok-modüllü** Maven yapısındadır:

* **`common-module`**
  Ortak şeyler: `ApiResponse`, `ErrorCode`, exception hiyerarşisi, `AbstractEntity` (id/version/audit), `JpaAuditingConfig`.

* **`crm-module`**
  **Müşteri** ve **bonus ledger** (hareket defteri) burada.

    * **CustomerEntity**: id, name, email, bonus (kalan bonus).
    * **BonusTransactionEntity (line)**: her bonus değişimi (ekleme/harcama/iade) için bir satır.
    * **BonusLedgerService**: bonusun tek doğruluk noktası (delta mantığı + hareket kaydı).

* **`invoice-module`**
  **Fatura başlık** + **fatura satırları (line)** burada.

    * **InvoiceEntity**: müşteri bağlantısı + tip (`RETAIL_SALE`, `WHOLESALE_SALE`, `RETAIL_RETURN`, `WHOLESALE_RETURN`) + toplam tutar.
    * **InvoiceLineEntity (line)**: productId, quantity, price.

* **`erp-application`**
  Spring Boot starter (main app), swagger-ui, full integration testler.

> **Not:** Dokümanda “Invoice servisten müşteri DAO’suna gidilmemeli” deniyordu. Biz de **Invoice → CRM’e sadece servis arayüzü** ile gittik; **DAO’ya inmedik**.

---

## 3) İlişkiler (Entity & Line yapısı)

* **Customer (header)** ↔ **BonusTransaction (line)** → **1-N**
  Tüm bonus hareketlerini (ekleme/harcama/iade) **BonusTransactionEntity**’de tutuyoruz (dokümandaki “bonus hareketleri line tablolarla yönetilmeli” beklentisi karşılandı).

* **Invoice (header)** ↔ **InvoiceLine (line)** → **1-N**
  Ürün/miktar/fiyat satırları **InvoiceLineEntity**’de (doküman “line tablolar” beklentisi karşılandı).

* **Invoice ↔ Customer** → **N-1**
  Fatura bir müşteriye ait.

---

## 4) Bonus Mantığı (tek otorite: Bonus Ledger)

Doküman:

* “Satışta bonus harcanacak; iade olursa bonus geri alınacak.”
* “Bonus tanımlamada bakiye artacak ve hareket yazılacak.”

Uygulama:

* **Delta mantığı** kullandık:

    * **Satış** → `delta = -amount`
    * **İade** → `delta = +amount`
    * **Manuel bonus ekleme** → `delta = +amount`
* Delta’yı **tek bir servis** uygular: `BonusLedgerService`

    * **Bakiye Güncelleme**
    * **Hareket (BonusTransaction) yazma**
    * **Negatif bakiye/ yetersiz bonus** kontrolü
* Böylece bonus kuralları **bir yerde**; tekrar ve dağınıklık yok.

---

## 5) Katmanlar & Bağımlılıklar

* **Controller** → **Service (DTO ile konuşur)** → **DAO/Entity (servis içinde)**
* **InvoiceService** bonusu **kendisi hesaplamaz**; sadece **delta’yı** hesaplar ve **CRM’e** “uygula” der.
* **CRM** tarafında bonusun kuralı/ledger kaydı **tek noktada**.

**Dairesel bağımlılığı kırma:**
Invoice, CRM’in “bonus değiştir” operasyonuna gider. CRM tarafı da “müşteri var mı?” doğrulaması için **CustomerLookupPort** adında **ince bir arayüz** sağlar. Böylece **Invoice ↔ CRM** arasında doğrudan, çift yönlü sıkı bağımlılık oluşmaz.

---

## 6) DTO / Mapper (MapStruct) & Lombok

* **Controller seviyesinde sadece DTO** kullanıldı (doküman şartı).
* `CustomerMapper`, `InvoiceMapper`, `InvoiceLineMapper`, `BonusTransactionMapper` ile **Entity ↔ DTO** dönüşümleri otomatik.
* **Lombok @Data** ile getter/setter/equals/hashCode/toString zahmetsiz (doküman şartı).

---

## 7) Hata Yönetimi & Generic Response

* **`ApiResponse`**:

  ```json
  {
    "outcome_type": "success|error",
    "status": 200|<businessCode>,
    "query": {},
    "data": {},
    "uimessage": [],
    "iomessage": []
  }
  ```
* **`ErrorCode`**: İş kodu + HTTP kodu + mesaj (ör. `INVOICE_BONUS_INSUFFICIENT` → 400).
* **`GlobalExceptionHandler`**:

    * `BaseException` (bizim business/validation/not found) → ilgili HTTP + business code
    * `DataIntegrityViolationException` → 409 “DB bütünlük hatası”
    * `MethodArgumentNotValidException` → 422 “Geçersiz veri (…)”
    * Genel `Exception` → 500

> **Beklenti ile uyum:** Dokümandaki generic response ve net hata yönetimi sağlandı.

---

## 8) JPA & Audit (createdAt/updatedAt)

* **`AbstractEntity`**: tüm entity’lerde **id, version, createdAt, updatedAt**.
* **JPA Auditing** aktif: `@CreatedDate`, `@LastModifiedDate` alanlarını otomatik doldurur (config: `@EnableJpaAuditing`).
* **Not-Null** audit kolonları yüzünden testte aldığın hatayı bu yapı çözdü.

---

## 9) API’ler (dokümandaki ile birebir)

* **Müşteri**

    * `POST /api/customers` → müşteri ekle (email uniq)
    * `GET /api/customers?minBonus&maxBonus` → kalan bonusla listele (filtre opsiyonel)
    * `POST /api/customers/{id}/bonus` → manuel bonus ekle (ledger + bakiye)
    * `GET /api/customers/{id}/bonus-transactions` → hareketleri listele (yeni→eski)
* **Fatura**

    * `POST /api/invoices`

      ```json
      {
        "customerId": 1,
        "type": "RETAIL_SALE|WHOLESALE_SALE|RETAIL_RETURN|WHOLESALE_RETURN",
        "amount": 200,
        "lines": [{"productId":1, "quantity":2, "price":100}]
      }
      ```
    * **Satış** → delta negatif (bonus düşer), **İade** → delta pozitif (bonus artar).
    * Fatura kayıt + satırlar + bonus değişim + ledger **tek akış**.



---


## 10) Testler (Full Integration)

`erp-application` içinde **SpringBootTest (RANDOM_PORT)** ile **gerçek PostgreSQL** üzerinde **uçtan uca** senaryolar koşturuldu. Aşağıdakiler `ErpApplicationIntegrationTest` sınıfındaki testlerin birebir karşılığıdır:

1. **Müşteri oluştur** → 200
2. **Bonus ekle (+500)** → 200
3. **Satış faturası (200)** → bonus **-200**
4. **İade faturası (50)** → bonus **+50**
5. **Müşteri listesi** → kalan bonus **350**
6. **Bonus hareketleri** → “Bonus eklendi / Bonus harcandı / Bonus iade edildi”
7. **Negatif bonus ekleme** → 400
8. **0 tutarlı fatura** → 400
9. **Yetersiz bonusla satış (1000)** → 400
10. **Geçersiz fatura tipi** → 400
11. **Var olmayan müşteriye bonus (id=999)** → 404
12. **Liste filtreleri** → `min=300` (Ali görünür), `max=100` (Ali görünmez), `min=200&max=400` (Ali görünür)


> **Beklenti ile uyum:** Dokümandaki örnek senaryo (500 ekle → 200 satış → 50 iade → kalan 350) ve ek köşe durumları (negatif/0 tutar, yetersiz bakiye, geçersiz tip, 404) tamamen doğrulandı. Ayrıca müşteri listeleme için **dinamik filtre** (Specification) testleri de eklendi.

---


## 11) POM & Bağımlılıklar (özet)

* **Spring Boot 3.3.x** starters (web, data-jpa, validation, aop)
* **PostgreSQL** driver
* **MapStruct** (annotation processor)
* **Lombok** (annotation processor)
* **springdoc-openapi** (Swagger UI)
* Test için **spring-boot-starter-test**, JUnit 5, Mockito

**Multi-module** kök `pom.xml` → alt modüller: `common-module`, `crm-module`, `invoice-module`, `erp-application`.
Alt modüller ihtiyaç duyduklarını **sadece** import eder; gereksiz bağımlılık yok.

---

## 12) Çalıştırma

* PostgreSQL ayarlarını (`application-*.properties`) düzenleyin.
* Maven ile derleyin:

  ```bash
  mvn clean install
  ```
* Uygulamayı başlatın:

  ```bash
  cd erp-application
  mvn spring-boot:run
  ```
* Swagger UI:

  ```
  http://localhost:<port>/swagger-ui.html
  ```

---

## 13) Tasarım Tercihleri – “Neden böyle yaptık?”

* **Bonus tek kaynak**: Bonus mantığı tek yerde (BonusLedgerService) — **tekrar yok**, hata alanı küçük.
* **Port (CustomerLookupPort)**: Invoice/Bonus CRM’i **servis üzerinden** değil, **ince bir arayüz** üzerinden “sadece okur”; böylece **dairesel bağımlılık** kesildi.
* **DTO/Mapper**: Controller seviyesi **sade ve stabil** kaldı. Entity’ler iç detay olarak kaldı.
* **Transaction**: Bonus + ledger + fatura işlemleri **tek transaction** ile tutarlı.
* **Audit + Version**: İzlenebilirlik ve optimistic locking için temel zemin hazır.

---

## 14) Dokümandaki Ek Notlara Karşılık Verdiğimiz Noktalar

* **“Line tablolarla yönetim”** → InvoiceLine & BonusTransaction **line** olarak kuruldu.
* **“BonusTransaction hem ekleme hem harcama/iade işlemine kayıt atmalı”** → Delta işaretli hareket satırları yazılıyor.
* **“Bonus negatif olamaz”** → Satışta yetersiz bonus hatası, savunmacı negatif bakiye kontrolü.
* **“Response generic olmalı”** → `ApiResponse` her yerde.
* **“Invoice servisten müşteri DAO’suna gitme”** → Invoice → CRM **servis/port** ile.
* **“Mapper + DTO + Lombok”** → Tamamı kullanıldı.

---


## 15) Kısa Özet

* İstenen tüm **entity/line** yapıları kuruldu.
* **Bonus iş mantığı** tek otoriteyle (ledger + delta) yönetildi.
* **REST API**’ler, **DTO/Mapper**, **transaction**, **audit**, **generic response** ve **full integration testler** doküman ile uyumlu şekilde tamamlandı.
* Modüler yapı ve bağımlılıklar **sade ve sağlıklı**: Invoice, CRM’e **doğrudan DAO** ile değil, **servis/port** üzerinden gidiyor.

