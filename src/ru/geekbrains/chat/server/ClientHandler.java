package ru.geekbrains.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private String nick;
    List<String> blackList;
    private boolean flagAuthorization=false;


    public String getNick() {
        return nick;
    }



    public ClientHandler(Server server, Socket socket) {


        try {
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.blackList = new ArrayList<>();
            socket.isClosed();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        long endTimeMillis = System.currentTimeMillis() + 10;
                        int n=1;
                        while (true) {
                            n++;
                            System.out.printf("time" + n);
                            if (n < 100) {
                                if (System.currentTimeMillis() == endTimeMillis & flagAuthorization == false){
                                    System.out.println("Отключаем");
////                            String str = in.readUTF();
////                            textArea.appendText(str + "\n");
////                            if(str.equals("/serverClose")) {
////                                break;
                                }
                            }else {
                            System.out.println("Мы тут");

                            }
                        }
                    } catch  (Exception e) {
                        e.printStackTrace();
                    }
//                        e.printStackTrace();
//                    }
                    }

//                        e.printStackTrace();

                }).start();

            new Thread(() -> {
                try {
//                    long endTimeMillis = System.currentTimeMillis() + 10000;
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/auth")) { // /auth login72 pass72
                            String[] tokens = str.split(" ");
                            String newNick = AuthService.getNickByLoginAndPass(tokens[1], tokens[2]);
                            if (newNick != null) {
                                if(!server.isNickBusy(newNick)) {
                                    sendMsg("/authok");
                                    nick = newNick;
                                    server.subscribe(this);
                                    flagAuthorization=true;
                                    break;
                                } else {

                                    timemethod();
                                    sendMsg("Учетная запись уже используется");
                                }
                            } else {
                                sendMsg("Неверный логин/пароль");
                            }
                        }
//                        if (System.currentTimeMillis() > endTimeMillis) {
//                            System.out.println("1");
//                            //break;
//                        }
                    }
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                out.writeUTF("/serverclosed");
                                break;
                            }
                            if (str.startsWith("/w ")) { // /w nick3 lsdfhldf sdkfjhsdf wkerhwr
                                String[] tokens = str.split(" ", 3);
                                String m = str.substring(tokens[1].length() + 4); // как сделать через substring
                                server.sendPersonalMsg(this, tokens[1], tokens[2]);
                            }
                            if (str.startsWith("/blacklist ")) { // /blacklist nick3
                                String[] tokens = str.split(" ");
                                blackList.add(tokens[1]);
                                sendMsg("Вы добавили пользователя " + tokens[1] + " в черный список");
                            }
                        } else {
                            server.broadcastMsg(this, nick + ": " + str);
                        }
                        System.out.println("Client: " + str);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    server.unsubscribe(this);
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean checkBlackList(String nick) {
        return blackList.contains(nick);
    }

    public void timemethod() {
        long endTimeMillis = System.currentTimeMillis() + 10000;
        while (true) {
            // method logic
            System.out.println("3");
            if (System.currentTimeMillis() > endTimeMillis) {
                // do some clean-up
                System.out.println("4");
                return;
            }
        }
    }
}
