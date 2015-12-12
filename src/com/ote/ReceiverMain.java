package com.ote;

import edu.biu.scapi.exceptions.DuplicatePartyException;
import edu.biu.scapi.exceptions.InvalidDlogGroupException;
import edu.biu.scapi.exceptions.SecurityLevelException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public class ReceiverMain {
    public static void main(String[] args) throws DuplicatePartyException, IOException, TimeoutException, ClassNotFoundException, InvalidDlogGroupException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, ShortBufferException, SecurityLevelException {

        //=====================================================
        //                  INITIATION PHASE
        //=====================================================

        PReceiver Pr = new PReceiver(); // Create the object of the Receiver

        Pr.setChoiceBits();
        //Pr.printChoiceBits();

        //=====================================================
        //                      OT PHASE
        //=====================================================

        Pr.setKArray();
        //Pr.printKArray();

        //Pr.obliviousTransferSender(); // Initiate the OT between the Sender and the Receiver. The OT will run l times.

        //=====================================================
        //                  OT EXTENSION PHASE
        //=====================================================

        Pr.setTArray();
        Pr.printTArray();

        //Pr.setUArray();
        //Pr.printUArray();

        //Pr.uArrayTransferSender();

    }
}
