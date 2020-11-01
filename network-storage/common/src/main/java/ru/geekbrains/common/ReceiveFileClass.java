package ru.geekbrains.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ReceiveFileClass {

    public enum State {
        INIT, NAME_LENGTH, NAME, FILE_LENGTH, FILE
    }

    private State currentState = State.INIT;
    private int newFileLength;
    private long fileLength;
    private long inFileLength;
    private BufferedOutputStream fileSaveStream;

    public void receiveFile(ChannelHandlerContext context, Object message) throws IOException {

        ByteBuf byteBuf = ((ByteBuf) message);

        while(byteBuf.readableBytes()>0) {

                    if (currentState == State.INIT) {
                        byte read = byteBuf.readByte();
                        if (read == (byte) 25) {
                            currentState = State.NAME_LENGTH;
                            inFileLength = 0L;
                            System.out.println("STATE: Start file receiving");
                        } else {
                            System.out.println("ERROR: Invalid first byte - " + read);
                        }
                    }

                    if (currentState == State.NAME_LENGTH) {
                        if (byteBuf.readableBytes() >= 4) {
                            System.out.println("STATE: Get filename length");
                            newFileLength = byteBuf.readInt();
                            currentState = State.NAME;
                        }
                    }

                    if (currentState == State.NAME) {
                        if (byteBuf.readableBytes() >= newFileLength) {
                            byte[] fileName = new byte[newFileLength];
                            byteBuf.readBytes(fileName);
                            System.out.println("STATE: Filename received - _" + new String(fileName, "UTF-8"));
                            fileSaveStream = new BufferedOutputStream(new FileOutputStream("_" + new String(fileName)));
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
                }
    }
}
