package com.shareyi.molicode.hander.gencode.loader;

import com.shareyi.fileutil.FileUtil;
import com.shareyi.molicode.common.chain.handler.SimpleHandler;
import com.shareyi.molicode.common.chain.handler.awares.DataLoadHandlerAware;
import com.shareyi.molicode.common.constants.CommonConstant;
import com.shareyi.molicode.common.constants.MoliCodeConstant;
import com.shareyi.molicode.common.enums.EnumCode;
import com.shareyi.molicode.common.enums.ResultCodeEnum;
import com.shareyi.molicode.common.enums.TemplateTypeEnum;
import com.shareyi.molicode.common.enums.columns.AcProjectColumn;
import com.shareyi.molicode.common.exception.AutoCodeException;
import com.shareyi.molicode.common.utils.*;
import com.shareyi.molicode.common.vo.code.AutoCodeParams;
import com.shareyi.molicode.common.vo.code.AutoMakeVo;
import com.shareyi.molicode.common.vo.maven.MavenResourceVo;
import com.shareyi.molicode.common.web.CommonResult;
import com.shareyi.molicode.common.context.MoliCodeContext;
import com.shareyi.molicode.common.utils.ThreadLocalHolder;
import com.shareyi.molicode.service.gencode.AutoMakeService;
import com.shareyi.molicode.service.maven.MavenService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * autoMake.xml 数据加载器
 *
 * @author zhangshibin
 * @date 2018/10/3
 */
