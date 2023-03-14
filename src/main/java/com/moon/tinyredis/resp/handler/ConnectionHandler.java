package com.moon.tinyredis.resp.handler;

import com.moon.tinyredis.resp.session.Connection;
import com.moon.tinyredis.resp.session.ConnectionHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Chanmoey
 * @date 2023年02月27日
 */
public class ConnectionHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 获取一个redis连接，并将它放入ConnectionHolder中保存
        // 初始化redis长连接
        Connection connection = new Connection(0, ctx.channel());
        ConnectionHolder.put(connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("channelInactive");
        ConnectionHolder.close(ConnectionHolder.get(ctx.channel()));
    }
}
