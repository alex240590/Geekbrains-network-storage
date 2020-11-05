package ru.geekbrains.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import ru.geekbrains.common.SendFileClass;

import java.nio.file.Paths;

public class OutHandler extends ChannelOutboundHandlerAdapter {
    private Channel currentChannel;

    public void setChannel(Channel s){
        currentChannel = s;
    };

    @Override
    public void write(ChannelHandlerContext context, Object message, ChannelPromise promise) throws Exception {
        String path = (String)message;

        String string = (String)message;
        byte[] array = string.getBytes();
        ByteBuf byteBuf = context.alloc().buffer(array.length);
        byteBuf.writeBytes(array);
        context.writeAndFlush(byteBuf);
        String file;
        file = message.toString();

       SendFileClass.sendFile(Paths.get(file), null, context, new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    future.cause().printStackTrace();
                }
                if (future.isSuccess()) {
                    System.out.println("Файл успешно передан клиенту");

                }
            }
        });

    }



}
