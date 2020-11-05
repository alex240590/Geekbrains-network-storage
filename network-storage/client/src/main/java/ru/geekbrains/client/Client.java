package ru.geekbrains.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import ru.geekbrains.common.SendFileClass;
import ru.geekbrains.common.MessagesClass;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;


public class Client {

    public void connect() throws InterruptedException {
        CountDownLatch networkInitializer = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Network.getInstance().start(networkInitializer);
            }
        }).start();
        networkInitializer.await();

    }


    public void sendFileClient(Path filePath) throws IOException {
        SendFileClass.sendFile(filePath, Network.getInstance().getCurrentChannel(), null, new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    future.cause().printStackTrace();
                }
                if (future.isSuccess()) {
                    System.out.println("Файл отправлен");

                }
            }
        });
    }

    public void authorisationClient(String login, String pass){

        ByteBuf byteBuf = null;

        byte[] messageBytes = MessagesClass.MessageType.AUTHORISATION.toString().getBytes(StandardCharsets.UTF_8);
        byteBuf = ByteBufAllocator.DEFAULT.directBuffer(messageBytes.length);
        byteBuf.writeBytes(messageBytes);
        send(byteBuf);

        sendString(login);

        sendString(pass);
    }


    private static void sendString(String string){
        ByteBuf byteBuf = null;

        byte[] messageBytes = string.getBytes(StandardCharsets.UTF_8);
        byteBuf = ByteBufAllocator.DEFAULT.directBuffer(4);
        byteBuf.writeInt(messageBytes.length);
        send(byteBuf);

        byteBuf = ByteBufAllocator.DEFAULT.directBuffer(messageBytes.length);
        byteBuf.writeBytes(messageBytes);
        send(byteBuf);
    }

    private static void send(ByteBuf byteBuf){
        Channel channel = Network.getInstance().getCurrentChannel();
        channel.writeAndFlush(byteBuf);
    }
}
