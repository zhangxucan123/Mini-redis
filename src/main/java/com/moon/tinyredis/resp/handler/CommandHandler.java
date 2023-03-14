package com.moon.tinyredis.resp.handler;

import com.moon.tinyredis.resp.command.CommandLine;
import com.moon.tinyredis.resp.config.SystemConfig;
import com.moon.tinyredis.resp.database.Database;
import com.moon.tinyredis.resp.database.IDatabase;
import com.moon.tinyredis.resp.parser.Message;
import com.moon.tinyredis.resp.parser.PayLoad;
import com.moon.tinyredis.resp.reply.MultiBulkReply;
import com.moon.tinyredis.resp.reply.error.CommonErrorReply;
import com.moon.tinyredis.resp.reply.error.ErrorReply;
import com.moon.tinyredis.resp.session.Connection;
import com.moon.tinyredis.resp.session.ConnectionHolder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 对指令进行转发，如果是正常的指令，则交由Database进行执行处理
 * 如果是错误的指令，则回送错误信息
 *
 * @author Chanmoey
 * @date 2023年02月27日
 */
public class CommandHandler extends SimpleChannelInboundHandler<PayLoad> {
    //创建一个单线程化的线程池，它只会用唯一的工作线程来执行任务，保证所有任务按照指定顺序(FIFO, LIFO, 优先级)执行。
    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();
    private static final IDatabase DATABASE = Database.getInstance();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PayLoad msg) throws Exception {
        if (msg.getException() != null) {
            ErrorReply errorReply = CommonErrorReply.makeCommonErrorReply(msg.getException().getMessage());
            ByteBuf errBuf = Message.makeMessage(errorReply);
            ctx.writeAndFlush(errBuf);
            return;
        }

        // 通过线程池将IO与业务处理进行解耦，并将指令转发给Database进行处理
        if (msg.getData() == null) {
            return;
        }
        // 客户发送的必须是数组的形式
        if (!(msg.getData() instanceof MultiBulkReply)) {
            return;
        }

        EXECUTOR.execute(() -> {
            Connection connection = ConnectionHolder.get(ctx.channel());// 获取 之前得到的redis连接
            MultiBulkReply multiBulkReply = (MultiBulkReply) msg.getData();
            byte[] commandName = multiBulkReply.getArgs()[0];
            byte[][] args = new byte[multiBulkReply.getArgs().length - 1][];
            for (int i = 0; i < args.length; i++) {
                args[i] = multiBulkReply.getArgs()[i + 1];
            }
            CommandLine commandLine = new CommandLine(commandName, args);

            DATABASE.exec(connection, commandLine);
        });
    }
}
