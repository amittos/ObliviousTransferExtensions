package com.ote;

import edu.biu.scapi.exceptions.DuplicatePartyException;
import edu.biu.scapi.exceptions.FactoriesException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public class SenderMain {

    public static void main(String[] args) throws ClassNotFoundException, DuplicatePartyException, TimeoutException, IOException, FactoriesException, InvalidKeyException, NoSuchAlgorithmException {

        //=====================================================
        //                  INITIATION PHASE
        //=====================================================

        //=====================================================
        Timer totalTimer_Sender = Timer.start();
        //=====================================================

        PSender Ps = new PSender();

        Ps.setXArray();
        //Ps.printXArray();

        //=====================================================
        //                      OT PHASE
        //=====================================================

        Ps.setSArray();
        //Ps.printSArray();

        //=====================================================
        Timer obliviousTransfer_Sender = Timer.start();
        //=====================================================

        Ps.obliviousTransferReceiver(); // Initiate the OT between the Sender and the Receiver. The OT will run l times.

        //=====================================================
        long obliviousTransferSeconds = obliviousTransfer_Sender.nanoToSeconds();
        long obliviousTransferMilliseconds = obliviousTransfer_Sender.nanoToMillis();
        //=====================================================

        //Ps.printKArray();

        //=====================================================
        //                  OT EXTENSION PHASE
        //=====================================================

        Ps.uArrayTransferReceiver();
        //Ps.printUArray();

        Ps.setQArray();
        //Ps.printQArray();

        Ps.setQjArray();
        //Ps.printQJArray();

        Ps.setYArrays();
        //Ps.printYArrays();

        Ps.yArrayTransferSender();
        Ps.printXArray();

        //=====================================================
        long totalTimeSeconds = totalTimer_Sender.nanoToSeconds();
        long totalTimeMilliseconds = totalTimer_Sender.nanoToMillis();

        System.out.println("\nTotal elapsed time: " + totalTimeSeconds + " seconds\nOr, " + totalTimeMilliseconds + " milliseconds");
        System.out.println("\nOblivious Transfer elapsed time: " + obliviousTransferSeconds + " seconds\nOr, " + obliviousTransferMilliseconds + " milliseconds");
        //=====================================================

        //=====================================================
        //                  TESTING PHASE
        //=====================================================
        /*
        Ps.t0ArrayTransferReceiver();
        Ps.t0jArrayTransferReceiver();
        Ps.rArrayTransferReceiver();
        Ps.printRArray();
        Ps.printT0JArray();
        Ps.testing();
        */


    }
}
