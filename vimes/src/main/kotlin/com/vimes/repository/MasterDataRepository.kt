package com.vimes.repository

import com.vimes.domain.model.Organization
import com.vimes.domain.model.Product
import com.vimes.domain.model.Warehouse
import com.vimes.domain.port.MasterDataRepositoryPort
import org.springframework.stereotype.Repository
import java.text.Normalizer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

@Repository
class MasterDataRepository : MasterDataRepositoryPort {

    private val products = ConcurrentHashMap<UUID, Product>()
    private val warehouses = ConcurrentHashMap<UUID, Warehouse>()
    private val organizations = ConcurrentHashMap<UUID, Organization>()

    init {
        seedProducts()
        seedWarehouses()
        seedOrganizations()
    }

    private fun seedProducts() {
        val baseProducts = listOf(
            Triple("VT001", "Thép cuộn Phi 6", "Kg" to 15000.0),
            Triple("VT002", "Xi măng Hà Tiên PCB40", "Bao" to 95000.0),
            Triple("VT003", "Cát vàng xây dựng", "m3" to 320000.0),
            Triple("VT004", "Đá hộc xây dựng 1x2", "m3" to 280000.0),
            Triple("VT005", "Gạch tuynel đặc 4x8x19", "Viên" to 1200.0),
            Triple("VT006", "Sơn lót chống thấm Kova", "Thùng" to 1450000.0),
            Triple("VT007", "Tấm thạch cao Vĩnh Tường 9mm", "Tấm" to 165000.0),
            Triple("VT008", "Ống nhựa PVC Tiền Phong D90", "Mét" to 48000.0),
            Triple("VT009", "Dây điện đôi Cadivi 2.5mm2", "Cuộn" to 850000.0),
            Triple("VT010", "Thép cây VinaKyoei D16", "Cây" to 210000.0),
            Triple("VT011", "Bê tông tươi thương phẩm R28", "m3" to 1250000.0),
            Triple("VT012", "Cửa nhôm xingfa nhập khẩu", "m2" to 2200000.0),
            Triple("VT013", "Kính cường lực 10mm", "m2" to 680000.0),
            Triple("VT014", "Đèn LED âm trần Rạng Đông 9W", "Cái" to 85000.0),
            Triple("VT015", "Ngói màu cao cấp SCG", "Viên" to 22000.0),
            Triple("VT016", "Tấm lợp thông minh Polycarbonate", "m2" to 350000.0),
            Triple("VT017", "Keo dán gạch Weber", "Bao" to 230000.0),
            Triple("VT018", "Bột trét tường Dulux", "Bao" to 380000.0),
            Triple("VT019", "Thiết bị vệ sinh Inax", "Bộ" to 3500000.0),
            Triple("VT020", "Lưới thép B40 bọc nhựa", "Cuộn" to 1100000.0),
            Triple("VT021", "Que hàn Kim Tín KT-421", "Hộp" to 125000.0),
            Triple("VT022", "Băng cản nước PVC V150", "Cuộn" to 1850000.0),
            Triple("VT023", "Thép hình I200 An Khánh", "Cây" to 3400000.0),
            Triple("VT024", "Chậu rửa inox 304 Tân Á", "Cái" to 1200000.0),
            Triple("VT025", "Công tắc ổ cắm Panasonic", "Bộ" to 65000.0)
        )

        val fixedIds = mapOf(
            "VT001" to UUID.fromString("e7d2b8a0-1234-4567-89ab-cdef01234567"),
            "VT002" to UUID.fromString("a8e3c9b1-2345-6789-abcd-ef0123456789"),
            "VT003" to UUID.fromString("b9f4d0c2-3456-7890-bcde-f12345678901")
        )

        for (i in 1..65) {
            val baseIndex = (i - 1) % baseProducts.size
            val base = baseProducts[baseIndex]
            val code = String.format("VT%03d", i)
            val name = if (i <= 25) base.second else "${base.second} Loại $i"
            val unit = base.third.first
            val price = base.third.second + ((i - 1) * 100)

            val id = fixedIds[code] ?: UUID.nameUUIDFromBytes(code.toByteArray())
            products[id] = Product(id, code, name, unit, price)
        }
    }

