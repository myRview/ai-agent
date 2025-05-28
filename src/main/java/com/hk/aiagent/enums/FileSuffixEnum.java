package com.hk.aiagent.enums;

import lombok.Getter;

/**
 * @author huangkun
 * @date 2025/5/27 14:49
 */
@Getter
public enum FileSuffixEnum {
    TXT("txt"),
    PDF("pdf"),
    DOC("doc"),
    DOCX("docx"),
    XLS("xls"),
    XLSX("xlsx"),
    PPT("ppt"),
    PPTX("pptx"),
    MP3("mp3"),
    MP4("mp4"),
    JPG("jpg"),
    JPEG("jpeg"),
    PNG("png"),
    GIF("gif"),
    ZIP("zip"),
    ;


    private final String value;

    FileSuffixEnum(String value) {
        this.value = value;
    }
}
