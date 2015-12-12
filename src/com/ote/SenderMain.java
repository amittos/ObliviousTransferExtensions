package com.ote;

import edu.biu.scapi.exceptions.DuplicatePartyException;
import edu.biu.scapi.exceptions.SecurityLevelException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public class SenderMain {
    public static void main(String[] args) throws DuplicatePartyException, IOException, TimeoutException, ClassNotFoundException, SecurityLevelException, NoSuchAlgorithmException {

        //=====================================================
        //                  INITIATION PHASE
        //=====================================================

        PSender Ps = new PSender(); // Create the object of the Sender

        Ps.setXArray();
        //Ps.printXArray();

        //=====================================================
        //                      OT PHASE
        //=====================================================

        Ps.setSArray();
        //Ps.printSArray();

        //Ps.obliviousTransferReceiver(); // Initiate the OT between the Sender and the Receiver. The OT will run l times.
        //Ps.printKArray();

        //=====================================================
        //                  OT EXTENSION PHASE
        //=====================================================

        Ps.uArrayTransferReceiver();
        Ps.printUArray();


    }
}
