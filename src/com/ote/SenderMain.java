package com.ote;

import edu.biu.scapi.exceptions.DuplicatePartyException;
import edu.biu.scapi.exceptions.FactoriesException;
import edu.biu.scapi.exceptions.SecurityLevelException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class SenderMain {
    public static void main(String[] args) throws DuplicatePartyException, IOException, TimeoutException, ClassNotFoundException, SecurityLevelException, NoSuchAlgorithmException, FactoriesException, InvalidKeyException {

        //=====================================================
        //                  INITIATION PHASE
        //=====================================================

        PSender Ps = new PSender(); // Create the object of the Sender

        //Ps.setXArray();
        //Ps.printXArray();

        //=====================================================
        //                      OT PHASE
        //=====================================================

        //Ps.setSArray();
        //Ps.printSArray();

        //Ps.obliviousTransferReceiver(); // Initiate the OT between the Sender and the Receiver. The OT will run l times.
        //Ps.printKArray();

        //=====================================================
        //                  OT EXTENSION PHASE
        //=====================================================

        //Ps.uArrayTransferReceiver();
        //Ps.printUArray();

        //Ps.setQArray();
        //Ps.printQArray();

        byte[] newByte = new byte[16]; // n = 128 bits, so 16 bytes (128/8)
        new Random().nextBytes(newByte);

        for (int i = 0; i < newByte.length*8; i++) {
            System.out.println("Pos: " + i + ", Bit: " + readBit(newByte, i));
        }


    }

    static int readBit(byte[] b, int x) {
        int i = x / 8;
        int j = x % 8;
        return (b[i] >> j) & 1;
    }

}
