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
        Timer obliviousTransferSender = Timer.start();
        //=====================================================

        Ps.obliviousTransferReceiver(); // Initiate the OT between the Sender and the Receiver. The OT will run l times.

        //=====================================================
        long obliviousTransferSeconds = obliviousTransferSender.nanoToSeconds();
        long obliviousTransferMilliseconds = obliviousTransferSender.nanoToMillis();
        //=====================================================

        //Ps.printKArray();

        //=====================================================
        //                  OT EXTENSION PHASE
        //=====================================================

        Ps.uArrayTransferReceiver();
        //Ps.printUArray();

        Ps.setQArray();
        //Ps.printQArray();

        //=====================================================
        Timer transpositionSender = Timer.start();
        //=====================================================

        Ps.setQjArray();
        //Ps.printQJArray();

        //=====================================================
        long transpositionSenderSeconds = transpositionSender.nanoToSeconds();
        long transpositionSenderMilliseconds = transpositionSender.nanoToMillis();
        //=====================================================

        //=====================================================
        Timer setYSender = Timer.start();
        //=====================================================

        Ps.setYArrays();

        //=====================================================
        long setYSenderSeconds = setYSender.nanoToSeconds();
        long setYSenderMilliseconds = setYSender.nanoToMillis();
        //=====================================================

        //Ps.printYArrays();

        //=====================================================
        Timer transferYSender = Timer.start();
        //=====================================================

        Ps.yArrayTransferSender();

        //=====================================================
        long transferYSenderSeconds = transferYSender.nanoToSeconds();
        long transferYSenderMilliseconds = transferYSender.nanoToMillis();
        //=====================================================

        //=====================================================
        long totalTimeSeconds = totalTimer_Sender.nanoToSeconds();
        long totalTimeMilliseconds = totalTimer_Sender.nanoToMillis();
        //=====================================================

        Ps.printXArray();

        //=====================================================
        System.out.println("\nTotal elapsed time: " + totalTimeSeconds + " seconds\nOr, " + totalTimeMilliseconds + " milliseconds");
        System.out.println("\nOblivious Transfer elapsed time: " + obliviousTransferSeconds + " seconds\nOr, " + obliviousTransferMilliseconds + " milliseconds");
        System.out.println("\nTransposition elapsed time: " + transpositionSenderSeconds + " seconds\nOr, " + transpositionSenderMilliseconds + " milliseconds");
        System.out.println("\nSet Y elapsed time: " + setYSenderSeconds + " seconds\nOr, " + setYSenderMilliseconds + " milliseconds");
        System.out.println("\nTransfer of Y elapsed time: " + transferYSenderSeconds + " seconds\nOr, " + transferYSenderMilliseconds + " milliseconds");
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
