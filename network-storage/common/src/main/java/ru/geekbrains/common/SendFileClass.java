package ru.geekbrains.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class SendFileClass {
    public static void sendFile(Path path, Channel channel, ChannelHandlerContext context,ChannelFutureListener finishListener) throws IOException {
        FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));


        ByteBuf byteBuf = null;
        byte[] messageBytes = MessagesClass.MessageType.FILE.toString().getBytes(StandardCharsets.UTF_8);
        byteBuf = ByteBufAllocator.DEFAULT.directBuffer(messageBytes.length);
        byteBuf.writeBytes(messageBytes);
        send(channel, context, byteBuf);

        byte[] filenameBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);
        byteBuf = ByteBufAllocator.DEFAULT.directBuffer(4);
        byteBuf.writeInt(filenameBytes.length);
        send(channel, context, byteBuf);

        byteBuf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);
        byteBuf.writeBytes(filenameBytes);
        send(channel, context, byteBuf);
        channel.writeAndFlush(byteBuf);

        byteBuf = ByteBufAllocator.DEFAULT.directBuffer(8);
        byteBuf.writeLong(Files.size(path));
        send(channel, context, byteBuf);
        channel.writeAndFlush(byteBuf);


        if (channel == null & context != null){
            ChannelFuture transferOperationFuture = context.writeAndFlush(region);
            if (finishListener != null) {
                transferOperationFuture.addListener(finishListener);
            }
        }else if (channel != null & context == null){
            ChannelFuture transferOperationFuture = channel.writeAndFlush(region);
            if (finishListener != null) {
                transferOperationFuture.addListener(finishListener);
            }
        }
    }


    private static void send(Channel channel, ChannelHandlerContext context,  ByteBuf byteBuf){
        if (channel == null & context != null){
            context.writeAndFlush(byteBuf);
        }else if (channel != null & context == null){
            channel.writeAndFlush(byteBuf);
        }
    }
}
