package com.webank.scaffold.artifact;

import com.webank.scaffold.util.CommonUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author aaronchu
 * @Description
 * @data 2021/03/02
 */
public class ApplicationProperties implements Artifact {

    private File parentDir;
    private List<String> contracts;

    public ApplicationProperties(File parentDir, List<String> contracts){
        this.parentDir = parentDir;
        this.contracts = contracts;
    }

    @Override
    public void generate() throws Exception {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(this.toFile()))){
            writeSdkConnectKeys(writer);
            writeContractKeys(writer);
            writeSpringBootKeys(writer);
            writer.flush();
        }
    }
    private void writeSdkConnectKeys(BufferedWriter writer) throws IOException {
        writer.write("### Required\n");
        writer.write("system.peers=127.0.0.1:20200,127.0.0.1:20201\n");
        writer.write("### Required\n");
        writer.write("system.groupId=1\n");
        writer.write("### Optional. Default will search conf,config,src/main/conf/src/main/config\n");
        writer.write("system.certPath=conf,config,src/main/resources/conf,src/main/resources/config\n");
        writer.write("### Optional. If don't specify a random private key will be used\n");
        writer.write("system.hexPrivateKey=\n");
    }

    private void writeSpringBootKeys(BufferedWriter writer) throws IOException {
        writer.write("### ### Springboot server config\n");
        writer.write("server.port=8080\n");
        writer.write("server.session.timeout=60\n");
        writer.write("banner.charset=UTF-8\n");
        writer.write("spring.jackson.date-format=yyyy-MM-dd HH:mm:ss\n");
        writer.write("spring.jackson.time-zone=GMT+8\n");
    }

    private void writeContractKeys(BufferedWriter writer) throws IOException {
        for(String contractName :contracts){
            contractName = CommonUtil.makeFirstCharLowerCase(contractName);
            writer.write("### Optional. Please fill this address if you want to use related service\n");
            writer.write("system.contract."+contractName+"Address=\n");
        }
    }


    @Override
    public File getParentDir() {
        return this.parentDir;
    }

    @Override
    public String getName() {
        return "application.properties";
    }
}
