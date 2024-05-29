package com.heima.wemedia;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Application {

    public static void main(String[] args) {
        try {
            //获取本地图片
            File file = new File(convertPng("D:\\img\\QQ截图20240521102709.png"));
            //创建Tesseract对象
            ITesseract tesseract = new Tesseract();
            //设置字体库路径
            tesseract.setDatapath("E:\\study\\workspace\\tessdata");
            //中文识别
            tesseract.setLanguage("chi_sim");
            //执行ocr识别
            String result = tesseract.doOCR(file);
            //替换回车和tal键  使结果为一行
            result = result.replaceAll("\\r|\\n","-").replaceAll(" ","");
            System.out.println("识别的结果为："+result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //转换图片为png格式
    public static String convertPng(String url) {
        String tarFilePath = url.substring(0, url.lastIndexOf(".")) + ".png";
        try {
            BufferedImage bufferedImage = ImageIO.read(new File(url));
            BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.white, null);
            ImageIO.write(newBufferedImage, "png", new File(tarFilePath));
        } catch (IOException e) {
            return "";
        }
        return tarFilePath;
    }
}
