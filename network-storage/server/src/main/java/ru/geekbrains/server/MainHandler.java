package ru.geekbrains.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import ru.geekbrains.common.MessagesClass;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainHandler extends ChannelInboundHandlerAdapter {

    private enum State {
        INIT,
        NAME_LENGTH, NAME, FILE_LENGTH, FILE,
        LOGIN_LENGTH, LOGIN, PASSWORD_LENGTH, PASSWORD
    }

    private State currentState = State.INIT;
    private int fileNameLength;
    private long fileLength;
    private long inFileLength;
    private int loginLength;
    private int passwordLength;
    private String login;
    private String password;

    private BufferedOutputStream fileSaveStream;
    private byte[] messageBytes = new byte[MessagesClass.MESSAGE_LENGTH];
    private String messageString = "";

    @Override
    public void channelRead (ChannelHandlerContext context, Object message) throws IOException {
        ByteBuf byteBuf = ((ByteBuf) message);
        currentState = State.INIT;

        while (byteBuf.readableBytes() > 0) {

            if (currentState == State.INIT){
                byteBuf.readBytes(messageBytes);
                messageString = new String(messageBytes, "UTF-8");
            }

            if (currentState == State.INIT & messageString.equals("AUTHORISATION")){
                currentState = State.LOGIN_LENGTH;
                inFileLength = 0L;
                System.out.println("STATE: Start authorisation receiving");
            }
            else if (currentState == State.LOGIN_LENGTH) {
                if (byteBuf.readableBytes() >= 4) {
                    System.out.println("STATE: Get login length");
                    loginLength = byteBuf.readInt();
                    currentState = State.LOGIN;
                }
            } else if (currentState == State.LOGIN) {
                if (byteBuf.readableBytes() >= loginLength) {
                    System.out.print("STATE: Get login: ");
                    byte[] loginBytes = new byte[loginLength];
                    byteBuf.readBytes(loginBytes);
                    login = new String(loginBytes, "UTF-8");
                    System.out.println(login);
                    currentState = State.PASSWORD_LENGTH;
                }
            } else if (currentState == State.PASSWORD_LENGTH) {
                if (byteBuf.readableBytes() >= 4) {
                    System.out.println("STATE: Get pass length");
                    passwordLength = byteBuf.readInt();
                    currentState = State.PASSWORD;
                }
            } else if (currentState == State.PASSWORD) {
                if (byteBuf.readableBytes() >= passwordLength) {
                    System.out.print("STATE: Get pass: ");
                    byte[] passBytes = new byte[passwordLength];
                    byteBuf.readBytes(passBytes);
                    password = new String(passBytes, "UTF-8");
                    System.out.println(password);
                    currentState = State.INIT;
                }
            }

            if (currentState == State.INIT & messageString.equals("FILE")) {
                byteBuf.readBytes(messageBytes);
                messageString = new String(messageBytes, "UTF-8");
                currentState = State.NAME_LENGTH;
                inFileLength = 0L;
                System.out.println("STATE: Start file receiving");
            }

            if (currentState == State.NAME_LENGTH) {
                if (byteBuf.readableBytes() >= 4) {
                    System.out.println("STATE: Get filename length");
                    fileNameLength = byteBuf.readInt();
                    currentState = State.NAME;
                }
            }

            if (currentState == State.NAME) {
                if (byteBuf.readableBytes() >= fileNameLength) {
                    byte[] fileName = new byte[fileNameLength];
                    byteBuf.readBytes(fileName);
                    System.out.println("STATE: Filename received - _" + new String(fileName, "UTF-8"));
                    fileSaveStream = new BufferedOutputStream
                            (new FileOutputStream("_" + new String(fileName)));
                    currentState = State.FILE_LENGTH;
                }
            }

            if (currentState == State.FILE_LENGTH) {
                if (byteBuf.readableBytes() >= 8) {
                    fileLength = byteBuf.readLong();
                    System.out.println("STATE: File length received - " + fileLength);
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
                        fileSaveStream.close();
                        break;
                    }

                }
            }
            if (byteBuf.readableBytes() == 0) {
                byteBuf.release();
            }
        }
        context.writeAndFlush("Example.txt");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable exception) throws Exception {
        exception.printStackTrace();
        context.close();
    }

}
