package com.mcst.easyfk.oss.util;

import cn.hutool.core.util.ArrayUtil;
import com.mcst.easyfk.core.constants.FileType;
import com.mcst.easyfk.core.utils.common.EmptyUtil;

/**
 * @author liu yijun
 * @version 1.0
 */
public class FileTypeUtil {

    private static final String[] IMAGE_NAMES = new String[]{"bmp", "dib", "gif", "jfif", "jpe", "jpeg", "jpg", "png"
            , "tif", "tiff", "ico"};
    private static final String[] AUDIO_NAMES = new String[]{"mp3", "mp4", "wma", "avi", "rm", "rmvb", "flv", "mpg",
            "mov", "mkv"};


    public static FileType getFileType(String extName) {
        if (isImage(extName)) {
            return FileType.IMAGE;
        } else {
            return isAudioFile(extName) ? FileType.VIDEO : FileType.FILE;
        }
    }


    public static boolean isImage(String extName) {
        return EmptyUtil.isNotEmpty(extName) && ArrayUtil.contains(IMAGE_NAMES, extName.toLowerCase());
    }

    private static boolean isAudioFile(String extName) {
        return EmptyUtil.isNotEmpty(extName) && ArrayUtil.contains(AUDIO_NAMES, extName.toLowerCase());
    }
}
