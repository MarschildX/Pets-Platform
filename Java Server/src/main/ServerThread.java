package main;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ServerThread implements Runnable {
    private Socket socket;
    private Connection connection;
    private BufferedOutputStream bout;
    private BufferedInputStream bin;
    private List<SocketPack> socketList;
    private DataOutputStream dout;
    private DataInputStream din;

    private static final String PET_MESSAGE="pet message";
    private static final String ASK_PET_MESSAGE="ask pet message";
    private static final String ASK_FRIEND_MESSAGE="ask friend message";
    private static final String CHAT_MESSAGE="chat message";

    /**这两个暂时是固定的，固定传给客户端的数据数量*/
    private int sendPetDataNum=20;
    private int sendFriendDataNum=20;


    @Override
    public void run(){
        while(true){
            receive();
        }
    }


    public ServerThread(Socket socket,Connection connection) {
        this.socket = socket;
        this.connection = connection;
        try {
            dout=new DataOutputStream(socket.getOutputStream());
            din=new DataInputStream(socket.getInputStream());
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }

    private void receive(){
        try {
            String dataheadString = din.readUTF();
            sendOK();
            if (dataheadString.startsWith(PET_MESSAGE)) {
                receivePetMessage(dataheadString);
            } else if (dataheadString.startsWith(CHAT_MESSAGE)) {
                dealChatting(dataheadString);
            } else if (dataheadString.startsWith(ASK_PET_MESSAGE)) {
                sendPetMessage(dataheadString);
            } else if (dataheadString.startsWith(ASK_FRIEND_MESSAGE)) {
                sendFriendMessage(dataheadString);
            }
        }
        catch (IOException ex){
            ex.printStackTrace();
            try {
                socket.close();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void sendPetMessage(String dataheadString){
        int start,end;
        start=dataheadString.indexOf("from ");
        start+="from ".length();
        String idFrom=dataheadString.substring(start);
        try {
            String select = "select * from messages LIMIT 20";
            PreparedStatement ps=connection.prepareStatement(select);
            ResultSet rs =ps.executeQuery();
            String numstring=String.valueOf(sendPetDataNum);
            try {
                dout.writeUTF(numstring);
                dout.flush();
                receiveOK();
                System.out.println("发送宠物信息数量");
                for(int i=0;i<sendPetDataNum;i++) {
                    rs.next();
                    byte[] imagesource=rs.getBytes("image");
                    sendImageUsual(dout,bout,imagesource);
                    System.out.println("yifasong tupian ");
                    String messageString=rs.getString("message");
                    sendMessageUsual(dout,messageString);
                    System.out.println("发送+1：");
                }
            }
            catch (IOException ex){
                ex.printStackTrace();
            }
        }
        catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    private void sendFriendMessage(String dataheadString){
        try {
            String select = "select * from accounts LIMIT 20";
            PreparedStatement ps=connection.prepareStatement(select);
            ResultSet rs =ps.executeQuery();
            String numstring=String.valueOf(sendFriendDataNum);
            System.out.println("用户数量为 ："+numstring);
            try {
                dout.writeUTF(numstring);
                dout.flush();
                receiveOK();
                System.out.println("开始发送用户信息：");
                for(int i=0;i<sendFriendDataNum;i++) {
                    rs.next();
                    String name=rs.getString("name");
                    String id=rs.getString("id");
                    String dataforsend="name:"+name+"\r\n"+"id:"+id;
                    dout.writeUTF(dataforsend);
                    dout.flush();
                    receiveOK();

                    byte[] imagebyte=rs.getBytes("image");
                    sendImageUsual(dout,bout,imagebyte);
                    System.out.println("发送用户数据+1");
                }
            }
            catch (IOException ex){
                ex.printStackTrace();
            }
        }
        catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    private void dealChatting(String dataheadString){
        int start,end;
        String data=new String("");
        String fromwhoid;
        String towhoid;
        try {
            data=din.readUTF();
            sendOK();
        }catch (IOException ex){
            ex.printStackTrace();
        }
        start="from:".length();
        end=data.indexOf("\r\n");
        fromwhoid=data.substring(start,end);
        start=end+"\r\n".length();
        data=data.substring(start);
        start="to:".length();
        end=data.indexOf("\r\n");
        towhoid=data.substring(start,end);
        start=end+"\r\n".length();
        data=data.substring(start);
        socketList=PetPetServer.socketList;
        for(int i=0;i<socketList.size();i++){
            if(socketList.get(i).getId().equals(towhoid)){
                try {
                    DataOutputStream tempdout=new DataOutputStream(socket.getOutputStream());
                    String datahead = CHAT_MESSAGE;
                    tempdout.writeUTF(datahead);
                    tempdout.flush();
                    receiveOK();
                    data = "from " + fromwhoid + "\r\n" + data;
                    tempdout.writeUTF(data);
                    tempdout.flush();
                    receiveOK();
                }
                catch(IOException ex){
                    ex.printStackTrace();
                }
                break;
            }
        }
    }

    private void receivePetMessage(String dataheadString) {
        //messageString放着文本数据
        String messageString = new String("");
        int start, end;
        byte[] imageByte2 = null;
        start = dataheadString.indexOf("from ");
        start += "from ".length();
        String idFrom = dataheadString.substring(start);
        imageByte2 = acceptImageUsual();
        messageString = acceptMessageUsual();

        /**插入数据到数据库*/
        try {
            String insert = "insert into messages(id,image,message) value(?,?,?)";
            PreparedStatement ps = connection.prepareStatement(insert);
            ps.setString(1, idFrom);
            ps.setBytes(2, imageByte2);
            ps.setString(3, messageString);
            int result = ps.executeUpdate();
            if (result != 1) {
                System.out.println("数据写入数据库失败");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void sendImageUsual(DataOutputStream dout,BufferedOutputStream bout,byte[] imagebyte){
        try {
            int length=imagebyte.length;
            dout.writeUTF(String.valueOf(length));
            dout.flush();
            System.out.println("dnegdai changdu queren"+"长度为 ："+length);
            receiveOK();
            System.out.println("changdu quren ");
            dout.write(imagebyte,0,length);
            dout.flush();
            receiveOK();
            System.out.println("已发送1组");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageUsual(DataOutputStream dout,String messageString){
        try {
            dout.writeUTF(messageString);
            dout.flush();
            receiveOK();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] acceptImageUsual(){
        byte[] imageByte=null;
        try {
            String lengthString=din.readUTF();
            sendOK();
            int length=Integer.parseInt(lengthString);
            imageByte=new byte[length];
            int real_length_get=0;
            int start=real_length_get;
            while(real_length_get<length) {
                int temp_length=din.read(imageByte, start, length-real_length_get);
                real_length_get+=temp_length;
                start=real_length_get;
            }
            sendOK();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageByte;
    }

    private String acceptMessageUsual(){
        String messageString=null;
        try{
            messageString=din.readUTF();
            sendOK();
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
        return messageString;
    }

    private void sendOK(){
        try{
            dout.writeUTF("OK");
            dout.flush();
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }

    private void receiveOK(){
        try{
            String temp=din.readUTF();
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
    }

}
