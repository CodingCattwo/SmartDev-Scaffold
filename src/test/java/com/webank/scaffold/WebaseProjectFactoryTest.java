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

package com.webank.scaffold;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.scaffold.artifact.ProjectArtifact;
import com.webank.scaffold.artifact.NewMainResourceDir.ContractInfo;
import com.webank.scaffold.factory.WebaseProjectFactory;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import org.fisco.bcos.sdk.codegen.SolidityContractWrapper;
import org.fisco.bcos.sdk.codegen.exceptions.CodeGenException;
import org.junit.Test;

public class WebaseProjectFactoryTest {

    // hello world
    String contractName = "HelloWorld";
    String solSourceCodeBase64Str = "cHJhZ21hIHNvbGlkaXR5IF4wLjQuMjsNCmNvbnRyYWN0IEhlbGxvV29ybGR7DQogICAgc3RyaW5nIG5hbWU7DQogICAgZXZlbnQgU2V0TmFtZShzdHJpbmcgbmFtZSk7DQogICAgZnVuY3Rpb24gZ2V0KCljb25zdGFudCByZXR1cm5zKHN0cmluZyl7DQogICAgICAgIHJldHVybiBuYW1lOw0KICAgIH0NCiAgICBmdW5jdGlvbiBzZXQoc3RyaW5nIG4pew0KICAgICAgICBlbWl0IFNldE5hbWUobik7DQogICAgICAgIG5hbWU9bjsNCiAgICB9DQp9";
    String abiStr = "[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"name\",\"type\":\"string\"}],\"name\":\"SetName\",\"type\":\"event\"}]";
    String binStr = "608060405234801561001057600080fd5b50610373806100206000396000f30060806040526004361061004c576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680634ed3885e146100515780636d4ce63c146100ba575b600080fd5b34801561005d57600080fd5b506100b8600480360381019080803590602001908201803590602001908080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050919291929050505061014a565b005b3480156100c657600080fd5b506100cf610200565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561010f5780820151818401526020810190506100f4565b50505050905090810190601f16801561013c5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b7f4df9dcd34ae35f40f2c756fd8ac83210ed0b76d065543ee73d868aec7c7fcf02816040518080602001828103825283818151815260200191508051906020019080838360005b838110156101ac578082015181840152602081019050610191565b50505050905090810190601f1680156101d95780820380516001836020036101000a031916815260200191505b509250505060405180910390a180600090805190602001906101fc9291906102a2565b5050565b606060008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156102985780601f1061026d57610100808354040283529160200191610298565b820191906000526020600020905b81548152906001019060200180831161027b57829003601f168201915b5050505050905090565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106102e357805160ff1916838001178555610311565b82800160010185558215610311579182015b828111156103105782518255916020019190600101906102f5565b5b50905061031e9190610322565b5090565b61034491905b80821115610340576000816000905550600101610328565b5090565b905600a165627a7a72305820baa9e2a7ab055843a8a3de62b50fba49e6309323fb92358b598491fa5a76b9e90029";
    String smBinStr = "";
    // build params
    String group = "org";
    String artifactName = "demo_" + contractName.toLowerCase();
    String outputDir = "output";
    String sdkMapStr = "{\"sdk.key\":\"-----BEGIN PRIVATE KEY-----\\nMIGEAgEAMBAGByqGSM49AgEGBSuBBAAKBG0wawIBAQQgxqr/d/VgQ0fAr/KvyAeW\\nJ6bD1tqxZ5gYOdfIJiK7WOmhRANCAAT3g/OsuSAD2I/dKLWnZTbMGQ8l9WnkD/wr\\npyoiQkMy1qI5/3Sj4WFKGcVu9vhsd0nLoP+y1QttYKM0m5QGcuhP\\n-----END PRIVATE KEY-----\\n\",\"ca.crt\":\"-----BEGIN CERTIFICATE-----\\nMIIBsDCCAVagAwIBAgIJAPwQ7ISyofOIMAoGCCqGSM49BAMCMDUxDjAMBgNVBAMM\\nBWNoYWluMRMwEQYDVQQKDApmaXNjby1iY29zMQ4wDAYDVQQLDAVjaGFpbjAgFw0y\\nMTA0MDYxMjMwNDBaGA8yMTIxMDMxMzEyMzA0MFowNTEOMAwGA1UEAwwFY2hhaW4x\\nEzARBgNVBAoMCmZpc2NvLWJjb3MxDjAMBgNVBAsMBWNoYWluMFYwEAYHKoZIzj0C\\nAQYFK4EEAAoDQgAE6UcrK7ukGBVvBmWYwgIloM38ibqtxF2zBnM9zgU4bujjJU1Y\\nCZsHGKVGuNstSOZYfYulnTtFUoHhUEyhddvql6NQME4wHQYDVR0OBBYEFBBSyZi8\\nk/Hz/Q2SAin5bMnE1nOFMB8GA1UdIwQYMBaAFBBSyZi8k/Hz/Q2SAin5bMnE1nOF\\nMAwGA1UdEwQFMAMBAf8wCgYIKoZIzj0EAwIDSAAwRQIgEpuPZypVImOtDty9p50X\\njeD4wdgzHXpd3CDPui4CnZYCIQC4n+r97cCB51dPb+WjDNV5C18S2uI8LlNVj+xL\\ndSweAg==\\n-----END CERTIFICATE-----\\n\",\"sdk.crt\":\"-----BEGIN CERTIFICATE-----\\nMIIBeDCCAR+gAwIBAgIJAJoEtSMUsa8HMAoGCCqGSM49BAMCMDgxEDAOBgNVBAMM\\nB2FnZW5jeUExEzARBgNVBAoMCmZpc2NvLWJjb3MxDzANBgNVBAsMBmFnZW5jeTAg\\nFw0yMTA0MDYxMjMwNDBaGA8yMTIxMDMxMzEyMzA0MFowMTEMMAoGA1UEAwwDc2Rr\\nMRMwEQYDVQQKDApmaXNjby1iY29zMQwwCgYDVQQLDANzZGswVjAQBgcqhkjOPQIB\\nBgUrgQQACgNCAAT3g/OsuSAD2I/dKLWnZTbMGQ8l9WnkD/wrpyoiQkMy1qI5/3Sj\\n4WFKGcVu9vhsd0nLoP+y1QttYKM0m5QGcuhPoxowGDAJBgNVHRMEAjAAMAsGA1Ud\\nDwQEAwIF4DAKBggqhkjOPQQDAgNHADBEAiANbeRFiiS6mH+vcAOwV3wXd9YW/B2a\\n+vrHMm6NwtliRAIgRH4gSF0XLmpVOEO21bJFDGWm9siIX0cnj0R3kNGZcB4=\\n-----END CERTIFICATE-----\\n-----BEGIN CERTIFICATE-----\\nMIIBcTCCARegAwIBAgIJANrOZ+FrVNpIMAoGCCqGSM49BAMCMDUxDjAMBgNVBAMM\\nBWNoYWluMRMwEQYDVQQKDApmaXNjby1iY29zMQ4wDAYDVQQLDAVjaGFpbjAeFw0y\\nMTA0MDYxMjMwNDBaFw0zMTA0MDQxMjMwNDBaMDgxEDAOBgNVBAMMB2FnZW5jeUEx\\nEzARBgNVBAoMCmZpc2NvLWJjb3MxDzANBgNVBAsMBmFnZW5jeTBWMBAGByqGSM49\\nAgEGBSuBBAAKA0IABIqMDvvzvTq8WW1UtJrnnsifw9/OrPsMc9CrrYBsWdwOGhdx\\nfNTJA1ss+vngjrhAmWHczvbh+E1WOlDGzpCumeqjEDAOMAwGA1UdEwQFMAMBAf8w\\nCgYIKoZIzj0EAwIDSAAwRQIhALsAbAQ9BDeofk4VYzYx2ZAHB1HviDp9ndvXAkLN\\nsfHZAiAjViK97dDr3gxP/qHg0e8BG9ptEv7Do8caOPj33F+yOQ==\\n-----END CERTIFICATE-----\\n-----BEGIN CERTIFICATE-----\\nMIIBsDCCAVagAwIBAgIJAPwQ7ISyofOIMAoGCCqGSM49BAMCMDUxDjAMBgNVBAMM\\nBWNoYWluMRMwEQYDVQQKDApmaXNjby1iY29zMQ4wDAYDVQQLDAVjaGFpbjAgFw0y\\nMTA0MDYxMjMwNDBaGA8yMTIxMDMxMzEyMzA0MFowNTEOMAwGA1UEAwwFY2hhaW4x\\nEzARBgNVBAoMCmZpc2NvLWJjb3MxDjAMBgNVBAsMBWNoYWluMFYwEAYHKoZIzj0C\\nAQYFK4EEAAoDQgAE6UcrK7ukGBVvBmWYwgIloM38ibqtxF2zBnM9zgU4bujjJU1Y\\nCZsHGKVGuNstSOZYfYulnTtFUoHhUEyhddvql6NQME4wHQYDVR0OBBYEFBBSyZi8\\nk/Hz/Q2SAin5bMnE1nOFMB8GA1UdIwQYMBaAFBBSyZi8k/Hz/Q2SAin5bMnE1nOF\\nMAwGA1UdEwQFMAMBAf8wCgYIKoZIzj0EAwIDSAAwRQIgEpuPZypVImOtDty9p50X\\njeD4wdgzHXpd3CDPui4CnZYCIQC4n+r97cCB51dPb+WjDNV5C18S2uI8LlNVj+xL\\ndSweAg==\\n-----END CERTIFICATE-----\\n\"}";
    String gradleDir = "gradle";

