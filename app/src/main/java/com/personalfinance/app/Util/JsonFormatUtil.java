package com.personalfinance.app.Util;


import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

public class JsonFormatUtil {



    /**
     * 把Blob类型转换为byte数组类型
     *
     * @param blob
     * @return
     */
    public static byte[] getBytes(Blob blob)
    {
        try
        {
            InputStream ins = blob.getBinaryStream();
            byte[] b = new byte[1024];
            int num = 0;
            String res = "";
            while ((num = ins.read(b)) != -1)
            {
                res += new String(b, 0, num);
            }
            return res.getBytes();
        } catch (SQLException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] blobToBytes(Blob blob)
    {
        InputStream is = null;
        byte[] b = null;
        try
        {
            is = blob.getBinaryStream();
            b = new byte[(int) blob.length()];
            is.read(b);
            return b;
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                is.close();
                is = null;
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return b;
    }




}
