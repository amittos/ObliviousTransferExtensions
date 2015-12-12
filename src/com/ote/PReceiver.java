package com.ote;

import edu.biu.scapi.comm.AuthenticatedChannel;
import edu.biu.scapi.comm.Channel;
import edu.biu.scapi.comm.twoPartyComm.*;
import edu.biu.scapi.exceptions.DuplicatePartyException;
import edu.biu.scapi.exceptions.InvalidDlogGroupException;
import edu.biu.scapi.exceptions.SecurityLevelException;
import edu.biu.scapi.interactiveMidProtocols.ot.OTOnByteArraySInput;
import edu.biu.scapi.interactiveMidProtocols.ot.oneSidedSimulation.OTOneSidedSimDDHOnByteArraySender;
import edu.biu.scapi.midLayer.symmetricCrypto.mac.ScCbcMacPrepending;
import edu.biu.scapi.primitives.prf.bc.BcAES;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class PReceiver {

    private int m, n, k, l;
    private byte[] choiceBits;
    private byte[][] k0Array; // Array which contains the keys made by the Receiver for the OT PHASE (Array of byte[])
    private byte[][] k1Array; // Array which contains the keys made by the Receiver for the OT PHASE (Array of byte[])
    private byte[][] t0Array; // Array which contains the results of the PRG for the OT EXTENSION PHASE (Array of byte[])
    private byte[][] t1Array; // Array which contains the results of the PRG for the OT EXTENSION PHASE (Array of byte[])
    private byte[][] uArray; // Array which contains the result of [G(k0) XOR G(k1) XOR choiceBits] to be sent to the Sender for the OT EXTENSION PHASE (Array of byte[])

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
    }

    // Overloaded Constructor
    public PReceiver(int m, int n, int k, int l) {

    }

    // Method to create the initial choice bits of size m
    public void setChoiceBits() {
        new SecureRandom().nextBytes(choiceBits);
    }

    // Method to print the initial choice bits of size m
    public void printChoiceBits() {
        int counter = 0;
        for (int i = 0; i < m; i++) {
            System.out.println(readBit(choiceBits, i));
            counter++;
        }
        System.out.println("Number of bits: " + counter);
    }

    // Method to read the value of a bit (0 or 1) of a byte array
    // http://stackoverflow.com/a/34095548/873309
    int readBit(byte[] b, int x) {
        int i = x / 8;
        int j = x % 8;
        return (b[i] >> j) & 1;
    }

    // OT PHASE
    // REMEMBER: During the OT PHASE the roles are inverted
    // The Sender acts as the Receicer and the Receiver as a Sender
    // Method to set the k0Array and the k1Array
    public void setKArray() {

        for (int i = 0; i < l; i++) {
            byte[] newByte = new byte[k/8]; // n = 128 bits, so 16 bytes (128/8)
            new Random().nextBytes(newByte);
            k0Array[i] = newByte;
        }

        for (int i = 0; i < l; i++) {
            byte[] newByte = new byte[k/8]; // n = 128 bits, so 16 bytes (128/8)
            new Random().nextBytes(newByte);
            k1Array[i] = newByte;
        }

    }

    // Method to print the kArray
    public void printKArray() {
        int counter = 0;
        for (int i = 0; i < l; i++) {
            System.out.println("Length: " + k0Array[i].length + ", " + Arrays.toString(k0Array[i]));
            counter++;
        }
        System.out.println("Number: " + counter);
        System.out.println("=======================");
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

    // Method to set the tArray
    public void setTArray() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, ShortBufferException {

        for (int i = 0; i < k0Array.length; i++) {
            t0Array[i] = AES_CTR_Generator(m, k0Array[i]);
        }

        for (int i = 0; i < k1Array.length; i++) {
            t1Array[i] = AES_CTR_Generator(m, k1Array[i]);
        }

    }

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

    // Method to print the tArray
    public void printTArray() {
        System.out.println("t0Array: ");
        for (int i = 0; i < t0Array.length; i++) {
            System.out.println("Length: " + t0Array[i].length + ", Output: " + Arrays.toString(t0Array[i]));
        }
        System.out.println("t1Array: ");
        for (int i = 0; i < t1Array.length; i++) {
            System.out.println("Length: " + t1Array[i].length + ", Output: " + Arrays.toString(t1Array[i]));
        }

    }

    // Method to set the uArray
    public void setUArray() {

        byte[] result;

        for (int i = 0; i < l; i++) {

            result = xorByteArrays(t0Array[i], t1Array[1]);
            uArray[i] = xorByteArrays(result, choiceBits);

        }

    }

    // Method to print the uArray
    public void printUArray() {
        System.out.println("\nU array:");
        for (int i = 0; i < l; i++) {
            System.out.println(Arrays.toString(uArray[i]));
        }
    }

    // Method to get the XOR result between two byte arrays
    static byte[] xorByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[Math.min(a.length, b.length)];

        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (((int) a[i]) ^ ((int) b[i]));
        }

        return result;

    }

    // Method for sending the uArray during the OT EXTENSION PHASE
    public void uArrayTransferSender() throws DuplicatePartyException, IOException, TimeoutException, SecurityLevelException, NoSuchAlgorithmException {

        PlainTCPSocketChannel plainChannel = plainChannelCreation(); // Create the channel object
        ScCbcMacPrepending mac = new ScCbcMacPrepending(new BcAES()); // Create the mac

        AuthenticatedChannel authChannel = new AuthenticatedChannel(plainChannel, mac);

        for (int i = 0; i < l; i++) {
            authChannel.send(uArray[i]);
            System.out.println("Success!");
        }

        authChannel.close();

    }

    // Method to create a plain channel
    public PlainTCPSocketChannel plainChannelCreation() throws DuplicatePartyException, TimeoutException {
        // Prepare the parties list.
        LoadSocketParties loadParties = new LoadSocketParties("SocketParties.properties");
        List<PartyData> listOfParties = loadParties.getPartiesList();

        TwoPartyCommunicationSetup commSetup = new SocketCommunicationSetup(listOfParties.get(1), listOfParties.get(0));

        // Call the prepareForCommunication function to establish one connection within 2000000 milliseconds.
        Map<String, Channel> connections = commSetup.prepareForCommunication(1, 2000000);

        // Return the channel to the calling application. There is only one created channel.
        return (PlainTCPSocketChannel) connections.values().toArray()[0];
    }

}




































