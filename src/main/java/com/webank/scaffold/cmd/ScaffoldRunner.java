package com.webank.scaffold.cmd;


import com.webank.scaffold.factory.ProjectFactory;
import picocli.CommandLine;

/**
 * @author aaronchu
 * @Description
 * @data 2021/01/20
 */

@CommandLine.Command(name = "ScaffoldRunner")
public class ScaffoldRunner implements Runnable{
    @CommandLine.Option(names = {"-s", "--sol"}, required = false, description = "Required. Solidity contracts dir.")
    private String solidityDir;

    @CommandLine.Option(names = {"-g", "--group"}, required = false,defaultValue = "org.example",description = "Optional. Group name.")
    private String group;

    @CommandLine.Option(names = {"-a", "--artifact"}, required = false, defaultValue = "demo",description = "Optional. Artifact name.")
    private String artifact;

    @CommandLine.Option(names = {"-o", "--output"},required = false, defaultValue = "artifacts",description = "Optional. Output directory.")
    private String output;

    @CommandLine.Option(names = {"-n", "--need"}, required = false,defaultValue = "",description = "Optional. The contracts you need,for example Contract1,Contract2,Contract3")
    private String need;

    @Override
    public void run() {

        solidityDir = "/Users/baidu/Documents/code/2021/java-projects/SmartDev-Scaffold/tools/contracts";
        output = "/Users/baidu/Documents/code/2021/java-projects/SmartDev-Scaffold/tools";
        need = "";
        ProjectFactory factory = new ProjectFactory(group, artifact, solidityDir,  output, need);
        factory.createProject();
    }

}
