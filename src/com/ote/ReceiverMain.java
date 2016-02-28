package com.ote;

import edu.biu.scapi.exceptions.DuplicatePartyException;
import edu.biu.scapi.exceptions.FactoriesException;
import edu.biu.scapi.exceptions.InvalidDlogGroupException;

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

    public static void main(String[] args) throws ClassNotFoundException, DuplicatePartyException, TimeoutException, IOException, FactoriesException, InvalidKeyException, NoSuchAlgorithmException, InvalidDlogGroupException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, ShortBufferException, NoSuchPaddingException {

        //=====================================================
        //                  INITIATION PHASE
        //=====================================================

        //=====================================================
        Timer totalTimer_Receiver = Timer.start();
        //=====================================================

        PReceiver Pr = new PReceiver();

        Pr.setChoiceBits();
        //Pr.printChoiceBits();

        //=====================================================
        //                      OT PHASE
        //=====================================================

        Pr.setKArray();
        //Pr.printKArray();

        //=====================================================
        Timer obliviousTransfer_Receiver = Timer.start();
        //=====================================================

        Pr.obliviousTransferSender(); // Initiate the OT between the Sender and the Receiver. The OT will run l times.

        //=====================================================
        long obliviousTransferSeconds = obliviousTransfer_Receiver.nanoToSeconds();
        long obliviousTransferMilliseconds = obliviousTransfer_Receiver.nanoToMillis();
        //=====================================================

        //=====================================================
        //                  OT EXTENSION PHASE
        //=====================================================

        Pr.setTArray();
        //Pr.printTArray();

        Pr.setUArray();
        //Pr.printUArray();

        Pr.uArrayTransferSender();

        Pr.setT0JArray();
        Pr.setT1JArray();
        //Pr.printTJArray();

        Pr.yArrayTransferReceiver();
        //Pr.printYArrays();

        Pr.getX();

        Pr.printResults();

        //Pr.getX_Cheat();
        //Pr.printCheatResults();

        //=====================================================
        long totalTimeSeconds = totalTimer_Receiver.nanoToSeconds();
        long totalTimeMilliseconds = totalTimer_Receiver.nanoToMillis();

        System.out.println("\nTotal elapsed time: " + totalTimeSeconds + " seconds\nOr, " + totalTimeMilliseconds + " milliseconds");
        System.out.println("\nOblivious Transfer elapsed time: " + obliviousTransferSeconds + " seconds\nOr, " + obliviousTransferMilliseconds + " milliseconds");
        //=====================================================

        //=====================================================
        //                  TESTING PHASE
        //=====================================================
        /*
        Pr.t0ArrayTransferSender();
        Pr.t0jArrayTransferSender();
        Pr.rArrayTransferSender();

        Pr.printChoiceBits();
        Pr.printT0JArray();
        */

    }
}
