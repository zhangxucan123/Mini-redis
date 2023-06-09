package com.moon.tinyredis.resp;

import com.moon.tinyredis.resp.handler.CommandHandler;
import com.moon.tinyredis.resp.handler.ConnectionHandler;
import com.moon.tinyredis.resp.parser.Parser;
import com.moon.tinyredis.resp.parser.ReadState;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author Chanmoey
 * @date 2023年02月26日
 */
public class TinyRedisServerStarter {

    private final EventLoopGroup mainGroup;
    private final EventLoopGroup subGroup;
    private final ServerBootstrap server;

    public TinyRedisServerStarter() {
        // 用于处理客户端的连接请求
        this.mainGroup = new NioEventLoopGroup(1);
        // 用于处理与各个客户端连接的 IO 操做，线程数量默认为CUP数量*2
        this.subGroup = new NioEventLoopGroup();
        this.server = new ServerBootstrap();
        server.group(mainGroup, subGroup)
                .channel(NioServerSocketChannel.class)
                // 服务端可连接队列大小
                .option(ChannelOption.SO_BACKLOG, 10240)
                // 参数表示允许重复使用本地地址和端口
                .option(ChannelOption.SO_REUSEADDR, true)
                // 是否禁用Nagle算法 简单点说是否批量发送数据 true关闭 false开启
                // 开启的话可以减少一定的网络开销，但影响消息实时性
                .childOption(ChannelOption.TCP_NODELAY, true)
                // 保活开关2h没有数据服务端会发送心跳包
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                // channelHandler是消息的具体处理器，主要负责处理客户端/服务端接收和发送的数据。
                // 一个channel包含一个channelPipeLine,一个channelPipeLine可以包含多个channelHandler。
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 用于将服务端的处理结果进行解码
                        ch.pipeline().addLast(new Parser(new ReadState()));
                        // 连接器，
                        ch.pipeline().addLast(new ConnectionHandler());
                        // 指令中转器
                        ch.pipeline().addLast(new CommandHandler());
                    }
                });
    }

    public void start() {
        this.server.bind(9736);
    }

    public static void main(String[] args) {
        TinyRedisServerStarter starter = new TinyRedisServerStarter();
        System.out.println("Mini-Redis Server Start");
        starter.start();
    }
}