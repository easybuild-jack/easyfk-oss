package com.mcst.easyfk.oss.controller;

import cn.hutool.core.io.file.FileNameUtil;
import com.mcst.easyfk.core.builders.RRBuilder;
import com.mcst.easyfk.core.constants.CharacterConstant;
import com.mcst.easyfk.core.constants.FileType;
import com.mcst.easyfk.core.dto.response.ResponseResult;
import com.mcst.easyfk.core.utils.common.EmptyUtil;
import com.mcst.easyfk.oss.properties.UploadProperties;
import com.mcst.easyfk.oss.util.FileTypeUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import jakarta.annotation.Resource;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.dromara.x.file.storage.core.upload.UploadPretreatment;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;


/**
 * @author liu yijun
 * @version 1.0
 */
@RestController
@RequestMapping("/oss/file")
public class OssController {
    @Resource
    private UploadProperties uploadProperties;

    @Resource
    private FileStorageService fileStorageService;//注入实列

    /**
     * 上传文件
     */
    @Operation(description = "上传接口")
    @PostMapping("/upload")
    @Parameters({@Parameter(description = "业务名称", name = "bizName"), @Parameter(description = "file", name = "文件信息", required = true)})
    public ResponseResult<String> upload(MultipartHttpServletRequest multipartRequest) {
        MultipartFile file = this.getFile(multipartRequest);
        if (EmptyUtil.isEmpty(file)) {
            return RRBuilder.buildFailedBody("上传的文件不可为空");
        }
        UploadPretreatment of = fileStorageService.of(file);
        String bizName = multipartRequest.getParameter("bizName");
        String filename = file.getOriginalFilename();
        assert filename != null;
        String extName = FileNameUtil.extName(filename);
        FileType fileType = FileTypeUtil.getFileType(extName);
        if (EmptyUtil.isNotEmpty(bizName)) {
            of.setPath(bizName + CharacterConstant.SLASH + fileType.name() + CharacterConstant.SLASH);
        } else {
            of.setPath(fileType.name() + CharacterConstant.SLASH);
        }
        if (fileType.equals(FileType.IMAGE)) {
            of.setContentType("image/" + extName);
            if (uploadProperties.getCompress()) {
                boolean compress = false;
                if (CharacterConstant.STAR.equals(uploadProperties.getCompressImageExtName())) {
                    compress = true;
                } else {
                    if (uploadProperties.getCompressImageExtName().contains(extName)) {
                        compress = true;
                    }
                }
                if (compress) {
                    of.image(img -> img.scale(uploadProperties.getScale())).image(img -> img.outputQuality(uploadProperties.getQuality()));
                }
            }
        }

        FileInfo fileInfo = of.upload();
        if (EmptyUtil.isEmpty(fileInfo)) {
            return RRBuilder.buildFailedBody("上传失败");
        }
        return RRBuilder.buildSuccessBody(fileInfo.getUrl());
    }

    private MultipartFile getFile(MultipartHttpServletRequest multipartRequest) {
        MultipartFile file = multipartRequest.getFile("file");
        if (EmptyUtil.isEmpty(file)) {
            file = multipartRequest.getFile("files");
        }
        return file;
    }

}
