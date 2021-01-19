package sample.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/1/19 12:59
 */
public class CrcUtilTest {

    @Test
    public void crc16_ccitt() {
        String s =
                "f0450020a9360208d9930208db930208df930208e3930208e793020800000000000000000000000000000000eb930208ed93020800000000ef930208f1930208c3360208c3360208c3360208c3360208c3360208c3360208c3360208c3360208c3360208c3360208c3360208c3360208c33602086b6e0208c3360208c3360208115f0208635f0208c33602082d670208d5660208c3360208c3360208b53c0208c3360208c3360208c3360208c33602089170020837940208c3360208c3360208c3360208c3360208c3360208c3360208c3360208215c0208055f02088b3e0208c3360208c3360208c3360208c3360208c3360208c3360208c3360208c3360208c3360208015b0208c3360208c3360208336e0208c3360208c3360208c3360208c33602084f630208c3360208095b0208c3360208c3360208c3360208c3360208f9930208c3360208c3360208c3360208c33602089d630208c336020843630208c3360208c3360208c3360208c3360208c3360208c3360208c3360208c3360208c3360208c3360208dff80cd009f0bcfe0048004761940208f045002032490143324a1160704770b5002100230f22c4781cb32e4c246804f4e064c4f5e064c4f30721c1f10404e3b20a4144789c40e1b28478144021430c07210e254c05786155047804f01f050124ac4005786d11ad0005f1e025c5f8004109e0047804f01f050124ac401b4d0678761145f8264070bd194a0a400243154b1b1f1a60704739b1124a121d12680243104b1b1d1a6006e00e4a121d126882430c4b1b1d1a607047042808d14ff0e021096941f004014ff0e022116107e04ff0e021096921f004014ff0e022116170470000fa050ced00e000e400e080e100e080ffff1f10b50121080201f0e7fc00214ff4807001f0e2fc10bd30b50246002000235068f84c20400d790c6844ea0524204350609068f54c2040d1e903542c438d682c434d7944ea450420439060d06a20f470000c7d641ee4b2234340ea0350d06230bd00210160017141718160c16001610121017570470021e54a1168e54a1140d0e900231a4383681a43c3681a431143e14ac2f8041370470021016041608160c160704721b1826842f00102826003e0826822f00102826070470a4600214168d64b19401143416070474162826270470a460021416821f01f0111434160704738b1ca49096841f40001ca4ac2f8041306e0c649096821f40001c64ac2f80413704738b1c249096841f48001c24ac2f8041306e0be49096821f48001be4ac2f804137047f0b504460d4600200021092d11dde068a5f10a0606eb4607072606fa07f18843a5f10a0606eb460603fa06f10843e0600ce0206905eb4507072606fa07f1884305eb450603fa06f108432061072a0fda606b561e06eb86071f2606fa07f18843561e";
        byte[] bytes = HexUtils.hexString2bytes(s);

        int i = CrcUtil.crc16_ccitt(bytes, bytes.length);
        System.out.println(i);

    }
}
