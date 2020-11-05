package ru.geekbrains.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

public class Network {

    private Network() {
    }

    private static final Network network = new Network();

    private static final Logger logger = LogManager.getLogger(Client.class);

    public static Network getInstance() {
        return network;
    }

    private Channel channel;

    public Channel getCurrentChannel() {
        return channel;
    }

    public void start(CountDownLatch countDownLatch) {
        EventLoopGroup clientGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();

            bootstrap.group(clientGroup)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress("localhost", 8190))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ClientInHandler());
                            channel = socketChannel;
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect().sync();
            countDownLatch.countDown();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                clientGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                logger.error("Connection closing failure - " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        channel.close();
    }
}

