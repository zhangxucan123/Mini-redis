package com.moon.tinyredis.resp.reply;

import com.moon.tinyredis.resp.config.SystemConfig;
import com.moon.tinyredis.resp.reply.constant.EmptyBulkReply;
import com.moon.tinyredis.resp.reply.constant.NullBulkReply;

/**
 * @author Chanmoey
 * @date 2023年02月20日
 */
public class BulkReply implements Reply {

    private final byte[] arg;

    public BulkReply(byte[] arg) {
        this.arg = arg;
    }

    @Override
    public byte[] toBytes() {

        if (arg == null) {
            return NullBulkReply.makeNullBulkReply().toBytes();
        }

        if (arg.length == 0) {
            return EmptyBulkReply.makeEmptyMultiBuckReply().toBytes();
        }
        // 使用的Bulk String  目的是了保证二进制安全
        String s = "$" + arg.length + RespConstant.CRLF + new String(arg) + RespConstant.CRLF;
        return s.getBytes(SystemConfig.SYSTEM_CHARSET);
    }

    public static BulkReply makeBulkReply(byte[] arg) {
        return new BulkReply(arg);
    }
}
