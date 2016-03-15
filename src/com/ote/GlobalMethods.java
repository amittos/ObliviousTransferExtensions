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

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    // Method to create a SHA1 sum of a byte array
    // http://stackoverflow.com/a/1515495/873309
    public static byte[] SHA1(byte[] convertMe) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return md.digest(convertMe);
    }

}

