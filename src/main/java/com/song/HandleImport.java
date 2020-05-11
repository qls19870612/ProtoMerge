package com.song;

import com.song.Main.FileInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.song.Main.NEXT_LINE;
import static com.song.Main.toFileString;

/**
 *
 * 创建人  liangsong
 * 创建时间 2020/05/11 18:03
 */
public class HandleImport {
    private static final Logger logger = LoggerFactory.getLogger(HandleImport.class);

    public static void main(String[] args) throws IOException {
        String currPath = System.getProperty("user.dir") + "/";
        logger.info("main currPath:{}", currPath);
        String srcProtoPath = currPath + args[0];
        String toProtoPath = currPath + args[1];
//                String srcProtoPath = "D:\\SVN\\server\\card_client\\proto\\client";
        //                String toProtoPath = "D:\\SVN\\server\\card_client\\proto\\client_handler";
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

                long modifyTime = Long.parseLong(toFileLines.get(0).replace("//", ""));
                boolean isSame = modifyTime == srcFileModifyTime;
                if (!isSame) {
                    logger.info("更新文件 getName:{}", file.getPath());
                    fileInfo = createToFileWithImport(file, srcCreateTime, srcFileModifyTime, toProtoPath);
                } else {
                    fileInfo = new FileInfo(toFile);
                    fileInfo.createTime = srcCreateTime;
                    fileInfo.mainContent = toFileString(toFileLines);
                }


            } else {
                logger.info("创建新文件 getName:{}", file.getPath());
                fileInfo = createToFileWithImport(file, srcCreateTime, srcFileModifyTime, toProtoPath);
            }
            fileInfos.add(fileInfo);

        }

    }

    private static FileInfo createToFileWithImport(File file, long srcCreateTime, long srcFileModifyTime, String toProtoPath) throws IOException {
        List<String> strings = Files.readAllLines(Paths.get(file.toURI()));
        StringBuilder content = new StringBuilder();
        for (String string : strings) {
            if (string.startsWith("option ")) {

            } else if (string.startsWith("import ")) {
                content.append(string.replace("client/", "")).append(NEXT_LINE);
            } else {
                content.append(string).append(NEXT_LINE);
            }
        }

        File toFile = new File(toProtoPath + "/" + file.getName());
        FileInfo fileInfo = new FileInfo(toFile);
        fileInfo.mainContent = content.toString();

        fileInfo.createTime = srcCreateTime;

        FileOperator.writeFile(toFile, "//" + srcFileModifyTime + NEXT_LINE + fileInfo.mainContent);
        return fileInfo;
    }


}
