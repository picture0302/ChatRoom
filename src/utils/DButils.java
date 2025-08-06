package utils;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DButils {
    //读取输入流中的数据放入propertie对象
   static Properties prop = new Properties();
   private static DruidDataSource dataSource;

        // 数据源配置

        static{
            Properties prop = new Properties();
            // 读取配置文件
            InputStream is = DButils.class.getResourceAsStream("/db.properties");
            try {
                prop.load(is);
            } catch (IOException e) {
                throw new RuntimeException("加载数据库配置文件失败", e);
            } finally

            {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                // 创建 Druid 数据源
                dataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(prop);
            } catch (Exception e) {
                throw new RuntimeException("初始化数据库连接池失败", e);
            }
        }

    public static Connection getConnection(){
        Connection conn = null;
        try{
            conn = dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return conn;
    }
    public static void close(Connection conn, PreparedStatement ps, ResultSet rs) {
        try {
            if(rs != null) {
                rs.close();
            }
            if(ps != null) {
                ps.close();
            }
            if(conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
