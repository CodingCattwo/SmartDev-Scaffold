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

package com.webank.scaffold.handler;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.webank.scaffold.clhandler.SystemConfigHandler;
import com.webank.scaffold.config.UserConfig;
import com.webank.scaffold.util.PackageNameUtil;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.lang.model.element.Modifier;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory;

/**
 * @author marsli
 */
public class ServiceManagerHandler {

    private UserConfig config;
    private List<String> contracts;

    public ServiceManagerHandler(List<String> contracts, UserConfig config) {
        this.config = config;
        this.contracts = contracts;
    }

    public TypeSpec build() {
        /**
         * 1. Service class
         */
        String configPkg = PackageNameUtil.getConfigPackageName(config);
        TypeSpec.Builder typeBuilder = this.initBuilder(configPkg);

        /**
         * 2. Instance fields: SystemConfig config|Client client
         */
        typeBuilder = this.populateInstanceFields(configPkg, typeBuilder);

        /**
         * 3. init post-construct
         */
        typeBuilder = this.populateInitializer(typeBuilder);

        /**
         * 3. Add method: init{ContractName}ServiceManager
         */
        typeBuilder = this.populateMethods(typeBuilder, this.contracts);
        return typeBuilder.build();

    }


    private TypeSpec.Builder initBuilder(String configPkg) {

        ClassName className = ClassName.get(configPkg, "ServiceManager");
        TypeSpec.Builder configClassBuilder
            = TypeSpec.classBuilder(className)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(ClassName.get("org.springframework.context.annotation","Configuration"))
            .addAnnotation(Data.class)
            .addAnnotation(Slf4j.class);
        return configClassBuilder;
    }

    private TypeSpec.Builder populateInstanceFields(String configPkg, TypeSpec.Builder typeBuilder){
        FieldSpec configField
            = FieldSpec.builder(
            ClassName.get(configPkg, SystemConfigHandler.SYSTEN_CONFIG), "config", Modifier.PRIVATE)
            .addAnnotation(ClassName.get("org.springframework.beans.factory.annotation","Autowired"))
            .build();
        FieldSpec clientField
            = FieldSpec.builder(Client.class, "client")
            .addModifiers(Modifier.PRIVATE)
            .addAnnotation(ClassName.get("org.springframework.beans.factory.annotation","Autowired"))
            .build();
        FieldSpec processField
            = FieldSpec.builder(List.class, "hexPrivateKeyList")
            .build();

        return typeBuilder
            .addField(configField)
            .addField(clientField)
            .addField(processField);
    }

    private TypeSpec.Builder populateInitializer(TypeSpec.Builder typeBuilder){
        MethodSpec.Builder txBuilder = MethodSpec.methodBuilder("init");
        txBuilder.addModifiers(Modifier.PUBLIC).addAnnotation(ClassName.get("javax.annotation","PostConstruct"));
        txBuilder
            .addStatement("hexPrivateKeyList = $T.asList(this.config.getHexPrivateKey().split(\",\"))", Arrays.class);
        typeBuilder.addMethod(txBuilder.build());
        return typeBuilder;
    }

