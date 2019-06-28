package main;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * PetPet app's server
 *
 * @author FangXu
 * @dateStart 2018/06/22
 *
 */
public class PetPetServer implements Runnable{

    /**数据库连接字符串，这里的petdb为数据库名*/
    private static final String URL="jdbc:mysql://localhost:3306/petdb?useSSL=false";
    /**登录名*/
    private static final String NAME="root";
    /**密码*/
    private static final String PASSWORD="123456";

    private PreparedStatement tableAccount;
    private PreparedStatement tableMessage;
    public Connection connection;
    public static List<SocketPack> socketList;

    private ExecutorService socketPool;
    private ServerSocket serverSocket;
    /**默认开启2334号端口*/
    public static final int PORT=2334;



    public static void main(String[] args) {
        PetPetServer server=new PetPetServer();
        new Thread(server).start();
    }

    public PetPetServer(){

        initDatabase();

        socketList=new ArrayList<SocketPack>();
        try {
            serverSocket = new ServerSocket(PORT);
            /**暂时允许100用户并发*/
            socketPool = Executors.newFixedThreadPool(20);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        /**采用轮询机制检查是否有用户连入，考虑到可能比较耗资源，所以创建了server的线程*/
        while (true) {
            createSocket();
        }
    }


    /**创建与客户端通信的线程*/
    public void createSocket(){
        Socket clientsocket=null;
        String idstring=new String("");
        try {
            clientsocket = serverSocket.accept();
            System.out.println("accept a new socket");
            DataInputStream din=new DataInputStream(clientsocket.getInputStream());
            System.out.println("开始等");
            idstring=din.readUTF();
            DataOutputStream dout=new DataOutputStream(clientsocket.getOutputStream());
            dout.writeUTF("OK");
            System.out.println("接到的id为："+idstring);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SocketPack socketPack=new SocketPack(clientsocket,idstring);
        /**将用户socket加入list*/
        socketList.add(socketPack);

        Runnable socketthread = new ServerThread(clientsocket,connection);
        socketPool.submit(socketthread);
    }


    private void initDatabase(){
        /**加载驱动*/
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("未能成功加载驱动程序，请检查是否导入驱动程序！");
            System.out.println("请检查添加驱动字符串是否正确。");
            e.printStackTrace();
        }

        try {
            connection = DriverManager.getConnection(URL, NAME, PASSWORD);
            System.out.println("获取数据库连接成功！");
        } catch (SQLException e) {
            System.out.println("获取数据库连接失败！");
            System.out.println("请检查用户名、密码等信息是否正确");
            e.printStackTrace();
        }

        try {
            String sql_create_account = "create table if not exists accounts(id varchar(20),name varchar(20),password varchar(20),image blob,primary key(id));";
            String sql_create_message = "create table if not exists messages(id varchar(20),image blob,message varchar(300),primary key(id));";

            /**执行数据库创建删除操作得用executeUpdate方法*/
            tableAccount= connection.prepareStatement(sql_create_account);
            tableAccount.executeUpdate();

            tableMessage=connection.prepareStatement(sql_create_message);
            tableMessage.executeUpdate();
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
    }


}