    private fun seedWarehouses() {
        val baseWarehouses = listOf(
            Triple("KHO-TONG-HN", "Kho Tổng Hà Nội", "KCN Sài Đồng, Long Biên, Hà Nội"),
            Triple("KHO-HCM", "Kho Chi Nhánh TP.HCM", "KCN Tân Bình, Tân Phú, TP.HCM"),
            Triple("KHO-DN", "Kho Miền Trung Đà Nẵng", "KCN Hòa Cấm, Cẩm Lệ, Đà Nẵng"),
            Triple("KHO-HP", "Kho Cảng Hải Phòng", "KCN Đình Vũ, Hải An, Hải Phòng"),
            Triple("KHO-CT", "Kho Tây Nam Bộ Cần Thơ", "KCN Trà Nóc, Bình Thủy, Cần Thơ"),
            Triple("KHO-BD", "Kho Bình Dương", "KCN VSIP 1, Thuận An, Bình Dương"),
            Triple("KHO-QN", "Kho Quảng Ninh", "KCN Cái Lân, Hạ Long, Quảng Ninh"),
            Triple("KHO-VT", "Kho Bà Rịa - Vũng Tàu", "KCN Phú Mỹ, Thị xã Phú Mỹ, BR-VT"),
            Triple("KHO-KH", "Kho Khánh Hòa", "KCN Suối Dầu, Cam Lâm, Khánh Hòa"),
            Triple("KHO-NA", "Kho Nghệ An", "KCN Bắc Vinh, TP. Vinh, Nghệ An")
        )

        val fixedIds = mapOf(
            "KHO-TONG-HN" to UUID.fromString("c9a646d3-9c61-4cd7-bf5b-9b4dc257850a"),
            "KHO-HCM" to UUID.fromString("d0b757e4-ad72-5de8-c06c-ac5ed368961b")
        )

        for (i in 1..35) {
            val baseIndex = (i - 1) % baseWarehouses.size
            val base = baseWarehouses[baseIndex]
            val code = if (i <= 10) base.first else String.format("KHO-%02d", i)
            val name = if (i <= 10) base.second else "${base.second} - Phân khu $i"
            val location = base.third

            val id = fixedIds[code] ?: UUID.nameUUIDFromBytes(code.toByteArray())
            warehouses[id] = Warehouse(id, code, name, location)
        }
    }

    private fun seedOrganizations() {
        val baseOrgs = listOf(
            Triple("VIMES-HN", "CÔNG TY CỔ PHẦN VIMES", "Kế toán - Quản lý Vật tư"),
            Triple("VIMES-HCM", "CHI NHÁNH VIMES TP.HCM", "Phòng Vật tư Kho vận"),
            Triple("VIMES-DN", "CHI NHÁNH VIMES ĐÀ NẴNG", "Ban Quản lý Dự án Miền Trung"),
            Triple("VIMES-HP", "CHI NHÁNH VIMES HẢI PHÒNG", "Phòng Logistics & Xuất Nhập Khẩu"),
            Triple("VIMES-CT", "CHI NHÁNH VIMES CẦN THƠ", "Trung tâm Phân phối Tây Nam Bộ"),
            Triple("VIMES-BD", "BAN QUẢN LÝ DỰ ÁN BÌNH DƯƠNG", "Khối Công trình Công nghiệp"),
            Triple("VIMES-QN", "XƯỞNG GIA CÔNG CƠ KHÍ QUẢNG NINH", "Bộ phận Sản xuất Kết cấu Thép"),
            Triple("VIMES-VT", "XÍ NGHIỆP THI CÔNG BÀ RỊA", "Phòng Kỹ thuật Thi công"),
            Triple("VIMES-KH", "TRUNG TÂM BẢO TRÌ KHÁNH HÒA", "Bộ phận Vật tư Thay thế"),
            Triple("VIMES-NA", "BỘ PHẦN TỔNG KHO NGHỆ AN", "Khối Cung ứng Vật tư")
        )

        val fixedIds = mapOf(
            "VIMES-HN" to UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"),
            "VIMES-HCM" to UUID.fromString("e36ab00a-47bb-3261-9456-0d01a1b2c3d4")
        )

        for (i in 1..35) {
            val baseIndex = (i - 1) % baseOrgs.size
            val base = baseOrgs[baseIndex]
            val code = if (i <= 10) base.first else String.format("VIMES-ORG-%02d", i)
            val name = if (i <= 10) base.second else "${base.second} Đội $i"
            val dept = base.third

            val id = fixedIds[code] ?: UUID.nameUUIDFromBytes(code.toByteArray())
            organizations[id] = Organization(id, code, name, dept)
        }
    }

    override fun getProducts(search: String?): List<Product> {
        val all = products.values.sortedBy { it.code }
        if (search.isNullOrBlank()) return all
        return all.filter {
            it.code.matchesFuzzy(search) || it.name.matchesFuzzy(search) || it.unit.matchesFuzzy(search)
        }
    }

    override fun getProductById(id: UUID): Product? = products[id]

    override fun getWarehouses(search: String?): List<Warehouse> {
        val all = warehouses.values.sortedBy { it.code }
        if (search.isNullOrBlank()) return all
        return all.filter {
            it.code.matchesFuzzy(search) || it.name.matchesFuzzy(search) || it.location.matchesFuzzy(search)
        }
    }

    override fun getWarehouseById(id: UUID): Warehouse? = warehouses[id]

    override fun getOrganizations(search: String?): List<Organization> {
        val all = organizations.values.sortedBy { it.code }
        if (search.isNullOrBlank()) return all
        return all.filter {
            it.code.matchesFuzzy(search) || it.name.matchesFuzzy(search) || it.department.matchesFuzzy(search)
        }
    }

    override fun getOrganizationById(id: UUID): Organization? = organizations[id]

    private fun String.matchesFuzzy(searchTerm: String?): Boolean {
        if (searchTerm.isNullOrBlank()) return true
        val normalizedTarget = Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace(DIACRITICS_PATTERN.toRegex(), "")
            .replace('đ', 'd').replace('Đ', 'D')
            .lowercase()
        val normalizedSearch = Normalizer.normalize(searchTerm, Normalizer.Form.NFD)
            .replace(DIACRITICS_PATTERN.toRegex(), "")
            .replace('đ', 'd').replace('Đ', 'D')
            .lowercase()
        return normalizedTarget.contains(normalizedSearch)
    }

    companion object {
        private val DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
    }
}
