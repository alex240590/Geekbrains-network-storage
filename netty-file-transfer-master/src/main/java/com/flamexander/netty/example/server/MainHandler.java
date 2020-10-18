package com.flamexander.netty.example.server;

import com.flamexander.netty.example.common.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class MainHandler extends ChannelInboundHandlerAdapter {

    static HashMap<Integer, Types> typesHashMap;
    static {
        typesHashMap.put(1,Types.FILE);
        typesHashMap.put(2,Types.AUTH);
        typesHashMap.put(3,Types.FILE_LIST);
        typesHashMap.put(4,Types.COMMAND);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FileRequest) {
            FileRequest fr = (FileRequest) msg;
            if (Files.exists(Paths.get("server_storage/" + fr.getFilename()))) {
                FileMessage fm = new FileMessage(Paths.get("server_storage/" + fr.getFilename()));
                ctx.writeAndFlush(fm);
            }
        }




        else if (msg instanceof FileDelete) {
            FileDelete fileDelete = (FileDelete) msg;
            try {
                Files.deleteIfExists(Paths.get("server_storage/" + fileDelete.getFilename()));
            }
            catch (NoSuchFileException e) {
                System.out.println("No such file/directory exists");
            }
            System.out.println("Deleted successfully");
        }

        else if (msg instanceof FileRename) {
            FileRename fileRename = (FileRename) msg;
            try {
                File fileAfter = new File("server_storage/" + fileRename.getFileNameAfter());
                Files.copy("server_storage/" + fileRename.getFileNameBefore(),"server_storage/" + fileRename.getFileNameAfter(),REPLACE_EXISTING);
                Files.deleteIfExists(Paths.get("server_storage/" + fileRename.getFileNameBefore()));
            }
            catch (NoSuchFileException e) {
                System.out.println("No such file/directory exists");
            }
            System.out.println("Renamed successfully");
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