    @Test
    public void testBuild() throws Exception {
        String solSourceCode = new String(Base64.getDecoder().decode(solSourceCodeBase64Str));

        ContractInfo contractInfo = new ContractInfo();
        contractInfo.setContractName(contractName);
        contractInfo.setAbiStr(abiStr);
        contractInfo.setBinStr(binStr);
        contractInfo.setSmBinStr("");
        contractInfo.setSolRawString(solSourceCode);
        contractInfo.setContractAddress("0xd5d4fcf2a46831510f095bfb447bc945f99309f7");

        ContractInfo creditContract = new ContractInfo();
        creditContract.setContractName("Credit");
        creditContract.setAbiStr("[{\"constant\":false,\"inputs\":[{\"name\":\"comName\",\"type\":\"string\"},{\"name\":\"mergeField\",\"type\":\"string\"}],\"name\":\"register\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"comName\",\"type\":\"string\"}],\"name\":\"selectList\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"},{\"name\":\"\",\"type\":\"string[]\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"comName\",\"type\":\"string\"},{\"name\":\"mergeField\",\"type\":\"string\"}],\"name\":\"selectOne\",\"outputs\":[{\"name\":\"\",\"type\":\"int256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"ret\",\"type\":\"int256\"},{\"indexed\":false,\"name\":\"comName\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"mergeField\",\"type\":\"string\"}],\"name\":\"RegisterEvent\",\"type\":\"event\"}]");
        creditContract.setBinStr("60806040523480156200001157600080fd5b506200002b62000031640100000000026401000000009004565b62000224565b600061100190508073ffffffffffffffffffffffffffffffffffffffff166356004b6a6040518163ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004016200008d90620001ce565b602060405180830381600087803b158015620000a857600080fd5b505af1158015620000bd573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250620000e39190810190620000fd565b5050565b6000620000f582516200021a565b905092915050565b6000602082840312156200011057600080fd5b60006200012084828501620000e7565b91505092915050565b6000601182527f6d657267654669656c642c7374617475730000000000000000000000000000006020830152604082019050919050565b6000601982527f745f6372656469745f636f6d70616e795f7265706f72745f31000000000000006020830152604082019050919050565b6000600782527f636f6d4e616d65000000000000000000000000000000000000000000000000006020830152604082019050919050565b60006060820190508181036000830152620001e98162000160565b90508181036020830152620001fe8162000197565b90508181036040830152620002138162000129565b9050919050565b6000819050919050565b6116f780620002346000396000f300608060405260043610610057576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680633ffbd47f1461005c5780636d50e48214610099578063edf90a89146100d7575b600080fd5b34801561006857600080fd5b50610083600480360361007e91908101906110e3565b610114565b6040516100909190611369565b60405180910390f35b3480156100a557600080fd5b506100c060048036036100bb9190810190611061565b6104c9565b6040516100ce929190611384565b60405180910390f35b3480156100e357600080fd5b506100fe60048036036100f991908101906110e3565b610a8c565b60405161010b9190611369565b60405180910390f35b600080600080600080600094506000935061012f8888610a8c565b935060008414151561045c57610143610dcb565b92508273ffffffffffffffffffffffffffffffffffffffff166313db93466040518163ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401602060405180830381600087803b1580156101a957600080fd5b505af11580156101bd573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052506101e19190810190610fe6565b91508173ffffffffffffffffffffffffffffffffffffffff1663e942b516896040518263ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004016102389190611479565b600060405180830381600087803b15801561025257600080fd5b505af1158015610266573d6000803e3d6000fd5b505050508173ffffffffffffffffffffffffffffffffffffffff1663e942b516886040518263ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004016102bf9190611501565b600060405180830381600087803b1580156102d957600080fd5b505af11580156102ed573d6000803e3d6000fd5b505050508173ffffffffffffffffffffffffffffffffffffffff1663e942b5166040518163ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401610344906114ae565b600060405180830381600087803b15801561035e57600080fd5b505af1158015610372573d6000803e3d6000fd5b505050508273ffffffffffffffffffffffffffffffffffffffff166331afac3689846040518363ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004016103cd929190611429565b602060405180830381600087803b1580156103e757600080fd5b505af11580156103fb573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525061041f9190810190611038565b905060018114156104335760009450610457565b7ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe94505b610480565b7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff94505b7fe71002dee81d9ff68a8184c07ed89508062d232ea9979314fd048b99aca6f25e8589896040516104b3939291906113b4565b60405180910390a1849550505050505092915050565b60006060600080600060606000806104df610dcb565b95508573ffffffffffffffffffffffffffffffffffffffff16637857d7c96040518163ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401602060405180830381600087803b15801561054557600080fd5b505af1158015610559573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525061057d9190810190610f94565b94508473ffffffffffffffffffffffffffffffffffffffff1663cd30a1d16040518163ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004016105d2906114ae565b600060405180830381600087803b1580156105ec57600080fd5b505af1158015610600573d6000803e3d6000fd5b505050508573ffffffffffffffffffffffffffffffffffffffff1663e8434e398a876040518363ffffffff167c010000000000000000000000000000000000000000000000000000000002815260040161065b9291906113f9565b602060405180830381600087803b15801561067557600080fd5b505af1158015610689573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052506106ad9190810190610fbd565b93508373ffffffffffffffffffffffffffffffffffffffff1663949d225d6040518163ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401602060405180830381600087803b15801561071357600080fd5b505af1158015610727573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525061074b9190810190611038565b60405190808252806020026020018201604052801561077e57816020015b60608152602001906001900390816107695790505b5092508373ffffffffffffffffffffffffffffffffffffffff1663949d225d6040518163ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401602060405180830381600087803b1580156107e557600080fd5b505af11580156107f9573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525061081d9190810190611038565b60001415610853577fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8381915097509750610a81565b600091505b8373ffffffffffffffffffffffffffffffffffffffff1663949d225d6040518163ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401602060405180830381600087803b1580156108bc57600080fd5b505af11580156108d0573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052506108f49190810190611038565b821215610a76578373ffffffffffffffffffffffffffffffffffffffff1663846719e0836040518263ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004016109509190611369565b602060405180830381600087803b15801561096a57600080fd5b505af115801561097e573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052506109a29190810190610fe6565b90508073ffffffffffffffffffffffffffffffffffffffff16639c981fcb6040518163ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004016109f7906114e1565b600060405180830381600087803b158015610a1157600080fd5b505af1158015610a25573d6000803e3d6000fd5b505050506040513d6000823e3d601f19601f82011682018060405250610a4e91908101906110a2565b8383815181101515610a5c57fe5b906020019060200201819052508180600101925050610858565b600083819150975097505b505050505050915091565b600080600080610a9a610dcb565b92508273ffffffffffffffffffffffffffffffffffffffff16637857d7c96040518163ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401602060405180830381600087803b158015610b0057600080fd5b505af1158015610b14573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250610b389190810190610f94565b91508173ffffffffffffffffffffffffffffffffffffffff1663cd30a1d1866040518263ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401610b8f9190611501565b600060405180830381600087803b158015610ba957600080fd5b505af1158015610bbd573d6000803e3d6000fd5b505050508173ffffffffffffffffffffffffffffffffffffffff1663cd30a1d16040518163ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401610c14906114ae565b600060405180830381600087803b158015610c2e57600080fd5b505af1158015610c42573d6000803e3d6000fd5b505050508273ffffffffffffffffffffffffffffffffffffffff1663e8434e3987846040518363ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401610c9d9291906113f9565b602060405180830381600087803b158015610cb757600080fd5b505af1158015610ccb573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250610cef9190810190610fbd565b90508073ffffffffffffffffffffffffffffffffffffffff1663949d225d6040518163ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401602060405180830381600087803b158015610d5557600080fd5b505af1158015610d69573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250610d8d9190810190611038565b60001415610dbd577fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff9350610dc2565b600093505b50505092915050565b600080600061100191508173ffffffffffffffffffffffffffffffffffffffff1663f23f63c96040518163ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401610e2890611459565b602060405180830381600087803b158015610e4257600080fd5b505af1158015610e56573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250610e7a919081019061100f565b9050809250505090565b6000610e9082516115f4565b905092915050565b6000610ea48251611606565b905092915050565b6000610eb88251611618565b905092915050565b6000610ecc825161162a565b905092915050565b6000610ee0825161163c565b905092915050565b600082601f8301121515610efb57600080fd5b8135610f0e610f0982611563565b611536565b91508082526020830160208301858383011115610f2a57600080fd5b610f3583828461166a565b50505092915050565b600082601f8301121515610f5157600080fd5b8151610f64610f5f82611563565b611536565b91508082526020830160208301858383011115610f8057600080fd5b610f8b838284611679565b50505092915050565b600060208284031215610fa657600080fd5b6000610fb484828501610e84565b91505092915050565b600060208284031215610fcf57600080fd5b6000610fdd84828501610e98565b91505092915050565b600060208284031215610ff857600080fd5b600061100684828501610eac565b91505092915050565b60006020828403121561102157600080fd5b600061102f84828501610ec0565b91505092915050565b60006020828403121561104a57600080fd5b600061105884828501610ed4565b91505092915050565b60006020828403121561107357600080fd5b600082013567ffffffffffffffff81111561108d57600080fd5b61109984828501610ee8565b91505092915050565b6000602082840312156110b457600080fd5b600082015167ffffffffffffffff8111156110ce57600080fd5b6110da84828501610f3e565b91505092915050565b600080604083850312156110f657600080fd5b600083013567ffffffffffffffff81111561111057600080fd5b61111c85828601610ee8565b925050602083013567ffffffffffffffff81111561113957600080fd5b61114585828601610ee8565b9150509250929050565b600061115a8261159c565b808452602084019350836020820285016111738561158f565b60005b848110156111ac57838303885261118e838351611220565b9250611199826115bd565b9150602088019750600181019050611176565b508196508694505050505092915050565b6111c681611646565b82525050565b6111d581611658565b82525050565b6111e4816115ea565b82525050565b60006111f5826115b2565b808452611209816020860160208601611679565b611212816116ac565b602085010191505092915050565b600061122b826115a7565b80845261123f816020860160208601611679565b611248816116ac565b602085010191505092915050565b6000601982527f745f6372656469745f636f6d70616e795f7265706f72745f31000000000000006020830152604082019050919050565b6000600182527f31000000000000000000000000000000000000000000000000000000000000006020830152604082019050919050565b6000600782527f636f6d4e616d65000000000000000000000000000000000000000000000000006020830152604082019050919050565b6000600682527f73746174757300000000000000000000000000000000000000000000000000006020830152604082019050919050565b6000600a82527f6d657267654669656c64000000000000000000000000000000000000000000006020830152604082019050919050565b600060208201905061137e60008301846111db565b92915050565b600060408201905061139960008301856111db565b81810360208301526113ab818461114f565b90509392505050565b60006060820190506113c960008301866111db565b81810360208301526113db81856111ea565b905081810360408301526113ef81846111ea565b9050949350505050565b6000604082019050818103600083015261141381856111ea565b905061142260208301846111bd565b9392505050565b6000604082019050818103600083015261144381856111ea565b905061145260208301846111cc565b9392505050565b6000602082019050818103600083015261147281611256565b9050919050565b60006040820190508181036000830152611492816112c4565b905081810360208301526114a681846111ea565b905092915050565b600060408201905081810360008301526114c7816112fb565b905081810360208301526114da8161128d565b9050919050565b600060208201905081810360008301526114fa81611332565b9050919050565b6000604082019050818103600083015261151a81611332565b9050818103602083015261152e81846111ea565b905092915050565b6000604051905081810181811067ffffffffffffffff8211171561155957600080fd5b8060405250919050565b600067ffffffffffffffff82111561157a57600080fd5b601f19601f8301169050602081019050919050565b6000602082019050919050565b600081519050919050565b600081519050919050565b600081519050919050565b6000602082019050919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000819050919050565b60006115ff826115ca565b9050919050565b6000611611826115ca565b9050919050565b6000611623826115ca565b9050919050565b6000611635826115ca565b9050919050565b6000819050919050565b6000611651826115ca565b9050919050565b6000611663826115ca565b9050919050565b82818337600083830152505050565b60005b8381101561169757808201518184015260208101905061167c565b838111156116a6576000848401525b50505050565b6000601f19601f83011690509190505600a265627a7a723058207a7c4cd11c41ef79317b6d0e3563f05519a5f9157c55514b193d40d2257793226c6578706572696d656e74616cf50037");
        creditContract.setSmBinStr("");
        String bbbSource = new String(Base64.getDecoder().decode("cHJhZ21hIHNvbGlkaXR5ID49MC40LjIyIDwwLjcuMDsKcHJhZ21hIGV4cGVyaW1lbnRhbCBBQklFbmNvZGVyVjI7CmltcG9ydCAiLi9BZG1pbi5zb2wiOwppbXBvcnQgIi4vVXNlci5zb2wiOwoKY29udHJhY3QgTWFuYWdlbWVudENlbnRlciBpcyBBZG1pbiB7CgogICAgbWFwcGluZyhhZGRyZXNzID0+IGJvb2wpIHByaXZhdGUgdXNlcnM7CiAgICBtYXBwaW5nKGFkZHJlc3MgPT4gYWRkcmVzcykgcHJpdmF0ZSB1c2Vyc0NvbnRyYWN0OwogICAgYWRkcmVzc1tdIHByaXZhdGUgdXNlcmxpc3Q7CiAgICBtYXBwaW5nKGFkZHJlc3MgPT4gYm9vbCkgcHJpdmF0ZSBncmFudG9yczsKICAgIGFkZHJlc3NbXSBwcml2YXRlIGdyYW50b3JsaXN0OwoKICAgY29uc3RydWN0b3IoKSBwdWJsaWMgQWRtaW4obXNnLnNlbmRlcikKICAgIHsKICAgIH0KICAgICAgICAKICAgIGZ1bmN0aW9uIHJlZ2lzdGVyVXNlcihhZGRyZXNzIF91c2VyLCBzdHJpbmcgX2luZm8pIHB1YmxpYyBvbmx5QWRtaW4gcmV0dXJucyhhZGRyZXNzKSB7CiAgICAgICAgcmVxdWlyZSh1c2Vyc0NvbnRyYWN0W191c2VyXSA9PSBhZGRyZXNzKDApLCAidXNlciBhbHJlYWR5IGV4aXN0Iik7CiAgICAgICAgdXNlcnNbX3VzZXJdID0gdHJ1ZTsKICAgICAgICB1c2VybGlzdC5wdXNoKF91c2VyKTsKICAgICAgICB1c2Vyc0NvbnRyYWN0W191c2VyXSA9IG5ldyBVc2VyKF9pbmZvKTsKICAgICAgICByZXR1cm4gdXNlcnNDb250cmFjdFtfdXNlcl07CiAgICB9CiAgICAKICAgIGZ1bmN0aW9uIHVucmVnaXN0ZXJVc2VyKGFkZHJlc3MgX3VzZXIpIHB1YmxpYyBvbmx5QWRtaW4gewogICAgICAgIHJlcXVpcmUodXNlcnNDb250cmFjdFtfdXNlcl0gIT0gYWRkcmVzcygwKSwgInVzZXIgbm90IGV4aXN0Iik7CiAgICAgICAgdXNlcnNbX3VzZXJdID0gZmFsc2U7CiAgICB9ICAgIAogICAgCiAgICBmdW5jdGlvbiBpc1VzZXJFeGlzdChhZGRyZXNzIF91c2VyKSBwdWJsaWMgY29uc3RhbnQgcmV0dXJucyAoYm9vbCl7CiAgICAgICAgcmV0dXJuIHVzZXJzW191c2VyXTsKICAgIH0KCiAgICBmdW5jdGlvbiByZWdpc3RlckdyYW50b3IoYWRkcmVzcyBfZ3JhbnRvcikgcHVibGljIG9ubHlBZG1pbiByZXR1cm5zKGJvb2wpIHsKICAgICAgICBncmFudG9yc1tfZ3JhbnRvcl0gPSB0cnVlOwogICAgICAgIHJldHVybiBncmFudG9yc1tfZ3JhbnRvcl07CiAgICB9CiAgICAKICAgIGZ1bmN0aW9uIHVucmVnaXN0ZXJHcmFudG9yKGFkZHJlc3MgX2dyYW50b3IpIHB1YmxpYyBvbmx5QWRtaW4gewogICAgICAgIGdyYW50b3JzW19ncmFudG9yXSA9IGZhbHNlOwogICAgfSAgICAKICAgIAogICAgZnVuY3Rpb24gaXNVc2VyR3JhbnRvcihhZGRyZXNzIF9ncmFudG9yKSBwdWJsaWMgY29uc3RhbnQgcmV0dXJucyAoYm9vbCl7CiAgICAgICAgcmV0dXJuIGdyYW50b3JzW19ncmFudG9yXTsKICAgIH0KCiAgICBmdW5jdGlvbiBhZGRVc2VyQ2VydGlmaWNhdGUoc3RyaW5nIF9jZXJ0aWZpY2F0ZUhhc2gsIHN0cmluZyBfY2VydGlmaWNhdGVzRGVzYywgYWRkcmVzcyBfZ3JhbnRvcikgcHVibGljIHJldHVybnMoYWRkcmVzcykgewogICAgICAgICAgICByZXF1aXJlKHVzZXJzQ29udHJhY3RbbXNnLnNlbmRlcl0gIT0gYWRkcmVzcygwKSwgInVzZXIgbm90IGV4aXN0Iik7CiAgICAgICAgICAgIHJlcXVpcmUodXNlcnNbbXNnLnNlbmRlcl0sICJ1bnJlZ2lzdGVyZWQgVXNlciIpOwogICAgICAgICAgICByZXR1cm4gVXNlcih1c2Vyc0NvbnRyYWN0W21zZy5zZW5kZXJdKS5hZGRDZXJ0aWZpY2F0ZShfY2VydGlmaWNhdGVIYXNoLCBfY2VydGlmaWNhdGVzRGVzYywgX2dyYW50b3IpOwogICAgfQoKICAgIGZ1bmN0aW9uIGdyYW50VXNlckNlcnRpZmljYXRlKGFkZHJlc3MgX3VzZXIsIHN0cmluZyBfY2VydGlmaWNhdGVIYXNoKSBwdWJsaWMgcmV0dXJucyhib29sKSB7CiAgICAgICAgICAgIHJlcXVpcmUoZ3JhbnRvcnNbbXNnLnNlbmRlcl0sICJ1bnJlZ2lzdGVyZWQgZ3JhbnRvciIpOwogICAgICAgICAgICByZXR1cm4gVXNlcih1c2Vyc0NvbnRyYWN0W191c2VyXSkuZ3JhbnRDZXJ0aWZpY2F0ZShfY2VydGlmaWNhdGVIYXNoLCBtc2cuc2VuZGVyKTsKICAgIH0KCiAgICBmdW5jdGlvbiBWZXJpZnlVc2VyQ2VydGlmaWNhdGUoIGFkZHJlc3MgX3VzZXIsIHN0cmluZyBfY2VydGlmaWNhdGVIYXNoKSBwdWJsaWMgcmV0dXJucyhib29sKSB7CiAgICAgICAgICAgIHJlcXVpcmUodXNlcnNDb250cmFjdFtfdXNlcl0gIT0gYWRkcmVzcygwKSwgImNlcnRpZmljYXRlIGRvZXMgbm90IGV4aXN0Iik7CiAgICAgICAgICAgIHJlcXVpcmUodXNlcnNbX3VzZXJdLCAidW5yZWdpc3RlcmVkIFVzZXIiKTsKICAgICAgICAgICAgcmV0dXJuIFVzZXIodXNlcnNDb250cmFjdFtfdXNlcl0pLnZlcmlmeUNlcnRpZmljYXRlKF9jZXJ0aWZpY2F0ZUhhc2gpOwogICAgfQogICAgCiAgICBmdW5jdGlvbiBnZXRVc2VySW5mbyhhZGRyZXNzIF91c2VyKSAgcHVibGljIGNvbnN0YW50IHJldHVybnMgKHN0cmluZywgc3RyaW5nW10sIHN0cmluZ1tdLCBhZGRyZXNzW10sIGJvb2xbXSkgewogICAgICAgIHJlcXVpcmUodXNlcnNDb250cmFjdFtfdXNlcl0gIT0gYWRkcmVzcygwKSwgImNlcnRpZmljYXRlIGRvZXMgbm90IGV4aXN0Iik7CiAgICAgICAgcmVxdWlyZSh1c2Vyc1tfdXNlcl0sICJ1bnJlZ2lzdGVyZWQgVXNlciIpOwogICAgICAgIHJldHVybiAgIFVzZXIodXNlcnNDb250cmFjdFtfdXNlcl0pLmdldENlcnRpZmljYXRlKCk7CiAgICB9CgogICAgZnVuY3Rpb24gZ2V0VXNlckxpc3QoKSAgcHVibGljIGNvbnN0YW50IHJldHVybnMgKGFkZHJlc3NbXSkgewogICAgICAgIHJldHVybiAgdXNlcmxpc3Q7CiAgICB9Cn0K"));
        creditContract.setSolRawString(bbbSource);
        creditContract.setContractAddress("0x646e909af2caae3bdfa78b069e0c3b52f569fd71");

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> sdkMap = mapper.readValue(sdkMapStr, Map.class);

        WebaseProjectFactory webaseProjectFactory = new WebaseProjectFactory();
        // ProjectArtifact result = projectFactory.buildProjectDir(Collections.singletonList(contractInfo),
        ProjectArtifact result = webaseProjectFactory
            .buildProjectDirWebase(Arrays.asList(contractInfo, creditContract),
            group, artifactName, outputDir, gradleDir,
            //null, null, null, null);
            "127.0.0.1:25200", 2, "0x123", sdkMap);
        System.out.println("result: ");
        System.out.println(result);

    }


//    @Test
//    public void testGenerateByString()
//        throws CodeGenException, IOException, ClassNotFoundException {
//        new SolidityContractWrapper()
//            .generateJavaFiles(
//                contractName,
//                binStr,
//                "",
//                abiStr,
//                outputDir,
//                "");
//
//    }
}
