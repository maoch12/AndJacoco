package com.wzh.server;

import javafx.beans.binding.Bindings;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Arrays;
import java.util.List;

@WebServlet(name = "JacocoApi")
public class JacocoApi extends HttpServlet {

    String fileDir = "/work/server/download/";

    protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(200);
        String path = request.getPathInfo();
        System.out.println("path=" + path);
        if ("/uploadEcFile".equals(path)) {
            uploadEcFile(request, resp);
            return;
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(200);

        String path = request.getPathInfo();
        System.out.println("path=" + path);
        if ("/queryEcFile".equals(path)) {
            queryEcFile(request, resp);
            return;
        }
    }

    private void uploadEcFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        //输出到客户端浏览器
        try {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload sup = new ServletFileUpload(factory);

            List<FileItem> list = sup.parseRequest(new ServletRequestContext(request));
            FileItem fileItem = null;
            String appName = null, versionCode = null;
            for (FileItem item : list) {
                System.out.println(item.getFieldName());
                if (!item.isFormField()) {
                    if ("file".equals(item.getFieldName())) {
                        fileItem = item;
                    }
                } else {
                    if ("appName".equals(item.getFieldName())) {
                        appName = item.getString();
                    } else if ("versionCode".equals(item.getFieldName())) {
                        versionCode = item.getString();
                    }
                }
            }
            if (fileItem != null && appName != null && versionCode != null) {
                saveFile(getSaveDir(appName, versionCode).getAbsolutePath(), fileItem);
                out.println("{\"code\":200,\"msg\":\"上传成功\"}");
            }else{
                out.println("{\"code\":402,\"msg\":\"上传失败，参数异常:appName="+appName+" versionCode="+versionCode+"\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println("{\"code\":401,\"msg\":\"上传失败，" + e.getMessage() + "\"}");
        }
        out.close();

    }

    private void saveFile(String dirPath, FileItem fileItem) throws IOException {
        String remoteFilename = new String(fileItem.getName().getBytes(), "UTF-8");
        File remoteFile = new File(remoteFilename);

        //设置服务器端存放文件的位置
        File locate = new File(dirPath, remoteFile.getName());
        System.out.println("save=" + locate.getAbsolutePath());

        locate.getParentFile().mkdirs();//用于确保文件目录存在,如果为单级目录可以去掉
        locate.createNewFile(); //创建新文件

        InputStream ins = fileItem.getInputStream();   //FileItem的内容
        OutputStream ous = new FileOutputStream(locate); //输出

        byte[] buffer = new byte[1024]; //缓冲字节
        int len = 0;
        while ((len = ins.read(buffer)) > -1)
            ous.write(buffer, 0, len);
        ins.close();
        ous.close();

    }

    private void queryEcFile(HttpServletRequest request, HttpServletResponse resp) throws IOException {
        String appName = request.getParameter("appName");
        String verCode = request.getParameter("versionCode");

        if (appName == null || verCode == null) {
            resp.setStatus(401);
            resp.getWriter().write("error appName==null || versionCode==null");
            return;
        }

        //设置状态码
        resp.setStatus(200);
        PrintWriter out = resp.getWriter();

        File f = getSaveDir(appName, verCode);
        System.out.println(f.getAbsolutePath() + " " + f.exists());
        File[] files;
        if (!f.exists() || isEmpty(files = f.listFiles())) {
            out.println("{\"files\":[]}");
        } else {
            StringBuffer sb = new StringBuffer();
            for (File file : files) {
                if(!file.getName().startsWith(".")){//隐藏文件
                    sb.append("\"/download/" + appName + "/" + verCode + "/" + file.getName() + "\",");
                }
            }
            sb.delete(sb.length() - 1, sb.length());
            out.println(String.format("{\"files\":[%s]}", sb.toString()));
        }
        out.close();

    }

    private File getSaveDir(String appName, String verCode) {
        return new File(System.getProperty("user.home") + fileDir, appName + "/" + verCode);
    }

    public static boolean isEmpty(Object[] arr) {
        if (arr == null || arr.length == 0) return true;
        return false;
    }

}
