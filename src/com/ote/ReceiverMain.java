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
import java.security.NoSuchProviderException;
import java.util.concurrent.TimeoutException;

public class ReceiverMain {

    public static void main(String[] args) throws ClassNotFoundException, DuplicatePartyException, TimeoutException, IOException, FactoriesException, InvalidKeyException, NoSuchAlgorithmException, InvalidDlogGroupException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, ShortBufferException, NoSuchPaddingException, NoSuchProviderException {

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

        //=====================================================
        Timer transpositionReceiver = Timer.start();
        //=====================================================

        Pr.setT0JArray();

        //=====================================================
        long transpositionReceiverSeconds = transpositionReceiver.nanoToSeconds();
        long transpositionReceiverMilliseconds = transpositionReceiver.nanoToMillis();
        //=====================================================

        //Pr.setT1JArray();
        //Pr.printTJArray();

        //=====================================================
        Timer transferYReceiver = Timer.start();
        //=====================================================

        Pr.yArrayTransferReceiver();

        //=====================================================
        long transferYReceiverSeconds = transferYReceiver.nanoToSeconds();
        long transferYReceiverMilliseconds = transferYReceiver.nanoToMillis();
        //=====================================================

        //Pr.printYArrays();

        Pr.getX();

        //Pr.getX_Cheat();
        //Pr.printCheatResults();

        //=====================================================
        long totalTimeSeconds = totalTimer_Receiver.nanoToSeconds();
        long totalTimeMilliseconds = totalTimer_Receiver.nanoToMillis();
        //=====================================================

        //Pr.printResults();

        //=====================================================
        System.out.println("\nTotal elapsed time: " + totalTimeSeconds + " seconds\nOr, " + totalTimeMilliseconds + " milliseconds");
        System.out.println("\nOblivious Transfer elapsed time: " + obliviousTransferSeconds + " seconds\nOr, " + obliviousTransferMilliseconds + " milliseconds");
        System.out.println("\nTransposition elapsed time: " + transpositionReceiverSeconds + " seconds\nOr, " + transpositionReceiverMilliseconds + " milliseconds");
        System.out.println("\nTransfer of Y elapsed time: " + transferYReceiverSeconds + " seconds\nOr, " + transferYReceiverMilliseconds + " milliseconds");
        //=====================================================

    }
}
