package com.moon.tinyredis.resp.client;

import com.moon.tinyredis.resp.parser.Parser;
import com.moon.tinyredis.resp.parser.ReadState;
import com.moon.tinyredis.resp.reply.MultiBulkReply;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Arrays;
import java.util.Scanner;

/**
 * 目前完成了PING、DEL、FLUSHDB、KEYS、SET、GET、SETNX等常用指令。
 * @author Chanmoey
 * @date 2023年02月27日
 */
public class TinyRedisClient {
    public static void main(String[] args) throws Exception {

        String host = "localhost";
        int port = 9736;
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            //实现长连接
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    // 用于解码response 没有request编码
                    ch.pipeline().addLast(new Parser(new ReadState()));
                    ch.pipeline().addLast(new TinyClientHandler());
                }
            });

            // Start the client.
            Channel channel = b.connect(host, port).sync().channel();

            while (true) {
                Scanner input = new Scanner(System.in);
                String str = input.nextLine();
                // 获取输入的指令并将其转化为字符串数组
                String[] strings = str.split(" ");
                byte[][] bytes = new byte[strings.length][];
                // 字符串数组 ----->  字节数组
                for (int i = 0; i < strings.length; i++) {
                    bytes[i] = strings[i].getBytes();
                }
                // 这里是对输入的命令转化为 bulkstring 的格式传输。
                byte[] resp = MultiBulkReply.makeMultiBulkReply(bytes).toBytes();
                ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(resp.length + 4);
                // int 占4个字节
                byteBuf.writeInt(resp.length);
                byteBuf.writeBytes(resp);
                // 数据传输
                channel.writeAndFlush(byteBuf);
            }
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
