package com.song;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * 创建人  liangsong
 * 创建时间 2020/05/09 10:01
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static final String NEXT_LINE = System.getProperty("line.separator");


    public static void main1(String[] args) throws IOException {
        String currPath = System.getProperty("user.dir") + "/";
        logger.info("main currPath:{}", currPath);
        String srcProtoPath = currPath + args[0];
        String toProtoPath = currPath + args[1];
        String mergeProtoPath = currPath + args[2];
        //        String srcProtoPath = "D:\\SVN\\server\\card_client\\proto\\client";
        //        String toProtoPath = "D:\\SVN\\server\\card_client\\proto\\client_handler";
        //        String mergeProtoPath = "D:\\SVN\\server\\card_client\\proto\\client_handler\\merge.proto";

        ArrayList<File> srcFiles = FileOperator.getAllFiles(new File(srcProtoPath), ".proto");
        ArrayList<File> toFiles = FileOperator.getAllFiles(new File(toProtoPath), ".proto");
        HashMap<String, File> toFileMap = new HashMap<>();
        for (File toFile : toFiles) {
            toFileMap.put(toFile.getName(), toFile);
        }
        ArrayList<FileInfo> fileInfos = new ArrayList<>();
        for (File file : srcFiles) {
            BasicFileAttributes attributes = Files.readAttributes(Paths.get(file.toURI()), BasicFileAttributes.class);
            long srcCreateTime = attributes.creationTime().toMillis();
            long srcFileModifyTime = attributes.lastModifiedTime().toMillis();
            FileInfo fileInfo = null;
            if (toFileMap.containsKey(file.getName())) {
                File toFile = toFileMap.get(file.getName());
                List<String> toFileLines = null;
                try {

                    toFileLines = Files.readAllLines(Paths.get(toFile.toURI()));
                } catch (Exception e) {

                    toFileLines = Files.readAllLines(Paths.get(toFile.toURI()), Charset.defaultCharset());
                }

                long modifyTime = Long.parseLong(toFileLines.get(0));
                if (modifyTime != srcFileModifyTime) {
                    logger.info("更新文件 getName:{}", file.getPath());
                    fileInfo = createToFile(file, srcCreateTime, srcFileModifyTime, toProtoPath);
                } else {
                    fileInfo = new FileInfo(toFile);
                    fileInfo.createTime = srcCreateTime;
                    fileInfo.mainContent = toFileString(toFileLines);
                }


            } else {
                logger.info("创建新文件 getName:{}", file.getPath());
                fileInfo = createToFile(file, srcCreateTime, srcFileModifyTime, toProtoPath);
            }
            fileInfos.add(fileInfo);

        }
        fileInfos.sort(new Comparator<FileInfo>() {
            @Override
            public int compare(FileInfo o1, FileInfo o2) {
                return Long.compare(o1.createTime, o2.createTime);
            }
        });
        StringBuilder stringBuilder = new StringBuilder("syntax = \"proto2\";").append(NEXT_LINE).append("package proto;").append(NEXT_LINE);
        for (FileInfo fileInfo : fileInfos) {
            logger.info("main getName:{},createTime:{}", fileInfo.file.getName(), fileInfo.createTime);
            stringBuilder.append("/////////////////").append(fileInfo.file.getName()).append("/////////////////").append(NEXT_LINE);
            stringBuilder.append(fileInfo.mainContent);
        }
        logger.info("main mergeProtoPath:{}", mergeProtoPath);
        FileOperator.writeFile(new File(mergeProtoPath), stringBuilder.toString());

    }

    public static String toFileString(List<String> toFileLines) {
        StringBuilder stringBuilder = new StringBuilder();

        int size = toFileLines.size();
        for (int i = 1; i < size; i++) {
            stringBuilder.append(toFileLines.get(i)).append(NEXT_LINE);
        }
        return stringBuilder.toString();
    }

    private static FileInfo createToFile(File file, long srcCreateTime, long srcFileModifyTime, String toProtoPath) throws IOException {
        List<String> strings = Files.readAllLines(Paths.get(file.toURI()));
        StringBuilder content = new StringBuilder();
        for (String string : strings) {
            if (string.startsWith("option ") || string.startsWith("import ") || string.startsWith("package ")) {

            } else {
                content.append(string).append(NEXT_LINE);
            }
        }

        File toFile = new File(toProtoPath + "/" + file.getName());
        FileInfo fileInfo = new FileInfo(toFile);
        fileInfo.mainContent = content.toString();

        fileInfo.createTime = srcCreateTime;

        FileOperator.writeFile(toFile, srcFileModifyTime + NEXT_LINE + fileInfo.mainContent);
        return fileInfo;
    }

    static class FileInfo {
        public final File file;
        public String mainContent;
        public long createTime;

        public FileInfo(File file) {
            this.file = file;
        }
    }
}
