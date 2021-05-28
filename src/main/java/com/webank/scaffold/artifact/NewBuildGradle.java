//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.webank.scaffold.artifact;

import com.webank.scaffold.config.GeneratorOptions;
import com.webank.scaffold.config.UserConfig;
import com.webank.scaffold.util.IOUtil;
import com.webank.scaffold.util.PackageNameUtil;
import java.io.File;
import java.io.InputStream;

/**
 * @author marsli
 */
public class NewBuildGradle extends BuildGradle {
    private static final String TEMPLATE_RESOURCE = "webase_templates/build.gradle";
    private static final String BUILD_GRADLE_FILE = "build.gradle";
    private File parent;
    private UserConfig userConfig;

    public NewBuildGradle(File parent, UserConfig userConfig) {
        super(parent, userConfig);
        this.parent = parent;
        this.userConfig = userConfig;
    }

    @Override
    public void generate() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream is = classLoader.getResourceAsStream(TEMPLATE_RESOURCE);
        Throwable var3 = null;

        try {
            this.generate(is, this.toFile());
        } catch (Throwable var12) {
            var3 = var12;
            throw var12;
        } finally {
            if (is != null) {
                if (var3 != null) {
                    try {
                        is.close();
                    } catch (Throwable var11) {
                        var3.addSuppressed(var11);
                    }
                } else {
                    is.close();
                }
            }

        }

    }

    public void generate(InputStream templateInput, File outputPath) throws Exception {
        String template = IOUtil.readAsString(templateInput);
        template = this.replaceAllVars(template);
        IOUtil.writeString(outputPath, template);
    }

    @Override
    public File toFile() {
        return new File(this.parent, BUILD_GRADLE_FILE);
    }

    @Override
    public File getParentDir() {
        return this.parent;
    }

    @Override
    public String getName() {
        return "build.gradle";
    }

    private String replaceAllVars(String template) {
        String group = this.userConfig.getProperty(GeneratorOptions.GENERATOR_GROUP);
        String artifact = this.userConfig.getProperty(GeneratorOptions.GENERATOR_ARTIFACT);
        template = template.replace("${" + GeneratorOptions.GENERATOR_GROUP + "}", group);
        String pkg = PackageNameUtil.getRootPackageName(group, artifact);
        template = template.replace("${package}", pkg);
        return template;
    }
}
