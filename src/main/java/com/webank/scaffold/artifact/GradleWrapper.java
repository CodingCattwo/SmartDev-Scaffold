/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.scaffold.artifact;

import com.webank.scaffold.util.IOUtil;
import java.io.File;
import java.io.InputStream;

/**
 * generate gradle script
 * @author marsli
 */
public class GradleWrapper implements Artifact {

    private static final String TEMPLATE_RESOURCE = "webase_templates/gradlew";
    private static final String TEMPLATE_RESOURCE_BAT = "webase_templates/gradlew.bat";
    private static final String GRADLE_SCRIPT_FILE = "gradlew";
    private static final String GRADLE_SCRIPT_BAT_FILE = "gradlew.bat";
    private File parent;

    public GradleWrapper(File parent) {
        this.parent = parent;
    }

    @Override
    public void generate() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream is = classLoader.getResourceAsStream(TEMPLATE_RESOURCE);
        InputStream is2 = classLoader.getResourceAsStream(TEMPLATE_RESOURCE_BAT);
        Throwable var3 = null;

        try {
            this.generateFile(is, this.gradleScriptFile());
            this.generateFile(is2, this.gradleBatFile());
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
            if (is2 != null) {
                if (var3 != null) {
                    try {
                        is2.close();
                    } catch (Throwable var11) {
                        var3.addSuppressed(var11);
                    }
                } else {
                    is2.close();
                }
            }
        }

    }

    public void generateFile(InputStream templateInput, File outputPath) throws Exception {
        String template = IOUtil.readAsString(templateInput);
        IOUtil.writeString(outputPath, template);
    }

    public File gradleScriptFile() {
        return new File(this.parent, GRADLE_SCRIPT_FILE);
    }

    public File gradleBatFile() {
        return new File(this.parent, GRADLE_SCRIPT_BAT_FILE);
    }

    // deprecated
    @Override
    public File toFile() {
        return new File(this.parent, GRADLE_SCRIPT_FILE);
    }

    @Override
    public File getParentDir() {
        return this.parent;
    }

    @Override
    public String getName() {
        return GRADLE_SCRIPT_FILE;
    }

}
