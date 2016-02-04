package com.ote;

import edu.biu.scapi.comm.Channel;
import edu.biu.scapi.comm.twoPartyComm.LoadSocketParties;
import edu.biu.scapi.comm.twoPartyComm.PartyData;
import edu.biu.scapi.comm.twoPartyComm.SocketCommunicationSetup;
import edu.biu.scapi.comm.twoPartyComm.TwoPartyCommunicationSetup;
import edu.biu.scapi.exceptions.DuplicatePartyException;
import edu.biu.scapi.exceptions.FactoriesException;
import edu.biu.scapi.exceptions.InvalidDlogGroupException;
import edu.biu.scapi.interactiveMidProtocols.ot.OTOnByteArraySInput;
import edu.biu.scapi.interactiveMidProtocols.ot.oneSidedSimulation.OTOneSidedSimDDHOnByteArraySender;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class PReceiver implements Serializable {

    public static byte[] choiceBits;
    public static byte[][] t0Array; // Array which contains the results of the PRG for the OT EXTENSION PHASE (Array of byte[])
    public static byte[][] t1Array; // Array which contains the results of the PRG for the OT EXTENSION PHASE (Array of byte[])
    public static byte[][] t0jArray;
    public static byte[][] t1jArray;
    private int m, n, k, l;
    private byte[][] k0Array; // Array which contains the keys made by the Receiver for the OT PHASE (Array of byte[])
    private byte[][] k1Array; // Array which contains the keys made by the Receiver for the OT PHASE (Array of byte[])
    private byte[][] uArray; // Array which contains the result of [G(k0) XOR G(k1) XOR choiceBits] to be sent to the Sender for the OT EXTENSION PHASE (Array of byte[])
    private byte[][][] x;

    // Default Constructor
    public PReceiver() {
        m = 128;
        n = 128;
        k = 128;
        l = 128;
        choiceBits = new byte[m/8];
        k0Array = new byte[l][];
        k1Array = new byte[l][];
        t0Array = new byte[l][];
        t1Array = new byte[l][];
        uArray = new byte[l][];
        t0jArray = new byte[m][];
        t1jArray = new byte[m][];
    }

    // Overloaded Constructor
    public PReceiver(int m, int n, int k, int l) {

    }

    // Method for the OT channel creation between the Sender and the Receiver
    public static Channel obliviousTransferChannelCreation() throws IOException, DuplicatePartyException, TimeoutException {

        // Prepare the parties list.
        LoadSocketParties loadParties = new LoadSocketParties("SocketParties.properties");
        List<PartyData> listOfParties = loadParties.getPartiesList();

        TwoPartyCommunicationSetup commSetup = new SocketCommunicationSetup(listOfParties.get(1), listOfParties.get(0));

        // Call the prepareForCommunication function to establish one connection within 2000000 milliseconds.
        Map<String, Channel> connections = commSetup.prepareForCommunication(1, 2000000);

        // Return the channel to the calling application. There is only one created channel.
        return (Channel) connections.values().toArray()[0];
    }

    // Method to get the XOR result between two byte arrays
    static byte[] xorByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[Math.min(a.length, b.length)];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (((int) a[i]) ^ ((int) b[i]));
        }
        return result;
    }

    // Method to create the initial choice bits of size m
    public void setChoiceBits() {
        new SecureRandom().nextBytes(choiceBits);
    }

    // Method to print the initial choice bits of size m
    public void printChoiceBits() {

        System.out.println("These are the Choice Bits of PReceiver. To be used in the OT Extension Phase\n");

        int counter = 0;
        for (int i = 0; i < m; i++) {
            System.out.println(GlobalMethods.readBit(choiceBits, i));
            counter++;
        }
        System.out.println("Number of bits: " + counter);
    }

    // OT PHASE
    // REMEMBER: During the OT PHASE the roles are inverted
    // The Sender acts as the Receicer and the Receiver as a Sender
    // Method to set the k0Array and the k1Array
    public void setKArray() {

        for (int i = 0; i < l; i++) {
            byte[] newByte = new byte[k / 8]; // n = 128 bits, so 16 bytes (128/8)
            new Random().nextBytes(newByte);
            k0Array[i] = newByte;
        }

        for (int i = 0; i < l; i++) {
            byte[] newByte = new byte[k / 8]; // n = 128 bits, so 16 bytes (128/8)
            new Random().nextBytes(newByte);
            k1Array[i] = newByte;
        }

    }

    // Method to print the kArray
    public void printKArray() {

        System.out.println("\nBelow are the l pairs of PReceiver. \n\nk0Array:\n");

        int counter = 0;
        for (int i = 0; i < l; i++) {
            System.out.println("Length: " + k0Array[i].length + ", " + Arrays.toString(k0Array[i]));
            counter++;
        }
        System.out.println("Number: " + counter);
        System.out.println("\n=======================");

        System.out.println("\nk1Array:\n");

        counter = 0;
        for (int i = 0; i < l; i++) {
            System.out.println("Length: " + k1Array[i].length + ", " + Arrays.toString(k1Array[i]));
            counter++;
        }
        System.out.println("Number: " + counter);
    }

    // OT PHASE
    // REMEMBER: During the OT PHASE the roles are inverted
    // The Sender acts as the Receiver and the Receiver acts as the Sender
    // Method for the PReceiver to use OT as a Sender (OT PHASE). The method will run l times
    public void obliviousTransferSender() throws DuplicatePartyException, IOException, TimeoutException, ClassNotFoundException, InvalidDlogGroupException {

        System.out.println("\nOblivious Transfer starting now...\n");

        // Concrete implementation of the receiver side in oblivious transfer based on the DDH assumption that achieves
        // privacy for the case that the sender is corrupted and simulation in the case that the receiver is corrupted.
        OTOneSidedSimDDHOnByteArraySender sender = new OTOneSidedSimDDHOnByteArraySender();

        // Creates the two inputs
        byte[] x0 = null;
        byte[] x1 = null;

        // Run the transfer l times
        for (int i = 0; i < l; i++) {

            // Get the inputs from k0Array and k1Array
            x0 = k0Array[i];
            x1 = k1Array[i];

            OTOnByteArraySInput input = new OTOnByteArraySInput(x0, x1); // Create the input object
            Channel channel = obliviousTransferChannelCreation(); // Create the channel object
            sender.transfer(channel, input); // Send the inputs through the channel

        }
    }

    // Method to set the tArray
    public void setTArray() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, ShortBufferException, FactoriesException {

        for (int i = 0; i < k0Array.length; i++) {
            t0Array[i] = GlobalMethods.SCAPI_PRG(m, k0Array[i]);
        }

        for (int i = 0; i < k1Array.length; i++) {
            t1Array[i] = GlobalMethods.SCAPI_PRG(m, k1Array[i]);
        }

    }

    // Method to print the tArray
    public void printTArray() {

        System.out.println("\nBelow is the tArray which contains the G(k). \n\nt0Array:\n");

        for (int i = 0; i < t0Array.length; i++) {
            System.out.println("Length: " + t0Array[i].length + ", Output: " + Arrays.toString(t0Array[i]));
        }

        System.out.println("\nt1Array:\n");

        for (int i = 0; i < t1Array.length; i++) {
            System.out.println("Length: " + t1Array[i].length + ", Output: " + Arrays.toString(t1Array[i]));
        }

    }

    // Method to set the uArray
    // G(k0) XOR G(k1) XOR ChoiceBits
    // (all of size m)
    public void setUArray() {

        byte[] result;

        for (int i = 0; i < l; i++) {

            result = xorByteArrays(t0Array[i], t1Array[1]);
            uArray[i] = xorByteArrays(result, choiceBits);

        }

    }

    // Method to print the uArray
    public void printUArray() {

        System.out.println("\nBelow is the uArray which contains the resulf of: G(k0) XOR G(k1) XOR ChoiceBits \nTo be sent to PSender.\n");

        for (int i = 0; i < l; i++) {
            System.out.println("Length: " + uArray[i].length + ", Output: " + Arrays.toString(uArray[i]));
        }
    }

    // OT EXTENSION PHASE
    // Method for sending the uArray during the OT EXTENSION PHASE
    // This is a PLAIN channel. No security is required.
    public void uArrayTransferSender() throws TimeoutException, DuplicatePartyException, IOException {

        System.out.println("\nTransfer begins now...\n");

        Channel plainTCPChannel = plainTCPChannelCreation();

        for (int i = 0; i < l; i++) {
            plainTCPChannel.send(uArray[i]);
        }

        plainTCPChannel.close();

        System.out.println("\nTransfer completed.\n");

    }

    // Method to create a plain channel
    public Channel plainTCPChannelCreation() throws DuplicatePartyException, TimeoutException {

        // Prepare the parties list.
        LoadSocketParties loadParties = new LoadSocketParties("SocketParties.properties");
        List<PartyData> listOfParties = loadParties.getPartiesList();

        TwoPartyCommunicationSetup commSetup = new SocketCommunicationSetup(listOfParties.get(1), listOfParties.get(0));

        // Call the prepareForCommunication function to establish one connection within 2000000 milliseconds.
        Map<String, Channel> connections = commSetup.prepareForCommunication(1, 2000000);

        // Return the channel to the calling application. There is only one created channel.
        return (Channel) connections.values().toArray()[0];

    }

    // Method to create the tj array using ArrayList
    public void setT0JArray() throws IOException {

        ArrayList<Byte> arrayList = new ArrayList<>();

        int bitCounter = 0;
        int mCounter = 0;

        for (int j = 0; j < m; j++) {

            arrayList.clear();

            for (int i = 0; i < l; i++) {

                int x = (GlobalMethods.readBit(t0Array[i], bitCounter));
                arrayList.add((byte) x);

            }

            System.out.println("\nThe converted arrayList to a ByteArray is: " + GlobalMethods.arrayListToByteArray(arrayList));
            System.out.println("The length is: " + GlobalMethods.arrayListToByteArray(arrayList).length);
            System.out.println("The converted arrayList to a ByteArray is (in String format): " + Arrays.toString(GlobalMethods.arrayListToByteArray(arrayList)));

            t0jArray[mCounter] = GlobalMethods.arrayListToByteArray(arrayList);

            bitCounter++;
            mCounter++;

        }
    }

    // Method to create the tj array using ArrayList
    public void setT1JArray() throws IOException {

        ArrayList<Byte> arrayList = new ArrayList<>();

        int bitCounter = 0;
        int mCounter = 0;

        for (int j = 0; j < m; j++) {

            arrayList.clear();

            for (int i = 0; i < l; i++) {

                int x = (GlobalMethods.readBit(t1Array[i], bitCounter));
                arrayList.add((byte) x);

            }

            System.out.println("\nThe converted arrayList to a ByteArray is: " + GlobalMethods.arrayListToByteArray(arrayList));
            System.out.println("The length is: " + GlobalMethods.arrayListToByteArray(arrayList).length);
            System.out.println("The converted arrayList to a ByteArray is (in String format): " + Arrays.toString(GlobalMethods.arrayListToByteArray(arrayList)));

            t1jArray[mCounter] = GlobalMethods.arrayListToByteArray(arrayList);

            bitCounter++;
            mCounter++;

        }
    }

    // Method to print the tj array
    public void printTJArray() {

        System.out.println("\nBelow is the t0JArray:\n");

        for (int i = 0; i < m; i++) {
            System.out.println("Length: " + t0jArray[i].length);
            System.out.println("Output: " + t0jArray[i]);
            System.out.println("Output (String): " + Arrays.toString(t0jArray[i]));
        }


        System.out.println("\nBelow is the t1JArray:\n");

        for (int i = 0; i < m; i++) {
            System.out.println("Length: " + t1jArray[i].length);
            System.out.println("Output: " + t1jArray[i]);
            System.out.println("Output (String): " + Arrays.toString(t1jArray[i]));
        }

    }

    // TEST method
    public void test_printR0() {
        //System.out.println("This is a TEST method!");
        System.out.println("choiceBits[0] = " + GlobalMethods.readBit(choiceBits, 0));
    }

    // TEST method
    public void test_printT0() {
        //System.out.println("This is a TEST method!");
        System.out.println("This is the result of T0[0]: " + Arrays.toString(t0Array[0]));
    }

    // TEST method
    public void test_printTJArray() {
        //System.out.println("This is a TEST method!");
        System.out.println("This is the result of TJ[0]: " + Arrays.toString(t0jArray[0]));
        System.out.println("This is the result of TJ[1]: " + Arrays.toString(t0jArray[1]));
    }

}




































