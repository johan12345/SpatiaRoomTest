package com.example.spatiaroomtest

import androidx.room.TypeConverter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PointTypeConverters {
    @TypeConverter
    fun toLatLng(bytes: ByteArray): LatLng {
        val byteOrder = if (bytes[1] == 1.toByte()) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN
        val buffer = ByteBuffer.wrap(bytes).order(byteOrder)

        assert(buffer.getInt(39) == 1)   // is point
        assert(buffer.getInt(2) == 4326) // is WGS84

        return LatLng(buffer.getDouble(51), buffer.getDouble(43))
    }

    @TypeConverter
    fun fromLatLng(point : LatLng): ByteArray {
        val buffer = ByteBuffer.allocate(60).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(0x00)  // start
            .put(0x01)  // byte order
            .putInt(4326)  // WGS84
            .putDouble(point.lng)  // min x
            .putDouble(point.lat)  // min y
            .putDouble(point.lng)  // max x
            .putDouble(point.lat)  // max y
            .put(0x7c)  // mbr_end
            .putInt(1) // point
            .putDouble(point.lng)  // x
            .putDouble(point.lat)  // y
            .put((0xfe).toByte())  // end
        return buffer.array()
    }
}