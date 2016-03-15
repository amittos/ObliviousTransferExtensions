/*

This file is part of OTExtentions.

OTExtentions is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License (AGPL)
v3.0 as published by the Free Software Foundation.

OTExtentions is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU Affero General Public License (AGPL) v3.0 for more details.

You should have received a copy of the  GNU Affero General Public
License (AGPL) v3.0 along with OTExtentions. If not, see
<http://www.gnu.org/licenses/agpl-3.0.txt>.


=====================================

    Author: Alexandros Mittos
    Year:   2016

=====================================

*/

package com.ote;

import edu.biu.scapi.exceptions.FactoriesException;
import edu.biu.scapi.primitives.prg.PseudorandomGenerator;
import edu.biu.scapi.tools.Factories.PrgFactory;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

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

    public static byte[] AES_PRG(int m, byte[] k) throws FactoriesException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, BadPaddingException, IllegalBlockSizeException {

        // http://stackoverflow.com/a/14204473/873309
        // Use the incoming byte array as the secret key
        SecretKey secretKey = new SecretKeySpec(k, 0, k.length, "AES");

        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        // Create a byte array of size m
        byte[] array = new byte[m];

        // Fill the byte array with random bytes
        new SecureRandom().nextBytes(array);

        byte[] outBytes = cipher.doFinal(array);

        return outBytes;

    }



    // Method to create a SHA1 sum of a byte array
    // http://stackoverflow.com/a/1515495/873309
    public static byte[] SHA1(byte[] convertMe) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return md.digest(convertMe);
    }

    public static byte[] SHA256(byte[] convertMe) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(convertMe);
    }

    public static byte[] SHA512(byte[] convertMe) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        return md.digest(convertMe);
    }



    // Method to create a MD5 sum of a byte array
    // http://stackoverflow.com/a/1515495/873309
    public static byte[] MD5(byte[] convertMe) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        return md.digest(convertMe);
    }

}

/*
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
