package com.tizz.signin.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketUtils {
    private Socket socket;
    public static final String ip="192.168.43.253";
    //public static final String ip="10.14.125.217";
    public static int port=6000;
    private boolean isConnected;
    private boolean isRegistered;
    private BufferedReader in;
    private PrintWriter out;


    public boolean isConnected(){
        return isConnected;
    }

    public boolean isRegistered(){
        return isRegistered;
    }

    public void connectServer() throws Exception{
        try{
            socket=new Socket(ip,port);
            socket.setSoTimeout(5*1000);
            in=new BufferedReader(new InputStreamReader(
                    socket.getInputStream()
            ));
            out=new PrintWriter(socket.getOutputStream());
            isConnected=true;
        }catch (Exception e){
            e.printStackTrace();
            isConnected=false;
            throw e;
        }finally {
            socket.close();
        }
    }

    public void stuRegister(String name,String num,String major,String psw){
        try{
            out.flush();
            String result=in.readLine();
            if(result.equals("REGISTERSUCCED")){
                isRegistered=true;
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void teacherRegister(String name,String psw){
        try{
            out.flush();
            String result=in.readLine();
            if(result.equals("REGISTERSUCCED")){
                isRegistered=true;
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

}