    private TypeSpec.Builder populateMethods(TypeSpec.Builder typeBuilder, List<String> contracts) {
        for (String contractName : contracts) {
            String servicePkg = PackageNameUtil.getServicePackageName(config);
            String contractServiceName = contractName + "Service";
            ClassName serviceClassName = ClassName.get(servicePkg, contractServiceName);
            /**
             * add annotation of bean with service name
             */
            AnnotationSpec.Builder annotation = AnnotationSpec
                .builder(ClassName.get("org.springframework.context.annotation","Bean"))
                .addMember("value", "\"" + contractServiceName + "\"");
            /**
             * add method name with return
             * todo return Map
             */
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("init" + contractName + "ServiceManager");
            methodBuilder.addModifiers(Modifier.PUBLIC)
                .addException(Exception.class)
                //.addAnnotation(ClassName.get("import org.springframework.context.annotation","Bean"))
                .addAnnotation(annotation.build())
                .returns(Map.class);

            // new concurrent hash map of contractService
            methodBuilder.addStatement("$T<String, " + serviceClassName + "> serviceMap = new $T<>(this.hexPrivateKeyList.size())",
                Map.class, ConcurrentHashMap.class);
            /**
             * add loop
              */
            String firstLowerCaseContractServiceName = contractServiceName.substring(0, 1).toLowerCase() + contractServiceName.substring(1);
            methodBuilder.addCode(
                "for (int i = 0; i < this.hexPrivateKeyList.size(); i++) {\n"
                + "\t" + serviceClassName + " " + firstLowerCaseContractServiceName + " = new " + serviceClassName + "();\n"
                + "\t" + firstLowerCaseContractServiceName + ".setAddress(this.config.getContract().get" + contractName + "Address());\n"
                + "\t" + firstLowerCaseContractServiceName + ".setClient(this.client);\n"
                + "\t" + "if (!((String)this.hexPrivateKeyList.get(i)).isEmpty()) {\n"
                + "\t" + "\t" + ClassName.get(AssembleTransactionProcessor.class) + " txProcessor = "
                + ClassName.get(TransactionProcessorFactory.class) + ".createAssembleTransactionProcessor(this.client, this.client.getCryptoSuite().createKeyPair((String)this.hexPrivateKeyList.get(i)));\n"
                + "\t" + "\t" + firstLowerCaseContractServiceName + ".setTxProcessor(txProcessor);\n"
                + "\t" + "}\n"
                + "\t" + "serviceMap.put(client.getCryptoSuite().getCryptoKeyPair().getAddress(), " + firstLowerCaseContractServiceName +");\n"
                + "}\n");

            methodBuilder.addStatement("return serviceMap");
            typeBuilder.addMethod(methodBuilder.build());
        }

        return typeBuilder;
    }
    //for (int i = 0; i < this.hexPrivateKeyList.size(); i++) {
    //            BAC002Service bac002Service = new BAC002Service();
    //            bac002Service.setAddress(this.config.getContract().getBAC002Address());
    //            bac002Service.setClient(client);
    //            if (!((String)this.hexPrivateKeyList.get(i)).isEmpty()) {
    //                AssembleTransactionProcessor txProcessor = TransactionProcessorFactory
    //                    .createAssembleTransactionProcessor(this.client, this.client.getCryptoSuite().createKeyPair((String)this.hexPrivateKeyList.get(i)));
    //                bac002Service.setTxProcessor(txProcessor);
    //            }
    //            serviceMap.put(client.getCryptoSuite().getCryptoKeyPair().getAddress(), bac002Service);
    //        }

//    private ClassName serviceClassName(String contract){
//        String pkg = PackageNameUtil.getRootPackageName(config);
//        String serviceName = contract + "Service";
//        return ClassName.get(pkg+".service", serviceName);
//    }


    private TypeSpec.Builder populateInitializer(String contract, TypeSpec.Builder typeBuilder){
        MethodSpec.Builder txBuilder = MethodSpec.methodBuilder("initPrivateKeyList");
        txBuilder.addModifiers(Modifier.PUBLIC).addAnnotation(ClassName.get("javax.annotation","PostConstruct")).addException(Exception.class);
        txBuilder
            .addStatement("this.txProcessor = $T.createAssembleTransactionProcessor(this.client, this.client.getCryptoSuite().getCryptoKeyPair())", TransactionProcessorFactory.class);
        typeBuilder.addMethod(txBuilder.build());
        return typeBuilder;
    }


    public void export(TypeSpec serviceType, File javaDir) throws IOException {
        if(serviceType == null) {
            return;
        }
        String pkgName = PackageNameUtil.getServicePackageName(config);
        JavaFile file = JavaFile.builder(pkgName, serviceType)
            .build();

        file.writeTo(javaDir);
    }


}
