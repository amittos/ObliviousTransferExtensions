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
import edu.biu.scapi.interactiveMidProtocols.ot.OTOnByteArrayROutput;
import edu.biu.scapi.interactiveMidProtocols.ot.OTRBasicInput;
import edu.biu.scapi.interactiveMidProtocols.ot.OTROutput;
import edu.biu.scapi.interactiveMidProtocols.ot.oneSidedSimulation.OTOneSidedSimDDHOnByteArrayReceiver;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class PSender {

    private int m, n, k, l;
    private byte[][] x0Array; // Array which contains the initial objects (Array of byte[])
    private byte[][] x1Array; // Array which contains the initial objects (Array of byte[])
    private byte[] sArray; // Choice bits for the OT PHASE
    private byte[][] qArray; // Array which contains the result of [(Si * Ui) XOR G(Ksi)] for the OT EXTENSION PHASE (Array of byte[])
    private byte[][] qjArray; // Array which contains the [...]
    private byte[][] kArray; // The array which contains the keys received via the OT PHASE (Array of byte[])
    private byte[][] uArray; // Array which contains the result of [G(k0) XOR G(k1) XOR choiceBits] to be sent to the Sender for the OT EXTENSION PHASE (Array of byte[])
    private byte[][] y0Array; // This array contains the result of [x0Array XOR H(qjArray)], to be sent to PReceiver. The size
    private byte[][] y1Array; // This array contains the result of [x1Array XOR (H(qjArray) XOR sArray)], to be sent to PReceiver

    //private byte[][] t0Array;
    //private byte[][] t0jArray;
    //private byte[] rArray;

    // Default Constructor
    public PSender() {
        m = 262144; // The number of the pairs of the Sender, the number of the choiceBits of the Receiver
        n = 160; // The size of each X in bits, also the size of the hash output. Must be the same in order to be XORed
        k = 128; // Security parameter
        l = 128;
        x0Array = new byte[m][]; // This array of byte[] has size m which means that it holds m inputs of size n
        x1Array = new byte[m][]; // This array of byte[] has size m which means that it holds m inputs of size n
        sArray = new byte[l / 8]; // The sArray has a size of l bits which means l/8 bytes
        kArray = new byte[l][]; // This array stores the keys received from Receiver. It has a size of l
        uArray = new byte[l][]; // This array stores the Ui [G(k0) XOR G(k1) XOR r] received from Receiver. It has a size of l
        qArray = new byte[l][]; // This array stores the Qi [(Si * Ui) XOR G(Ki^Si)]. It has a size of l
        qjArray = new byte[m][]; // This array is the transposition of the Qi array. It has a size of m because qArray would be l*m. Therefore the transposed table must have width of size m
        y0Array = new byte[m][]; // This array has a size of m because that's the size of the original array (xArray)
        y1Array = new byte[m][]; // This array has a size of m because that's the size of the original array (xArray)

        /*
        t0jArray = new byte[l][];
        rArray = new byte[l/8];
        t0Array = new byte[l][];
        */
    }

    // Overloaded Constructor
    public PSender(int m, int n, int k, int l) {

    }

    // Method for the OT channel creation between the Sender and the Receiver
    static Channel obliviousTransferChannelCreation() throws IOException, DuplicatePartyException, TimeoutException {

        // Prepare the parties list.
        LoadSocketParties loadParties = new LoadSocketParties("SocketParties.properties");
        List<PartyData> listOfParties = loadParties.getPartiesList();

        TwoPartyCommunicationSetup commSetup = new SocketCommunicationSetup(listOfParties.get(0), listOfParties.get(1));

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
    // http://stackoverflow.com/a/24487074/873309
    static byte[] xorByteArrays(byte[] a, byte[] b) {

        byte[] result = new byte[Math.min(a.length, b.length)];

        if (!(a.length == b.length)) {
            System.out.println("Lengths NOT equal");
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

    // Method to create randomly the initial objects for the Sender
    // (There's no need to use SecureRandom here)
    public void setXArray() {

        for (int i = 0; i < x0Array.length; i++) {
            byte[] newByte = new byte[n/8]; // n = 128 bits, so 16 bytes (128/8)
            new Random().nextBytes(newByte);
            x0Array[i] = newByte;
        }

        for (int i = 0; i < x1Array.length; i++) {
            byte[] newByte = new byte[n/8]; // n = 128 bits, so 16 bytes (128/8)
            new Random().nextBytes(newByte);
            x1Array[i] = newByte;
        }

    }

    // Method to printXArray which contains the initial objects of PSender
    public void printXArray() {
        System.out.println("\n=========================================================================================");
        System.out.println("\tBelow are the initial objects of PSender which will be sent to PReceiver");
        System.out.println("=========================================================================================\n");
        System.out.println("X0 Array:\n");

        for (int i = 0; i < x0Array.length; i++) {
            System.out.println("x0Array[" + i + "] = " + Arrays.toString(x0Array[i]));
        }

        System.out.println("\nX1 array:\n");

        for (int i = 0; i < x1Array.length; i++) {
            System.out.println("x1Array[" + i + "] = " + Arrays.toString(x1Array[i]));
        }
    }

    // Method to create the sArray of size l
    public void setSArray() {

        //new SecureRandom().nextBytes(sArray);
        //new Random().nextBytes(sArray);

        int[] array = new int[l];

        for (int i = 0; i < l; i++) {

            array[i] = 0;

        }

        sArray = intArrayToByteArray(array);

    }

    // Method to print the sArray of size l
    public void printSArray() {
        System.out.println("\n=========================================================================================");
        System.out.println("\tBelow are the choice bits created by PSender to use in the OT Phase.");
        System.out.println("=========================================================================================\n");
        int counter = 0;
        for (int i = 0; i < l; i++) {
            System.out.println(readBit(sArray, i));
            counter++;
        }
        System.out.println("Number of bits: " + counter);
        System.out.println("\nsArray Length: " + sArray.length);
        System.out.println("sArray: " + Arrays.toString(sArray));
    }

    // Method for the PSender to use OT as a receiver (OT PHASE). The method will run l times
    public void obliviousTransferReceiver() throws DuplicatePartyException, IOException, TimeoutException, ClassNotFoundException {

        // OT PHASE
        // REMEMBER: During the OT PHASE the roles are inverted
        // The Sender acts as the Receiver and the Receiver acts as the Sender
        System.out.println("\n=========================================================================================");
        System.out.println("\t\t\t\tOblivious Transfer for the OT phase starting now...");
        System.out.println("=========================================================================================\n");
        // Concrete implementation of the receiver side in oblivious transfer based on the DDH assumption that achieves
        // privacy for the case that the sender is corrupted and simulation in the case that the receiver is corrupted.
        OTOneSidedSimDDHOnByteArrayReceiver receiver = new OTOneSidedSimDDHOnByteArrayReceiver();
        OTROutput output = null; // Create the output in the general OTROutput format

        Channel channel = obliviousTransferChannelCreation(); // Create the channel

        for (int i = 0; i < l; i++) {

            //System.out.println("The bit is: " + readBit(sArray, i));
            byte sigma = (byte) readBit(sArray, i); // Reads the input for the receiver from the generated sArray

            // Create the input using sigma
            // Concrete implementation of OT receiver input.
            // In the basic scenario, the receiver gets a single bit representing 0/1.
            OTRBasicInput input = new OTRBasicInput(sigma);

            output = receiver.transfer(channel, input); // Get the output
            OTOnByteArrayROutput newOutput = (OTOnByteArrayROutput) output; // Convert the general OTROutput to the specific OTOnByteArrayROutput a.k.a. a byte array

            //System.out.println("Received throught OT: " + Arrays.toString(newOutput.getXSigma()));

            kArray[i] = newOutput.getXSigma(); // Store the keys to kArray

        }
        System.out.println("=========================================================================================");
        System.out.println("\t\t\t\t\t\tOblivious Transfer completed.");
        System.out.println("=========================================================================================");
    }

    // Method to print the kArray
    public void printKArray() {
        System.out.println("\n=========================================================================================");
        System.out.println("Below is the kArray which contains the objects obtained by PReceiver for the OT Phase");
        System.out.println("=========================================================================================\n");
        for (int i = 0; i < l; i++) {
            System.out.println("kArray length: " + kArray[i].length);
            System.out.println("kArray output: " + Arrays.toString(kArray[i]));
        }

    }

    // This is a PLAIN channel. No security is required.
    public void uArrayTransferReceiver() throws TimeoutException, DuplicatePartyException, IOException, ClassNotFoundException {

        // OT EXTENSION PHASE
        // Method for sending the uArray during the OT EXTENSION PHASE
        System.out.println("\n=========================================================================================");
        System.out.println("\t\t\t\t\t\tUArray Transfer begins now...");
        System.out.println("=========================================================================================\n");
        Channel plainTCPChannel = plainTCPChannelCreation();

        for (int i = 0; i < l; i++) {
            uArray[i] = (byte[]) plainTCPChannel.receive();
        }
        plainTCPChannel.close();
        System.out.println("\n=========================================================================================");
        System.out.println("\t\t\t\t\t\t\tTransfer completed.");
        System.out.println("=========================================================================================\n");
    }

    // Method to create a plain channel
    public Channel plainTCPChannelCreation() throws DuplicatePartyException, TimeoutException {

        // Prepare the parties list.
        LoadSocketParties loadParties = new LoadSocketParties("SocketParties.properties");
        List<PartyData> listOfParties = loadParties.getPartiesList();

        TwoPartyCommunicationSetup commSetup = new SocketCommunicationSetup(listOfParties.get(0), listOfParties.get(1));

        // Call the prepareForCommunication function to establish one connection within 2000000 milliseconds.
        Map<String, Channel> connections = commSetup.prepareForCommunication(1, 2000000);

        // Return the channel to the calling application. There is only one created channel.
        return (Channel) connections.values().toArray()[0];

    }

    // Method to print the uArray
    public void printUArray() {
        System.out.println("\n=========================================================================================");
        System.out.println("\t\tBelow is the uArray received by PReceiver via a plain channel");
        System.out.println("=========================================================================================\n");
        for (int i = 0; i < l; i++) {
            System.out.println("Length: " + uArray[i].length + ", Output: " + Arrays.toString(uArray[i]));
        }
    }

    // Method to set the qArray which contains the result of [(Si * Ui) XOR G(Ksi)]
    public void setQArray() throws InvalidKeyException, FactoriesException, NoSuchProviderException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException {

        // OT EXTENSION PHASE
        for (int i = 0; i < l; i++) {
            if (readBit(sArray, i) == 0) {
                qArray[i] = GlobalMethods.SCAPI_PRG(m / 8, kArray[i]);
            } else {
                qArray[i] = xorByteArrays(uArray[i], GlobalMethods.SCAPI_PRG(m / 8, kArray[i]));
            }
        }
    }

    // Method to print the qArray
    public void printQArray() {
        int counter = 0;
        System.out.println("\n=========================================================================================");
        System.out.println("\t\tBelow is the qArray which contains the result of [(Si * Ui) XOR G(Ksi)]");
        System.out.println("=========================================================================================\n");
        for (int i = 0; i < l; i++) {
            System.out.println(counter + ": Length: " + qArray[i].length + ", Output: " + Arrays.toString(qArray[i]));
            counter++;
        }
    }

    // Method to set the qj array using In-place matrix transposition
    // https://www.wikiwand.com/en/In-place_matrix_transposition
    public void setQjArray() throws IOException {

        int width = l; // Why l? l is the size of the qArray, meaning it contains l byte[]
        int height = m; // Why m? m is the size of each byte[] in bits, or m/8 in bytes

        int[][] array = new int[height][width]; // Make a two-dimensional int array to put each bit of the byte arrays

        // Transposition of the byte array to the int array
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                array[j][i] = readBit(qArray[i], j); // <------------------------------- REVERSED array[i][j] = readBit(qArray[j], i);
            }
        }

        // Convert each int array to byte array and insert it in qjArray
        for (int i = 0; i < m; i++) {
            qjArray[i] = intArrayToByteArray(array[i]);
        }
    }

    // Method to print the qj array
    public void printQJArray() {
        System.out.println("\n=========================================================================================");
        System.out.println("\t\t\t\t\t\t\tBelow is the qjArray:");
        System.out.println("=========================================================================================\n");
        for (int i = 0; i < m; i++) {
            System.out.println("Length: " + qjArray[i].length);
            System.out.println("Output: " + qjArray[i]);
            System.out.println("Output (String): " + Arrays.toString(qjArray[i]));
        }

    }

    // Method the set both yArrays (y0Array, y1Array)
    public void setYArrays() throws NoSuchAlgorithmException {

        for (int i = 0; i < m; i++) {
            y0Array[i] = xorByteArrays(x0Array[i], GlobalMethods.SHA1(qjArray[i]));
            y1Array[i] = xorByteArrays(x1Array[i], GlobalMethods.SHA1(xorByteArrays(sArray, qjArray[i])));
        }
    }

    // Method to print both yArrays
    public void printYArrays() {
        System.out.println("\n=========================================================================================");
        System.out.println("\t\t\t\t\t\t\tBelow are the yArrays:");
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

    // Method to transfer the yArrays to PReceiver
    public void yArrayTransferSender() throws TimeoutException, DuplicatePartyException, IOException {
        System.out.println("\n=========================================================================================");
        System.out.println("\t\t\t\t\tTransfer of the yArrays begins now...");
        System.out.println("=========================================================================================\n");
        Channel plainTCPChannel = plainTCPChannelCreation();

        for (int i = 0; i < m; i++) {
            plainTCPChannel.send(y0Array[i]);
        }

        for (int i = 0; i < m; i++) {
            plainTCPChannel.send(y1Array[i]);
        }

        plainTCPChannel.close();
        System.out.println("\n=========================================================================================");
        System.out.println("\t\t\t\t\t\t\tTransfer completed");
        System.out.println("=========================================================================================\n");

    }

    //========================================================================================================
    //                                              TEST AREA
    //========================================================================================================

    /*

    public void t0ArrayTransferReceiver() throws TimeoutException, DuplicatePartyException, IOException, ClassNotFoundException {

        // OT EXTENSION PHASE
        // Method for sending the uArray during the OT EXTENSION PHASE

        System.out.println("\nTransfer begins now...\n");
        Channel plainTCPChannel = plainTCPChannelCreation();

        for (int i = 0; i < l; i++) {
            t0Array[i] = (byte[]) plainTCPChannel.receive();
        }
        plainTCPChannel.close();
        System.out.println("\nTransfer completed.\n");
    }

    public void t0jArrayTransferReceiver() throws TimeoutException, DuplicatePartyException, IOException, ClassNotFoundException {

        // OT EXTENSION PHASE
        // Method for sending the uArray during the OT EXTENSION PHASE

        System.out.println("\nTransfer begins now...\n");
        Channel plainTCPChannel = plainTCPChannelCreation();

        for (int i = 0; i < l; i++) {
            t0jArray[i] = (byte[]) plainTCPChannel.receive();
        }
        plainTCPChannel.close();
        System.out.println("\nTransfer completed.\n");
    }

    public void printT0JArray() {

        System.out.println("\nt0jArray: \n");

        for (int i = 0; i < t0jArray.length; i++) {
            System.out.println("t0jArray[" + i + "] length = " + t0jArray[i].length);
            System.out.println("t0jArray[" + i + "] output = " + Arrays.toString(t0jArray[i]));
        }


    }

    public void printRArray() {

        System.out.println("These are the Choice Bits of PReceiver.\n");

        int counter = 0;
        for (int i = 0; i < m; i++) {
            System.out.println(readBit(rArray, i));
            counter++;
        }
        System.out.println("Number of bits: " + counter);

    }

    public void rArrayTransferReceiver() throws TimeoutException, DuplicatePartyException, IOException, ClassNotFoundException {

        // OT EXTENSION PHASE
        // Method for sending the uArray during the OT EXTENSION PHASE

        System.out.println("\nTransfer begins now...\n");
        Channel plainTCPChannel = plainTCPChannelCreation();

        rArray = (byte[]) plainTCPChannel.receive();

        plainTCPChannel.close();
        System.out.println("\nTransfer completed.\n");
    }

    public void testing() {

        System.out.println("==================================================");
        System.out.println("\t\t\t\tTESTING Qi");
        System.out.println("==================================================");

        for (int i = 0; i < l; i++) {

            if (readBit(sArray, i) == 0) {

                System.out.println("\nsArray[" + i + "] = " + readBit(sArray, i));
                System.out.println("So, qi = ti");

                if (Arrays.equals(qArray[i], t0Array[i])) {
                    System.out.println("qArray[" + i + "]: " + Arrays.toString(qArray[i]));
                    System.out.println("t0Array[" + i + "]: " + Arrays.toString(t0Array[i]));
                    System.out.println("TRUE");
                } else {
                    System.out.println("qArray[" + i + "]: " + Arrays.toString(qArray[i]));
                    System.out.println("t0Array[" + i + "]: " + Arrays.toString(t0Array[i]));
                    System.out.println("FALSE");
                }

            } else if (readBit(sArray, i) == 1) {

                System.out.println("\nsArray[" + i + "] = " + readBit(sArray, i));
                System.out.println("So, qi = r XOR ti");

                if (Arrays.equals(qArray[i], xorByteArrays(rArray, t0Array[i]))) {
                    System.out.println("rArray: " + Arrays.toString(rArray));
                    System.out.println("t0Array[" + i + "]: " + Arrays.toString(t0Array[i]));
                    System.out.println("qArray[" + i + "]: " + Arrays.toString(qArray[i]));
                    System.out.println("xorByteArrays(rArray, t0Array[i]): " + Arrays.toString(xorByteArrays(rArray, t0Array[i])));
                    System.out.println("TRUE");
                } else {
                    System.out.println("rArray: " + Arrays.toString(rArray));
                    System.out.println("t0Array[" + i + "]: " + Arrays.toString(t0Array[i]));
                    System.out.println("qArray[" + i + "]: " + Arrays.toString(qArray[i]));
                    System.out.println("xorByteArrays(rArray, t0Array[i]): " + Arrays.toString(xorByteArrays(rArray, t0Array[i])));
                    System.out.println("FALSE");
                }

            }

        }

        System.out.println("==================================================");
        System.out.println("\t\t\t\tTESTING QJ");
        System.out.println("==================================================");

        for (int i = 0; i < l; i++) {

            if (readBit(rArray, i) == 0) {

                System.out.println("\nrj = " + readBit(rArray, i));
                System.out.println("So, qj = tj");

                if (Arrays.equals(qjArray[i], t0jArray[i])) {
                    System.out.println("qjArray[" + i + "]: " + Arrays.toString(qjArray[i]));
                    System.out.println("t0jArray[" + i + "]: " + Arrays.toString(t0jArray[i]));
                    System.out.println("TRUE");
                } else {
                    System.out.println("qjArray[" + i + "]: " + Arrays.toString(qjArray[i]));
                    System.out.println("t0jArray[" + i + "]: " + Arrays.toString(t0jArray[i]));
                    System.out.println("FALSE");
                }

            } else if (readBit(rArray, i) == 1) {

                System.out.println("\nrj = " + readBit(rArray, i));
                System.out.println("So, qj = s XOR tj");

                if (Arrays.equals(xorByteArrays(sArray, t0jArray[i]), qjArray[i])) {
                    System.out.println("t0jArray[" + i + "]: " + Arrays.toString(t0jArray[i]));
                    System.out.println("sArray: " + Arrays.toString(sArray));
                    System.out.println("xorByteArrays(t0jArray[i], sArray)): " + Arrays.toString(xorByteArrays(t0jArray[i], sArray)));
                    System.out.println("qjArray[" + i + "]: " + Arrays.toString(qjArray[i]));
                    System.out.println("TRUE");
                } else {
                    System.out.println("t0jArray[" + i + "]: " + Arrays.toString(t0jArray[i]));
                    System.out.println("sArray: " + Arrays.toString(sArray));
                    System.out.println("xorByteArrays(t0jArray[i], sArray)): " + Arrays.toString(xorByteArrays(t0jArray[i], sArray)));
                    System.out.println("qjArray[" + i + "]: " + Arrays.toString(qjArray[i]));
                    System.out.println("FALSE");
                }

            }

        }

        System.out.println("==================================================");
        System.out.println("\t\t\t\tEND OF TEST");
        System.out.println("==================================================");

    }

    */

    //========================================================================================================
    //                                              TEST AREA
    //========================================================================================================

}













































