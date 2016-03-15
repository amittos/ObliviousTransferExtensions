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
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class PReceiver {

    private int m, n, k, l;
    private byte[] choiceBits; // Array which contains r = (r1, r2, r3, ..., rm)
    private byte[][] t0Array; // Array which contains the results of the PRG for the OT EXTENSION PHASE (Array of byte[])
    private byte[][] t1Array; // Array which contains the results of the PRG for the OT EXTENSION PHASE (Array of byte[])
    private byte[][] t0jArray;
    private byte[][] t1jArray;
    private byte[][] k0Array; // Array which contains the keys made by the Receiver for the OT PHASE (Array of byte[])
    private byte[][] k1Array; // Array which contains the keys made by the Receiver for the OT PHASE (Array of byte[])
    private byte[][] uArray; // Array which contains the result of [G(k0) XOR G(k1) XOR choiceBits] to be sent to the Sender for the OT EXTENSION PHASE (Array of byte[])
    private byte[][] y0Array; // This array contains the result of [x0Array XOR H(qjArray)], received by PSender to be decrypted
    private byte[][] y1Array; // This array contains the result of [x1Array XOR (H(qjArray) XOR sArray)], received by PSender to be decrypted
    private byte[][] xArray; // This array contains the answers after the decryption


    // Default Constructor
    public PReceiver() {

        m = 262144; // The number of the pairs of the Sender, the number of the choiceBits of the Receiver
        n = 160; // The size of each X in bits, also the size of the hash output. Must be the same in order to be XORed
        k = 128; // Security parameter
        l = 128;
        choiceBits = new byte[m / 8]; // We need m bits, so m/8 bytes
        k0Array = new byte[l][]; // The size of the array must be l since the Receiver builds l pairs
        k1Array = new byte[l][]; // The size of the array must be l since the Receiver builds l pairs
        t0Array = new byte[l][]; // The size of the array must be l since the Receiver builds l t0
        t1Array = new byte[l][]; // The size of the array must be l since the Receiver builds l t1
        uArray = new byte[l][]; // The size of the array must be l since the Receiver builds l Ui
        t0jArray = new byte[m][]; // This is the transposed t0Array. It has a size of m because t0Array would be l*m. Therefore the transposed table must have width of size m
        t1jArray = new byte[m][]; // This is the transposed t1Array. It has a size of m because t1Array would be l*m. Therefore the transposed table must have width of size m
        y0Array = new byte[m][]; // This array contains the encoded original messages. Of size m because m is the size of the original array
        y1Array = new byte[m][]; // This array contains the encoded original messages. Of size m because m is the size of the original array
        xArray = new byte[m][]; // This array contains the decrypted message, of size me because m is the size of the original array

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

    // Method to convert an int array into a byte array
    // http://stackoverflow.com/a/35346273/873309
    public static byte[] intArrayToByteArray(int[] bits) {

        // If the condition isn't satisfied, an AssertionError will be thrown.
        // The length MUST be divisible by 8.
        assert bits.length % 8 == 0;
        byte[] bytes = new byte[bits.length / 8];

        for (int i = 0; i < bytes.length; i++) {
            int b = 0;
            for (int j = 0; j < 8; j++)
                b = (b << 1) + bits[i * 8 + j];
            bytes[i] = (byte) b;
        }
        return bytes;
    }

    // Method to get the XOR result between two byte arrays
    static byte[] xorByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[Math.min(a.length, b.length)];

        if (!(a.length == b.length)) {
            System.out.println("\n=================");
            System.out.println("Lengths NOT equal");
            System.out.println("=================\n");
            System.exit(3);
        }

        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (((int) a[i]) ^ ((int) b[i]));
        }
        return result;
    }

    // Method to read the value of a bit (0 or 1) of a byte array
    // http://stackoverflow.com/a/34095548/873309
    public static int readBit(byte[] b, int x) {
        int i = x / 8;
        int j = x % 8;
        return (b[i] >> j) & 1;
    }

    // Method to create the initial choice bits of size m
    public void setChoiceBits() {
        new SecureRandom().nextBytes(choiceBits);
    }

    // Method to print the initial choice bits of size m
    public void printChoiceBits() {
        System.out.println("\n=========================================================================================");
        System.out.println("\t\t\t\t\tThese are the Choice Bits of PReceiver.");
        System.out.println("=========================================================================================\n");
        int counter = 0;
        for (int i = 0; i < m; i++) {
            System.out.println(readBit(choiceBits, i));
            counter++;
        }
        System.out.println("Number of bits: " + counter);
    }

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
        System.out.println("\n=========================================================================================");
        System.out.println("\t\t\t\tBelow are the l pairs of PReceiver.");
        System.out.println("=========================================================================================\n");

        System.out.println("K0 Array:\n");
        int counter = 0;
        for (int i = 0; i < l; i++) {
            System.out.println("Length: " + k0Array[i].length + ", " + Arrays.toString(k0Array[i]));
            counter++;
        }
        System.out.println("Number: " + counter);

        System.out.println("\nK1 Array:\n");

        counter = 0;
        for (int i = 0; i < l; i++) {
            System.out.println("Length: " + k1Array[i].length + ", " + Arrays.toString(k1Array[i]));
            counter++;
        }
        System.out.println("Number: " + counter);
    }

    // Method for the PReceiver to use OT as a Sender (OT PHASE). The method will run l times
    public void obliviousTransferSender() throws DuplicatePartyException, IOException, TimeoutException, ClassNotFoundException, InvalidDlogGroupException {

        // OT PHASE
        // REMEMBER: During the OT PHASE the roles are inverted
        // The Sender acts as the Receiver and the Receiver acts as the Sender
        System.out.println("\n=========================================================================================");
        System.out.println("\t\t\t\t\t\tOblivious Transfer starting now...");
        System.out.println("=========================================================================================\n");
        // Concrete implementation of the receiver side in oblivious transfer based on the DDH assumption that achieves
        // privacy for the case that the sender is corrupted and simulation in the case that the receiver is corrupted.
        OTOneSidedSimDDHOnByteArraySender sender = new OTOneSidedSimDDHOnByteArraySender();

        Channel channel = obliviousTransferChannelCreation(); // Create the channel object

        // Creates the two inputs
        byte[] x0 = null;
        byte[] x1 = null;

        // Run the transfer l times
        for (int i = 0; i < l; i++) {

            // Get the inputs from k0Array and k1Array
            x0 = k0Array[i];
            x1 = k1Array[i];

            OTOnByteArraySInput input = new OTOnByteArraySInput(x0, x1); // Create the input object
            sender.transfer(channel, input); // Send the inputs through the channel

        }

        System.out.println("\n=========================================================================================");
        System.out.println("\t\t\t\t\t\tOblivious Transfer completed");
        System.out.println("=========================================================================================\n");
    }

    // Method to set the tArray
    public void setTArray() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, ShortBufferException, FactoriesException, NoSuchProviderException {

        for (int i = 0; i < k0Array.length; i++) {
            t0Array[i] = GlobalMethods.SCAPI_PRG(m / 8, k0Array[i]);
        }

        for (int i = 0; i < k1Array.length; i++) {
            t1Array[i] = GlobalMethods.SCAPI_PRG(m / 8, k1Array[i]);
        }

    }

    // Method to print the tArray
    public void printTArray() {

        int counter = 0;
        System.out.println("\n=========================================================================================");
        System.out.println("\t\t\t\tBelow is the tArray which contains the G(k)");
        System.out.println("=========================================================================================\n");

        System.out.println("\nT0 Array:\n");

        for (int i = 0; i < t0Array.length; i++) {
            System.out.println(counter + ": Length: " + t0Array[i].length + ", Output: " + Arrays.toString(t0Array[i]));
            counter++;
        }

        System.out.println("\nT1 Array:\n");
        counter = 0;

        for (int i = 0; i < t1Array.length; i++) {
            System.out.println(counter + ": Length: " + t1Array[i].length + ", Output: " + Arrays.toString(t1Array[i]));
            counter++;
        }

    }

    // Method to set the uArray
    // G(k0) XOR G(k1) XOR ChoiceBits
    // (all of size m)
    public void setUArray() {
        for (int i = 0; i < l; i++) {
            uArray[i] = xorByteArrays(choiceBits, xorByteArrays(t0Array[i], t1Array[i]));
        }
    }

    // Method to print the uArray
    public void printUArray() {
        System.out.println("\n=========================================================================================");
        System.out.println("\tBelow is the uArray which contains the result of: G(k0) XOR G(k1) XOR ChoiceBits");
        System.out.println("=========================================================================================\n");
        for (int i = 0; i < l; i++) {
            System.out.println("Length: " + uArray[i].length + ", Output: " + Arrays.toString(uArray[i]));
        }
    }

    // This is a PLAIN channel. No security is required.
    public void uArrayTransferSender() throws TimeoutException, DuplicatePartyException, IOException {

        // OT EXTENSION PHASE
        // Method for sending the uArray during the OT EXTENSION PHASE
        System.out.println("\n=========================================================================================");
        System.out.println("\t\t\t\t\t\t\t\tTransfer begins now...");
        System.out.println("==========================================================================================\n");

        Channel plainTCPChannel = plainTCPChannelCreation();

        for (int i = 0; i < l; i++) {
            plainTCPChannel.send(uArray[i]);
        }

        plainTCPChannel.close();
        System.out.println("\n=========================================================================================");
        System.out.println("\t\t\t\t\t\t\t\tTransfer completed.");
        System.out.println("=========================================================================================\n");
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

    // Method to set the tj array using In-place matrix transposition
    // https://www.wikiwand.com/en/In-place_matrix_transposition
    public void setT0JArray() throws IOException {

        int width = l; // Why l? l is the size of the qArray, meaning it contains l byte[]
        int height = m; // Why m? m is the size of each byte[] in bits, or m/8 in bytes

        int[][] array = new int[height][width]; // Make a two-dimensional int array to put each bit of the byte arrays

        // Transposition of the byte array to the int array
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                array[j][i] = readBit(t0Array[i], j);
            }
        }

        // Convert each int array to byte array and insert it in qjArray
        for (int i = 0; i < m; i++) {
            t0jArray[i] = intArrayToByteArray(array[i]);
        }

    }

    // Method to set the tj array using In-place matrix transposition
    // https://www.wikiwand.com/en/In-place_matrix_transposition
    public void setT1JArray() throws IOException {

        int width = l; // Why l? l is the size of the qArray, meaning it contains l byte[]
        int height = m; // Why m? m is the size of each byte[] in bits, or m/8 in bytes

        int[][] array = new int[height][width]; // Make a two-dimensional int array to put each bit of the byte arrays

        // Transposition of the byte array to the int array
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                array[j][i] = readBit(t1Array[i], j);
            }
        }

        // Convert each int array to byte array and insert it in qjArray
        for (int i = 0; i < m; i++) {
            t1jArray[i] = intArrayToByteArray(array[i]);
        }

    }

    // Method to print the tj array
    public void printTJArray() {
        System.out.println("\n=========================================================================================");
        System.out.println("\t\t\t\t\t\t\tBelow is the t0JArray:");
        System.out.println("=========================================================================================\n");
        for (int i = 0; i < m; i++) {
            System.out.println("Length: " + t0jArray[i].length);
            System.out.println("Output: " + t0jArray[i]);
            System.out.println("Output (String): " + Arrays.toString(t0jArray[i]));
        }

        System.out.println("\n=========================================================================================");
        System.out.println("\t\t\t\t\t\t\tBelow is the t1JArray:");
        System.out.println("=========================================================================================\n");

        for (int i = 0; i < m; i++) {
            System.out.println("Length: " + t1jArray[i].length);
            System.out.println("Output: " + t1jArray[i]);
            System.out.println("Output (String): " + Arrays.toString(t1jArray[i]));
        }

    }

    // This is a PLAIN channel. No security is required.
    public void yArrayTransferReceiver() throws TimeoutException, DuplicatePartyException, IOException, ClassNotFoundException {
        System.out.println("\n=========================================================================================");
        System.out.println("\t\t\t\t\t\tTransfer of yArrays begins now...");
        System.out.println("=========================================================================================\n");
        Channel plainTCPChannel = plainTCPChannelCreation();

        for (int i = 0; i < m; i++) {
            y0Array[i] = (byte[]) plainTCPChannel.receive();
        }

        for (int i = 0; i < m; i++) {
            y1Array[i] = (byte[]) plainTCPChannel.receive();
        }

        plainTCPChannel.close();
        System.out.println("\n=========================================================================================");
        System.out.println("\t\t\t\t\t\t\t\tTransfer completed.");
        System.out.println("=========================================================================================\n");
    }

    // Method to print both yArrays
    public void printYArrays() {
        System.out.println("\n=========================================================================================");
        System.out.println("\t\t\t\t\t\t\t\tBelow are the yArrays:");
        System.out.println("=========================================================================================\n");
        System.out.println("Y0 Array:\n");
        for (int i = 0; i < m; i++) {
            System.out.println("Length: " + y0Array[i].length + ", Output: " + Arrays.toString(y0Array[i]));
        }
        System.out.println("\nY1 Array:\n");
        for (int i = 0; i < m; i++) {
            System.out.println("Length: " + y1Array[i].length + ", Output: " + Arrays.toString(y1Array[i]));
        }

    }

    // Method to decrypt the yArrays
    public void getX() throws NoSuchAlgorithmException {

        for (int i = 0; i < m; i++) {
            if (readBit(choiceBits, i) == 0) {
                xArray[i] = xorByteArrays(y0Array[i], GlobalMethods.SHA1(t0jArray[i]));
            } else {
                xArray[i] = xorByteArrays(y1Array[i], GlobalMethods.SHA1(t0jArray[i]));
            }
        }

    }

    // Method to print the results
    public void printResults() {
        System.out.println("\n=========================================================================================");
        System.out.println("=========================================================================================");
        System.out.println("\n\t\t\t\t\t\t\tBelow are the final results:\n");
        System.out.println("=========================================================================================");
        System.out.println("=========================================================================================\n");

        for (int i = 0; i < m; i++) {

            System.out.println(readBit(choiceBits, i) + ": xArray[" + i + "] = " + Arrays.toString(xArray[i]));

        }
        System.out.println("\n=========================================================================================");
        System.out.println("=========================================================================================");
    }


}




































