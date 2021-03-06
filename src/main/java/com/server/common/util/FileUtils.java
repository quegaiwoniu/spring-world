package com.server.common.util;

import org.apache.http.entity.ContentType;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Service
public class FileUtils {
    /**
     * 根据url拿取file
     *
     * @param url
     * @param suffix 文件后缀名
     */
    public static File createFileByUrl(String url, String suffix) {
        byte[] byteFile = getImageFromNetByUrl(url);
        if (byteFile != null) {
            File file = getFileFromBytes(byteFile, suffix);
            return file;
        } else {
            return null;
        }
    }

    /**
     * 根据地址获得数据的字节流
     *
     * @param strUrl 网络连接地址
     * @return
     */
    private static byte[] getImageFromNetByUrl(String strUrl) {
        try {
            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5 * 1000);
            InputStream inStream = conn.getInputStream();// 通过输入流获取图片数据
            byte[] btImg = readInputStream(inStream);// 得到图片的二进制数据
            return btImg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从输入流中获取数据
     *
     * @param inStream 输入流
     * @return
     * @throws Exception
     */
    private static byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
    }

    // 创建临时文件
    private static File getFileFromBytes(byte[] b, String suffix) {
        BufferedOutputStream stream = null;
        File file = null;
        try {
            file = File.createTempFile("pattern", "." + suffix);
            System.out.println("临时文件位置：" + file.getCanonicalPath());
            FileOutputStream fstream = new FileOutputStream(file);
            stream = new BufferedOutputStream(fstream);
            stream.write(b);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

    public static MultipartFile createImg(String url) {
        try {
            // File转换成MutipartFile
            File file = FileUtils.createFileByUrl(url, "jpg");
            FileInputStream inputStream = new FileInputStream(file);
            MultipartFile multipartFile = new MockMultipartFile(file.getName(), inputStream);
            return multipartFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MultipartFile fileToMultipart(String filePath) {
        try {
            // File转换成MutipartFile
            File file = new File(filePath);
            FileInputStream inputStream = new FileInputStream(file);
            MultipartFile multipartFile = new MockMultipartFile(file.getName(), "png", "image/png", inputStream);
            return multipartFile;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        // WebFileUtils.createFileByUrl("http://122.152.205.72:88/group1/M00/00/01/CpoxxFr7oIaAZ0rOAAC0d3GKDio580.png",
        // "png");
        // WebFileUtils.createImg("http://122.152.205.72:88/group1/M00/00/01/CpoxxFr7oIaAZ0rOAAC0d3GKDio580.png");
    }

    public static boolean base64ToFile(String filePath, String base64Data) throws Exception {
        String dataPrix = "";
        String data = "";

        if (base64Data == null || "".equals(base64Data)) {
            return false;
        } else {
            String[] d = base64Data.split("base64,");
            if (d != null && d.length == 2) {
                dataPrix = d[0];
                data = d[1];
            } else {
                return false;
            }
        }

        // 因为BASE64Decoder的jar问题，此处使用spring框架提供的工具包
        byte[] bs = Base64Utils.decodeFromString(data);
        // 使用apache提供的工具类操作流
        org.apache.commons.io.FileUtils.writeByteArrayToFile(new File(filePath), bs);

        return true;
    }

    /**
     * 获取文件全路径
     *
     * @param file
     * @param list
     * @return
     */
    public static List<String> printName(File file, List<String> list) {
        //判断文件是否为空
        if (file == null || !file.exists()) {
            return list;
        } else {
            //判断是否是文件夹
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    printName(f, list);
                }
            } else {
                // 文件全路径
                list.add(file.getPath());
            }
        }
        return list;
    }

    /**
     * 获取word文档的内容
     *
     * @param path 文档全路径
     * @return
     * @throws FileNotFoundException
     */
    public static String getTextByPath(String path) throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(path);
        String text = "";
        try {
            XWPFDocument doc = new XWPFDocument(inputStream);
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
            text = extractor.getText();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }

    /**
     * File类型转MultipartFile类型
     *
     * @param path
     * @return
     * @throws FileNotFoundException
     */
    public static MultipartFile fileToMultipartFile(String path) throws IOException {
//        String path = "D:\\fdd\\v1.6\\法律条款\\地方性法规\\《湖南省实施《中华人民共和国种子法》办法》_湖南省人民代表大会常务委员会_地方性法规_有效_[2019-09-28]_2019-12-01.docx";
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);
        // MockMultipartFile(String name, @Nullable String originalFilename, @Nullable String contentType, InputStream contentStream)
        // 其中originalFilename,String contentType 旧名字，类型  可为空
        // ContentType.APPLICATION_OCTET_STREAM.toString() 需要使用HttpClient的包
        MultipartFile multipartFile = new MockMultipartFile("copy" + file.getName(), file.getName(), ContentType.APPLICATION_OCTET_STREAM.toString(), fileInputStream);
        return multipartFile;
    }


}