@Service
public class AutoMakeLoadHandler extends SimpleHandler<MoliCodeContext>
        implements DataLoadHandlerAware {

    /**
     * 约定的配置文件名称
     */
    public static final String AUTO_CODE_XML_FILE_NAME = "autoCode.xml";

    @Resource
    MavenService mavenService;

    @Resource
    AutoMakeService autoMakeService;

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public boolean shouldHandle(MoliCodeContext context) {
        return true;
    }

    @Override
    public void doHandle(MoliCodeContext context) {
        try {
            AutoCodeParams autoCodeParams = context.getAutoCodeParams();
            ValidateUtils.notEmptyField(autoCodeParams, AcProjectColumn.projectKey.name());
            autoMakeService.getConfigInfo(autoCodeParams);
            TemplateTypeEnum typeEnum = EnumCode.Parser.parseToNullSafe(TemplateTypeEnum.class, autoCodeParams.getTemplateType());
            AutoMakeVo autoMake = null;
            if (typeEnum == null) {
                typeEnum = TemplateTypeEnum.LOCAL;
            }
            if (Objects.equals(typeEnum, TemplateTypeEnum.LOCAL)) {
                ValidateUtils.notEmptyField(autoCodeParams, "templateBaseDir");
                File templateBaseFile = new File(autoCodeParams.getTemplateBaseDir());
                if (!templateBaseFile.exists()) {
                    throw new AutoCodeException("文件目录不存在，templateBaseDir=" + autoCodeParams.getTemplateBaseDir(), ResultCodeEnum.PARAM_ERROR);
                }
                if (templateBaseFile.isFile()) {
                    if (!isJarFile(templateBaseFile.getName())) {
                        throw new AutoCodeException("文件不是jar file，templateBaseDir=" + autoCodeParams.getTemplateBaseDir(), ResultCodeEnum.PARAM_ERROR);
                    }
                    autoMake = loadAutoMakeFromJarFile(autoCodeParams, templateBaseFile);
                } else {
                    autoCodeParams.setAutoXmlPath(FileUtil.contactPath(autoCodeParams.getTemplateBaseDir(), AUTO_CODE_XML_FILE_NAME));
                    String autoXmlPath = SystemFileUtils.parseFilePath(autoCodeParams.getAutoXmlPath());
                    String templateBaseDir = SystemFileUtils.parseFilePath(autoCodeParams.getTemplateBaseDir());
                    autoMake = XmlUtils.getAutoMake(autoXmlPath, templateBaseDir);
                }
            } else if (Objects.equals(typeEnum, TemplateTypeEnum.MAVEN)) {
                CommonResult<File> mavenFileResult = mavenService.getMavenTemplateFile(autoCodeParams.getMavenResourceVo(),
                        Objects.equals(CommonConstant.STD_YN_YES_STR, autoCodeParams.getFlushMaven()));
                if (!mavenFileResult.isSuccess()) {
                    throw new AutoCodeException(mavenFileResult.getMessage(), ResultCodeEnum.FAILURE);
                }
                File jarFilePath = mavenFileResult.getDefaultModel();
                autoMake = loadAutoMakeFromJarFile(autoCodeParams, jarFilePath);

            }
            context.put(MoliCodeConstant.CTX_KEY_AUTO_MAKE, autoMake);
        } catch (AutoCodeException ee) {
            throw ee;
        } catch (Exception e) {
            LogHelper.EXCEPTION.error("加载AutoMake.xml配置文件失败", e);
            throw new AutoCodeException("加载AutoMake.xml配置文件失败，原因是：" + e.getMessage(), ResultCodeEnum.EXCEPTION);
        }
    }

    /**
     * 从jar file中获取 autoMake 信息
     *
     * @param autoCodeParams
     * @param jarFilePath
     * @return
     * @throws IOException
     */
    private AutoMakeVo loadAutoMakeFromJarFile(AutoCodeParams autoCodeParams, File jarFilePath) throws IOException {

        JarFile jarFile = new JarFile(jarFilePath);

        JarEntry jarEntry = jarFile.getJarEntry(AUTO_CODE_XML_FILE_NAME);
        if (jarEntry == null) {
            throw new AutoCodeException("maven下载的jar包中，没有autoCode.xml配置文件!", ResultCodeEnum.FAILURE);
        }

        InputStream autoCodeXmlInputStream = jarFile.getInputStream(jarEntry);
        String autoCodeContent = IOUtils.toString(autoCodeXmlInputStream, Profiles.getInstance().getFileEncoding());
        IOUtils.closeQuietly(autoCodeXmlInputStream);
        AutoMakeVo autoMake = XmlUtils.getAutoMakeByContent(autoCodeContent);

        if (autoCodeParams.getLoadTemplateContent()) {
            ThreadLocalHolder.setJarFileToCodeContext(jarFile);
            autoMakeService.loadTemplateContent(autoMake, jarFile);
        }

        loadMavenInfo(jarFile, autoMake);
        return autoMake;
    }

    /**
     * 是否为jar 文件
     *
     * @param templateBaseFile
     * @return
     */
    private boolean isJarFile(String templateBaseFile) {
        return StringUtils.endsWith(templateBaseFile, ".jar");
    }

    /**
     * 获取maven相关信息
     *
     * @param jarFile
     * @param autoMake
     * @throws IOException
     */
    private void loadMavenInfo(JarFile jarFile, AutoMakeVo autoMake) throws IOException {
        Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
        JarEntry mavenPomEntry = null;
        while (jarEntryEnumeration.hasMoreElements()) {
            JarEntry entry = jarEntryEnumeration.nextElement();
            String entryName = entry.getName();
            if (entryName.endsWith("pom.xml") && entryName.contains("META-INF")) {
                mavenPomEntry = entry;
                break;
            }
        }
        if (mavenPomEntry != null) {
            InputStream mavenPomEntryInputStream = jarFile.getInputStream(mavenPomEntry);
            try {
                String mavenPomContent = IOUtils.toString(mavenPomEntryInputStream, Profiles.getInstance().getFileEncoding());
                autoMake.setMavenResourceVo(XmlUtils.parseMavenInfoByContent(mavenPomContent));
            } finally {
                IOUtils.closeQuietly(mavenPomEntryInputStream);
            }
        }
    }

}
