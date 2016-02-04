package com.ote;

import edu.biu.scapi.exceptions.FactoriesException;
import edu.biu.scapi.primitives.prg.PseudorandomGenerator;
import edu.biu.scapi.tools.Factories.PrgFactory;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.util.List;

public class GlobalMethods {

    // Pseudorandom Generator
    // SCAPI's implementation using RC4 (only RC4 is supported at the moment)
    // Uses the byte array as key and produces an outpout of size m
    // The reason this is "global" is because both the Sender and the Receiver use it
    // and they need to use the same instance.
    public static byte[] SCAPI_PRG(int m, byte[] k) throws FactoriesException, InvalidKeyException {

        // Create prg using the PrgFactory
        PseudorandomGenerator prg = PrgFactory.getInstance().getObject("RC4");

        // http://stackoverflow.com/a/14204473/873309
        // Use the incoming byte array as the secret key
        SecretKey secretKey = new SecretKeySpec(k, 0, k.length, "RC4");

        // set the key
        prg.setKey(secretKey);

        // Get PRG bytes. The caller is responsible for allocating the out array.
        // The result will be put in the out array.
        byte[] outBytes = new byte[m];
        prg.getPRGBytes(outBytes, 0, m);

        return outBytes;
    }

    // Method to convert ArrayList<Byte> into a byte[] (byte array)
    // http://stackoverflow.com/q/6860055/873309
    public static byte[] arrayListToByteArray(List<Byte> in) {
        final int n = in.size();
        byte ret[] = new byte[n];
        for (int i = 0; i < n; i++) {
            ret[i] = in.get(i);
        }
        return ret;
    }

    // Method to read the value of a bit (0 or 1) of a byte array
    // http://stackoverflow.com/a/34095548/873309
    public static int readBit(byte[] b, int x) {
        int i = x / 8;
        int j = x % 8;
        return (b[i] >> j) & 1;
    }


/*
    // Method for tests
    public static void testing() {

        //System.out.println("PSender: sArray[0]: " + readBit(PSender.sArray, 0));
        //System.out.println("PSender: qArray[0]: " +  Arrays.toString(PSender.qArray[0]));

        byte[] psender_qarray = PSender.qArray[0];
        byte[] psender_qjarray = PSender.qjArray[0];

        //System.out.println("PReceiver: choiceBits[0]: " + readBit(PReceiver.choiceBits, 0));
        //System.out.println("PReceiver: t0Array[0]: " +  Arrays.toString(PReceiver.t0Array[0]));

        byte[] preceiver_t0array = PReceiver.t0Array[0];
        byte[] preceiver_t0jarray = PReceiver.t0jArray[0];

        if (psender_qarray == preceiver_t0array) {
            System.out.println("TRUE: PSender.qArray[0] == PReceiver.t0Array[0]");
        } else {
            System.out.println("FALSE: PSender.qArray[0] =/= PReceiver.t0Array[0]");
        }

        if (psender_qjarray == preceiver_t0jarray) {
            System.out.println("TRUE: PSender.qjArray[0] == PReceiver.t0jArray[0]");
        } else {
            System.out.println("FALSE: PSender.qjArray[0] =/= PReceiver.t0jArray[0]");
        }

    }

    //INCORRECT FOR THE MOMENT
    // Method to return an encrypted message of size m using AES in Counter Mode
    // Why of size m? Because G: {0,1}^k -> {0,1}^m
    public byte[] AES_CTR_Generator(int m, byte[] k0) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, ShortBufferException {

        byte[] keyBytes = new byte[] { (byte) 0x36, (byte) 0xf1, (byte) 0x83,
                (byte) 0x57, (byte) 0xbe, (byte) 0x4d, (byte) 0xbd,
                (byte) 0x77, (byte) 0xf0, (byte) 0x50, (byte) 0x51,
                (byte) 0x5c, 0x73, (byte) 0xfc, (byte) 0xf9, (byte) 0xf2 };

        byte[] ivBytes = new byte[] { (byte) 0x69, (byte) 0xdd, (byte) 0xa8,
                (byte) 0x45, (byte) 0x5c, (byte) 0x7d, (byte) 0xd4,
                (byte) 0x25, (byte) 0x4b, (byte) 0xf3, (byte) 0x53,
                (byte) 0xb7, (byte) 0x73, (byte) 0x30, (byte) 0x4e, (byte) 0xec };

        // Initialisation
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

        // Mode
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

        return cipher.doFinal(k0);
    }
    */

}
