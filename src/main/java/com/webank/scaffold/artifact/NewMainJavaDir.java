//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.webank.scaffold.artifact;

import com.squareup.javapoet.TypeSpec;
import com.webank.scaffold.clhandler.ServicesHandler;
import com.webank.scaffold.clhandler.SystemConfigHandler;
import com.webank.scaffold.config.UserConfig;
import com.webank.scaffold.handler.NewBOHandler;
import com.webank.scaffold.handler.ServiceManagerHandler;
import com.webank.scaffold.util.CommonUtil;
import com.webank.scaffold.util.IOUtil;
import com.webank.scaffold.util.PackageNameUtil;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinition;

/**
 * @author marsli
 */
public class NewMainJavaDir extends MainJavaDir {
    private File abiDir;
    private String need;
    private NewBOHandler bOHandler;
    private ServicesHandler srvBuilder;
    private UserConfig config;

    public NewMainJavaDir(File parentDir, File abi, String need, UserConfig config) {
        super(parentDir, abi, need, config);
        this.abiDir = abi;
        this.need = need;
        this.bOHandler = new NewBOHandler(config);
        this.srvBuilder = new ServicesHandler(config);
        this.config = config;
    }

    @Override
    protected void doGenerateSubContents() throws Exception {
        this.handleIOUtil();
        this.handleBOAndService();
        this.handleApplication();
        this.handleSystemConfig();
        this.handleSdkBeanConfig();
        this.handleCommonResponse();
        this.handleServiceManager();
    }

    private void handleApplication() throws Exception {
        File javaDir = this.toFile();
        String pkg = PackageNameUtil.getRootPackageName(this.config);
        ApplicationJava applicationJava = new ApplicationJava(PackageNameUtil.convertPackageToFile(javaDir, pkg), this.config);
        applicationJava.generate();
    }

    private void handleIOUtil() throws Exception {
        File javaDir = this.toFile();
        String utilsPackage = PackageNameUtil.getUtilsPackageName(this.config);
        IOUtilJava ioUtilJava = new IOUtilJava(PackageNameUtil.convertPackageToFile(javaDir, utilsPackage), this.config);
        ioUtilJava.generate();
    }

    private void handleBOAndService() throws Exception {
        File javaDir = this.toFile();
        List<String> contractList = CommonUtil.contracts(this.abiDir, this.need);
        Iterator var3 = contractList.iterator();

        while(var3.hasNext()) {
            String contractName = (String)var3.next();
            File abiFile = new File(this.abiDir, contractName + ".abi");
            String abiStr = IOUtil.readAsString(abiFile);
            TypeSpec ctorBO = this.bOHandler.buildCtorBO(contractName, abiStr);
            Map<ABIDefinition, TypeSpec> functionBos = this.bOHandler.buildFunctionBO(contractName, abiStr);
            this.bOHandler.exportBOs(javaDir, Arrays.asList(ctorBO));
            this.bOHandler.exportBOs(javaDir, (List)functionBos.values().stream().collect(Collectors.toList()));
            TypeSpec serviceType = this.srvBuilder.build(contractName, ctorBO, functionBos);
            this.srvBuilder.exportBO(serviceType, javaDir);
        }

    }

    private void handleSystemConfig() throws Exception {
        List<String> contracts = CommonUtil.contracts(this.abiDir, this.need);
        SystemConfigHandler configBuilder = new SystemConfigHandler(this.toFile(), contracts, this.config);
        configBuilder.export();
    }

    private void handleSdkBeanConfig() throws Exception {
        File javaDir = this.toFile();
        String configPackage = PackageNameUtil.getConfigPackageName(this.config);
        NewSdkBeanConfigJava sdkBeanConfigJava = new NewSdkBeanConfigJava(PackageNameUtil.convertPackageToFile(javaDir, configPackage), this.config);
        sdkBeanConfigJava.generate();
    }

    private void handleCommonResponse() throws Exception {
        File javaDir = this.toFile();
        String utilsPackage = PackageNameUtil.getModelPackage(this.config);
        CommonResponseJava commonResponseJava = new CommonResponseJava(PackageNameUtil.convertPackageToFile(javaDir, utilsPackage), this.config);
        commonResponseJava.generate();
    }

    /**
     * generate service manager
     */
    private void handleServiceManager() throws IOException {
        List<String> contractNameList = Arrays.asList(this.need.split(","));
        ServiceManagerHandler serviceManagerHandler = new ServiceManagerHandler(contractNameList, this.config);
        serviceManagerHandler.export(serviceManagerHandler.build(), this.toFile());
    }

    @Override
    public String getName() {
        return "java";
    }
}
