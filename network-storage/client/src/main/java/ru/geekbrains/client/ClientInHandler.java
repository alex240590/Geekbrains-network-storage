package ru.geekbrains.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.geekbrains.common.MessagesClass;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ClientInHandler extends ChannelInboundHandlerAdapter {

    public enum State {
        INIT, NAME_LENGTH, NAME, FILE_LENGTH, FILE
    }

    private State currentState = State.INIT;
    private int fileNameLength;
    private long fileLength;
    private long inFileLength;
    private BufferedOutputStream fileSaveStream;
    private byte[] messageBytes = new byte[MessagesClass.MESSAGE_LENGTH];
    private String messageString = "";
    private final String fileFolder = "storage";

    private static final Logger logger = LogManager.getLogger(Client.class);

    @Override
    public void channelRead(ChannelHandlerContext context, Object message) throws IOException {
        ByteBuf byteBuf = ((ByteBuf) message);

        while (byteBuf.readableBytes() > 0) {

            if (currentState == State.INIT) {
                byteBuf.readBytes(messageBytes);
                messageString = new String(messageBytes, "UTF-8");
            }

            if (currentState == State.INIT & messageString.equals("FILE")) {
                currentState = State.NAME_LENGTH;
                inFileLength = 0L;
                System.out.println("STATE: Start file receiving");
                logger.info("STATE: Start file receiving");
            }

            if (currentState == State.NAME_LENGTH) {
                if (byteBuf.readableBytes() >= 4) {
                    System.out.println("STATE: Get filename length");
                    logger.info("STATE: Get filename length");
                    fileNameLength = byteBuf.readInt();
                    currentState = State.NAME;
                }
            }

            if (currentState == State.NAME) {
                if (byteBuf.readableBytes() >= fileNameLength) {
                    byte[] fileName = new byte[fileNameLength];
                    byteBuf.readBytes(fileName);
                    System.out.println("STATE: Filename received - __" + new String(fileName, "UTF-8"));
                    logger.info("STATE: Filename received - __" + new String(fileName, "UTF-8"));
                    fileSaveStream = new BufferedOutputStream
                            (new FileOutputStream("__" + new String(fileName)));
                    currentState = State.FILE_LENGTH;
                }
            }

            if (currentState == State.FILE_LENGTH) {
                if (byteBuf.readableBytes() >= 8) {
                    fileLength = byteBuf.readLong();
                    System.out.println("STATE: File length received - " + fileLength);
                    logger.info("STATE: File length received - " + fileLength);
                    currentState = State.FILE;
                }
            }

            if (currentState == State.FILE) {
                while (byteBuf.readableBytes() > 0) {
                    fileSaveStream.write(byteBuf.readByte());
                    inFileLength++;
                    if (fileLength == inFileLength) {
                        currentState = State.INIT;
                        System.out.println("File received and saved");
                        logger.info("File received and saved");
                        fileSaveStream.close();
                        break;
                    }
                }
            }
        }
        if (byteBuf.readableBytes() == 0) {
            byteBuf.release();
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable exception) throws Exception {
        exception.printStackTrace();
        logger.error(exception.getMessage());
        context.close();
    }

}
