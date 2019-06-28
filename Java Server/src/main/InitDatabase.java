package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InitDatabase {

    /**数据库连接字符串，这里的petdb为数据库名*/
    private static final String URL="jdbc:mysql://localhost:3306/petdb?useSSL=false";
    /**登录名*/
    private static final String NAME="root";
    /**密码*/
    private static final String PASSWORD="123456";

    private PreparedStatement tableAccount;
    private PreparedStatement tableMessage;
    public Connection connection;

    /*public static void main(String[] args){
        InitDatabase init=new InitDatabase();
        init.pullAccountData();
    }*/

    public InitDatabase(){
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
            String sql_create_account = "create table if not exists accounts(id varchar(20),name varchar(20),password varchar(20),image longblob,primary key(id));";
            String sql_create_message = "create table if not exists messages(id varchar(20),image longblob,message varchar(300));";

            /**执行数据库创建删除操作得用executeUpdate方法*/
            tableAccount= connection.prepareStatement(sql_create_account);
            tableAccount.executeUpdate();

            tableMessage=connection.prepareStatement(sql_create_message);
            tableMessage.executeUpdate();
        }
        catch (SQLException ex){
            ex.printStackTrace();
            System.out.println("建表失败");
        }
    }

    private void pullPetMessage() {


        try {
            String sql_insert="insert into messages(id,image,message) value(?,?,?)";
            String petmessage="愚蠢的人类啊，快点领养我吧";

            try {
                for(int i=17;i<=21;i++) {
                    String filePath = "D://tempPicture//";
                    filePath += "petimage";
                    filePath+=String.valueOf(i)+".jpeg";
                    File file = new File(filePath);
                    FileInputStream fin = new FileInputStream(file);
                    PreparedStatement ps = connection.prepareStatement(sql_insert);
                    ps.setString(1, "123456");
                    ps.setBinaryStream(2, fin, fin.available());
                    ps.setString(3, petmessage);
                    ps.executeUpdate();
                    fin.close();
                }
            }
            catch (SQLException ex){
                ex.printStackTrace();
            }
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }


    private void pullAccountData(){
        String[] names={"最单纯的乌龟",
                "伊面",
                "您的好友蓝忘机已上羡",
                "魔",
                "青柠芒果",
                "蛮可爱",
                "最笨的告白",
                "喪",
                "后来的我们",
                "童话",
                "似梦非梦",
                "谈情不如逗狗",
                "高冷爸爸",
                "南城旧梦",
                "别理我",
                "诺曦",
                "慈悲佛祖",
                "一枫情书",
                "尹雨沫",
                "呆橘",
                "困倦",
                "二货你真萌"};
        for(int i=16;i<21;i++){
            int idint=123456+i;
            String id=String.valueOf(idint);
            String name=names[i];
            String password="666666";
            String filepath="D://tempimage//";
            filepath+="image"+String.valueOf(i+1)+".jpg";
            try {
                FileInputStream fin = new FileInputStream(filepath);
                String insert = "insert into accounts(id,name,password,image) value(?,?,?,?)";

                PreparedStatement ps = connection.prepareStatement(insert);
                ps.setString(1, id);
                ps.setString(2, name);
                ps.setString(3, password);
                ps.setBinaryStream(4,fin,fin.available());
                ps.executeUpdate();
                fin.close();
            }catch(IOException ex){
                ex.printStackTrace();
            }catch (SQLException ex){
                ex.printStackTrace();
            }

        }
    }
}
