package com.webank.scaffold.artifact;

import com.webank.scaffold.artifact.NewMainResourceDir.ContractInfo;
import com.webank.scaffold.config.GeneratorOptions;
import com.webank.scaffold.config.UserConfig;
import com.webank.scaffold.exception.ScaffoldException;
import com.webank.scaffold.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.codegen.SolidityContractWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author marsli
 * @Description
 * @data 2021/01/19
 */
@Getter
public class NewMainDir extends MainDir {
    private static final Logger logger = LoggerFactory.getLogger(NewMainDir.class);

    private static final String MAIN_DIR = "main";
    private static final String SOL_Dir = "contracts";
    public static final String SOL_FILE_SUFFIX = ".sol";
    
    private List<ContractInfo> contractInfoList;
    private String systemPeers;
    private Integer groupId;
    private String hexPrivateKey;
    private Map<String, String> sdkContentMap;

    private UserConfig config;

    public NewMainDir(File basePath, UserConfig config, List<ContractInfo> contractInfoList) {
        super(null, basePath, null, config);
        this.config = config;
        this.contractInfoList = contractInfoList;
    }

    public NewMainDir(File basePath, UserConfig config, List<ContractInfo> contractInfoList,
        String systemPeers, Integer groupId, String hexPrivateKey, Map<String, String> sdkContentMap) {
        super(null, basePath, null, config);
        this.config = config;
        this.contractInfoList = contractInfoList;
        this.systemPeers = systemPeers;
        this.groupId = groupId;
        this.hexPrivateKey = hexPrivateKey;
        this.sdkContentMap = sdkContentMap;
    }


    /**
     * Generate sub directories: contracts，abi，bin，smbin，java
     * @throws Exception
     */
    @Override
    protected void doGenerateSubContents() throws Exception {
        /**
         * 1. Write raw solidity contracts to "contracts" directory
         */
        File contractsDir = generateContractsDir();

        /**
         * 2. Resources, set contractInfoList instead of get from files
         */
        NewMainResourceDir resources = new NewMainResourceDir(this.toFile(), contractInfoList,
            systemPeers, groupId, hexPrivateKey, sdkContentMap);
        resources.generate();

        /**
         * 3. Javas, remain unchanged
         */
        //1. Abi And Bin
        String need = StringUtils.join(
            contractInfoList.stream().map(ContractInfo::getContractName).toArray(),",");
        NewMainJavaDir javas = new NewMainJavaDir(
                this.toFile(),
                resources.getAbiDir(),
                need,
                this.config);
        javas.generate();

        /**
         * 4. generate raw solidity java file
         * generate raw java file of soldiity: 0.4.25
         */
        this.generateSolJavaFiles();

    }

    @Override
    public String getName() {
        return MAIN_DIR;
    }

    private File generateContractsDir() throws IOException {
        if (contractInfoList == null || contractInfoList.isEmpty()) {
            throw new ScaffoldException("contractInfoList is empty!");
        }
        File contractsDir = new File(this.toFile(), SOL_Dir);
        for (ContractInfo info : contractInfoList) {
            FileUtils.writeStringToFile(info.getSolRawString(), contractsDir,
                info.getContractName() + SOL_FILE_SUFFIX);
        }
        return contractsDir;
    }

    private void generateSolJavaFiles() {
        String groupName = config.getProperty(GeneratorOptions.GENERATOR_GROUP);
        String artifactName = config.getProperty(GeneratorOptions.GENERATOR_ARTIFACT);
        String javaFileOutputDir = this.getParentDir().getPath() + File.separator + MAIN_DIR
            + File.separator + "java" + File.separator + groupName.replace(".", File.separator)
            + File.separator + artifactName + File.separator + "raw";
        logger.info("generateSolJavaFiles javaFileOutputDir:{}", javaFileOutputDir);
        File check = new File(javaFileOutputDir);
        if (!check.exists()) {
            boolean result = check.mkdirs();
            logger.info("javaFileOutputDir mkdir:{}", result);
        }
        for (ContractInfo info : contractInfoList) {
            if (info == null) {
                logger.error("generate java file error for contractInfo list contains null info");
                continue;
            }
            logger.info("now generate java file of {}", info.getContractName());
            try {
                new SolidityContractWrapper().generateJavaFiles(
                        info.getContractName(),
                        info.getBinStr(),
                        info.getSmBinStr(),
                        info.getAbiStr(),
                        javaFileOutputDir,
                        "");
            } catch (Exception e) {
                logger.error("generate java file of {} failed for error:{}", info.getContractName(), e);
            }
        }

    }
}
