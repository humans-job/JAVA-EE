package org.example.army.militarycommon.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ByteOrderValues;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MySQL 专用 Geometry 类型处理器 (终极修正版)
 * 1. 动态读取 Geometry 对象的 SRID
 * 2. 强制使用 Little-Endian 字节序
 */
public class MysqlGeometryTypeHandler extends BaseTypeHandler<Geometry> {

    // 读取时默认使用的 Factory，SRID 会被读取到的数据覆盖，这里的 0 只是初始值
    private final WKBReader wkbReader = new WKBReader(new GeometryFactory(new PrecisionModel(), 0));

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Geometry parameter, JdbcType jdbcType) throws SQLException {
        byte[] geometryBytes = toMySqlBytes(parameter);
        ps.setBytes(i, geometryBytes);
    }

    @Override
    public Geometry getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return fromMySqlBytes(rs.getBytes(columnName));
    }

    @Override
    public Geometry getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return fromMySqlBytes(rs.getBytes(columnIndex));
    }

    @Override
    public Geometry getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return fromMySqlBytes(cs.getBytes(columnIndex));
    }

    /**
     * 转换为 MySQL 内部格式：SRID(4字节) + WKB
     */
    private byte[] toMySqlBytes(Geometry geometry) {
        if (geometry == null) {
            return null;
        }

        // 1. 获取 SRID
        // 如果 Geometry 对象本身没有设置 SRID (即为 0)，且你的数据库是 4326，
        // 你可能需要在这里手动兜底： int srid = geometry.getSRID() == 0 ? 4326 : geometry.getSRID();
        int srid = geometry.getSRID();

        // 2. 初始化 WKBWriter
        // 参数1：维度 (2=2D, 3=3D)。通常数据库存2D即可，如果需要3D改为3。
        // 参数2：字节序。MySQL 推荐 LittleEndian。
        // 参数3：是否包含 SRID。MySQL 内部格式要求 WKB 部分纯净（false），SRID 由头部 4 字节提供。
        WKBWriter wkbWriter = new WKBWriter(2, ByteOrderValues.LITTLE_ENDIAN, false);
        byte[] wkb = wkbWriter.write(geometry);

        // 3. 拼接：SRID (Little Endian) + WKB
        ByteBuffer buffer = ByteBuffer.allocate(4 + wkb.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN); // 必须是 Little Endian
        buffer.putInt(srid);
        buffer.put(wkb);

        return buffer.array();
    }

    private Geometry fromMySqlBytes(byte[] bytes) {
        if (bytes == null || bytes.length < 5) {
            return null;
        }

        // 读取前 4 字节作为 SRID
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int srid = buffer.getInt();

        // 剩下的字节是 WKB
        byte[] wkb = new byte[bytes.length - 4];
        System.arraycopy(bytes, 4, wkb, 0, wkb.length);

        try {
            // 使用读取到的 SRID 创建对应的 Factory 解析数据
            GeometryFactory factory = new GeometryFactory(new PrecisionModel(), srid);
            WKBReader reader = new WKBReader(factory);
            return reader.read(wkb);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

