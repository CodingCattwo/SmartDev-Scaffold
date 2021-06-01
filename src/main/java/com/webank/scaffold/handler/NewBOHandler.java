//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.webank.scaffold.handler;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;
import com.webank.scaffold.clhandler.BOHandler;
import com.webank.scaffold.clhandler.ListObject;
import com.webank.scaffold.clhandler.SolidityTypeHandler;
import com.webank.scaffold.config.UserConfig;
import com.webank.scaffold.util.CommonUtil;
import com.webank.scaffold.util.PackageNameUtil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.lang.model.element.Modifier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinitionFactory;
import org.fisco.bcos.sdk.abi.wrapper.ContractABIDefinition;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinition.NamedType;
import org.fisco.bcos.sdk.crypto.CryptoSuite;

public class NewBOHandler extends BOHandler {
    private static final String CTOR = "CtorBO";
    private static final String INPUT = "InputBO";
    private UserConfig config;

    public NewBOHandler(UserConfig config) {
        super(config);
        this.config = config;
    }

    @Override
    public void exportBOs(File destDir, List<TypeSpec> bos) throws IOException {
        String pkg = PackageNameUtil.getBOPackageName(this.config);
        Iterator var4 = bos.iterator();

        while(var4.hasNext()) {
            TypeSpec typeSpec = (TypeSpec)var4.next();
            if (typeSpec != null) {
                JavaFile javaFile = JavaFile.builder(pkg, typeSpec).build();
                javaFile.writeTo(destDir);
            }
        }

    }

    @Override
    public TypeSpec buildCtorBO(String contractName, String abiStr) {
        ABIDefinitionFactory factory = new ABIDefinitionFactory(new CryptoSuite(0));
        ContractABIDefinition rootAbi = factory.loadABI(abiStr);
        ABIDefinition ctorAbi = rootAbi.getConstructor();
        if (ctorAbi != null && !ctorAbi.getInputs().isEmpty()) {
            String className = contractName + "CtorBO";
            return this.buildBOType(className, ctorAbi.getInputs());
        } else {
            return null;
        }
    }

    @Override
    public Map<ABIDefinition, TypeSpec> buildFunctionBO(String contractName, String abiStr) {
        ABIDefinitionFactory factory = new ABIDefinitionFactory(new CryptoSuite(0));
        ContractABIDefinition rootAbi = factory.loadABI(abiStr);
        Map<String, List<ABIDefinition>> functions = rootAbi.getFunctions();
        Map<ABIDefinition, TypeSpec> result = new HashMap();
        Iterator var7 = functions.entrySet().iterator();

        while(var7.hasNext()) {
            Entry<String, List<ABIDefinition>> e = (Entry)var7.next();
            List<ABIDefinition> definitions = (List)e.getValue();

            for(int i = 0; i < definitions.size(); ++i) {
                ABIDefinition abiDef = (ABIDefinition)definitions.get(i);
                String functionName = CommonUtil.makeFirstCharUpperCase(abiDef.getName());
                String overloadMark = i > 0 ? Integer.toString(i) : "";
                String className = contractName + functionName + overloadMark + "InputBO";
                TypeSpec inputType = this.buildBOType(className, abiDef.getInputs());
                result.put(abiDef, inputType);
            }
        }

        return result;
    }

    private TypeSpec buildBOType(String className, List<NamedType> args) {
        if (args.isEmpty()) {
            return null;
        } else {
            Builder boBuilder = TypeSpec.classBuilder(className).addModifiers(new Modifier[]{Modifier.PUBLIC}).addAnnotation(Data.class).addAnnotation(NoArgsConstructor.class).addAnnotation(AllArgsConstructor.class);
            int argIndex = 0;

            for(Iterator var5 = args.iterator(); var5.hasNext(); ++argIndex) {
                NamedType namedType = (NamedType)var5.next();
                String argName = namedType.getName();
                if (argName == null || argName.isEmpty()) {
                    argName = "arg" + argIndex;
                }

                String typeString = namedType.getTypeAsString();
                TypeName type = SolidityTypeHandler.convert(typeString);
                boBuilder.addField(type, argName, new Modifier[]{Modifier.PRIVATE});
            }

            com.squareup.javapoet.MethodSpec.Builder toArgsMethodBuilder = MethodSpec.methodBuilder("toArgs").addModifiers(new Modifier[]{Modifier.PUBLIC}).returns(
                ListObject.class.getGenericInterfaces()[0]).addStatement("$T args = new $T()", new Object[]{List.class, ArrayList.class});

            // todo fix args of mapping
            argIndex = 0;
            for(Iterator var11 = args.iterator(); var11.hasNext(); ++argIndex) {
                NamedType namedType = (NamedType) var11.next();
                String argName = namedType.getName();
                if (argName == null || argName.isEmpty()) {
                    argName = "arg" + argIndex;
                }
                toArgsMethodBuilder.addStatement("args.add($L)", new Object[]{argName});
            }
//            Iterator var11 = args.iterator();
//            while(var11.hasNext()) {
//                NamedType arg = (NamedType)var11.next();
//                toArgsMethodBuilder.addStatement("args.add($L)", new Object[]{arg.getName()});
//            }

            toArgsMethodBuilder.addStatement("return args", new Object[0]);
            boBuilder.addMethod(toArgsMethodBuilder.build());
            TypeSpec boType = boBuilder.build();
            return boType;
        }
    }
}
