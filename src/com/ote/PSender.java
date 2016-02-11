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

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class PSender {

    private int m, n, k, l;
    private byte[] sArray; // Choice bits for the OT PHASE
    private byte[][] qArray; // Array which contains the result of [(Si * Ui) XOR G(Ksi)] for the OT EXTENSION PHASE (Array of byte[])
    private byte[][] qjArray; // Array which contains the [...]
    private byte[][] x0Array; // Array which contains the initial objects (Array of byte[])
    private byte[][] x1Array; // Array which contains the initial objects (Array of byte[])
    private byte[][] kArray; // The array which contains the keys received via the OT PHASE (Array of byte[])
    private byte[][] uArray; // Array which contains the result of [G(k0) XOR G(k1) XOR choiceBits] to be sent to the Sender for the OT EXTENSION PHASE (Array of byte[])
    private byte[][] y0Array; // This array contains the result of [x0Array XOR H(qjArray)], to be sent to PReceiver. The size
    private byte[][] y1Array; // This array contains the result of [x1Array XOR (H(qjArray) XOR sArray)], to be sent to PReceiver

    // Default Constructor
    public PSender() {
        m = 128;
        n = 160;
        k = 128;
        l = 128;
        x0Array = new byte[m][];
        x1Array = new byte[m][];
        sArray = new byte[l / 8]; // We need l bits, so l/8 bytes
        kArray = new byte[l][];
        uArray = new byte[l][];
        qArray = new byte[l][];
        qjArray = new byte[m][];
        y0Array = new byte[m][];
        y1Array = new byte[m][];
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
    static byte[] xorByteArrays(byte[] a, byte[] b) {

        byte[] result = new byte[Math.min(a.length, b.length)];

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

        System.out.println("Below are the initial objects of PSender which will be sent to PReceiver \n\nX0 array:\n");

        for (int i = 0; i < x0Array.length; i++) {
            System.out.println(Arrays.toString(x0Array[i]));
        }

        System.out.println("\nX1 array:\n");

        for (int i = 0; i < x1Array.length; i++) {
            System.out.println(Arrays.toString(x1Array[i]));
        }
    }

    // Method to get String from byte array
    // http://stackoverflow.com/questions/6684665/java-byte-array-to-string-to-byte-array
    public String getStringFromBytesArray(byte[] output) {

        String response = Arrays.toString(output);
        String[] byteValues = response.substring(1, response.length() - 1).split(",");
        byte[] bytes = new byte[byteValues.length];
        for (int i = 0, len = bytes.length; i < len; i++) {
            bytes[i] = Byte.parseByte(byteValues[i].trim());
        }

        String result = new String(bytes);

        return result;
    }

    // OT PHASE
    // REMEMBER: During the OT PHASE the roles are inverted
    // The Sender acts as the Receiver and the Receiver as a Sender
    // Method to create the sArray of size l
    public void setSArray() {
        new SecureRandom().nextBytes(sArray);
    }

    // Method to print the sArray of size l
    public void printSArray() {

        System.out.println("\nBelow are the choice bits created by PSender to use in the OT Phase.\n");

        int counter = 0;
        for (int i = 0; i < l; i++) {
            System.out.println(readBit(sArray, i));
            counter++;
        }
        System.out.println("Number of bits: " + counter);
    }

    // OT PHASE
    // REMEMBER: During the OT PHASE the roles are inverted
    // The Sender acts as the Receiver and the Receiver acts as the Sender
    // Method for the PSender to use OT as a receiver (OT PHASE). The method will run l times
    public void obliviousTransferReceiver() throws DuplicatePartyException, IOException, TimeoutException, ClassNotFoundException {

        System.out.println("\nOblivious Transfer starting now...\n");

        // Concrete implementation of the receiver side in oblivious transfer based on the DDH assumption that achieves
        // privacy for the case that the sender is corrupted and simulation in the case that the receiver is corrupted.
        OTOneSidedSimDDHOnByteArrayReceiver receiver = new OTOneSidedSimDDHOnByteArrayReceiver();
        OTROutput output = null; // Create the output in the general OTROutput format

        for (int i = 0; i < l; i++) {

            //System.out.println("The bit is: " + readBit(sArray, i));
            byte sigma = (byte) readBit(sArray, i); // Reads the input for the receiver from the generated sArray

            // Create the input using sigma
            // Concrete implementation of OT receiver input.
            // In the basic scenario, the receiver gets a single bit representing 0/1.
            OTRBasicInput input = new OTRBasicInput(sigma);

            Channel channel = obliviousTransferChannelCreation(); // Create the channel

            output = receiver.transfer(channel, input); // Get the output
            OTOnByteArrayROutput newOutput = (OTOnByteArrayROutput) output; // Convert the general OTROutput to the specific OTOnByteArrayROutput a.k.a. a byte array

            //System.out.println("Received throught OT: " + Arrays.toString(newOutput.getXSigma()));

            kArray[i] = newOutput.getXSigma(); // Store the keys to kArray

        }
    }

    // Method to print the kArray
    public void printKArray() {
        System.out.println("\nBelow is the kArray which contains the objects obtained by PReceiver for the OT Phase\n");
        for (int i = 0; i < l; i++) {
            System.out.println("kArray length: " + kArray[i].length);
            System.out.println("kArray output: " + Arrays.toString(kArray[i]));
        }

    }

    // OT EXTENSION PHASE
    // Method for sending the uArray during the OT EXTENSION PHASE
    // This is a PLAIN channel. No security is required.
    public void uArrayTransferReceiver() throws TimeoutException, DuplicatePartyException, IOException, ClassNotFoundException {

        System.out.println("\nTransfer begins now...\n");
        Channel plainTCPChannel = plainTCPChannelCreation();

        for (int i = 0; i < l; i++) {
            uArray[i] = (byte[]) plainTCPChannel.receive();
        }
        plainTCPChannel.close();
        System.out.println("\nTransfer completed.\n");
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
        System.out.println("\nBelow is the uArray [G(k0) XOR G(k1) XOR ChoiceBits] which is received by PReveiver via a plain channel\n");
        for (int i = 0; i < l; i++) {
            System.out.println("Length: " + uArray[i].length + ", Output: " + Arrays.toString(uArray[i]));
        }
    }

    // OT EXTENSION PHASE
    // Method to set the qArray which contains the result of [(Si * Ui) XOR G(Ksi)]
    public void setQArray() throws InvalidKeyException, FactoriesException {

        for (int i = 0; i < m; i++) {
            if (readBit(sArray, i) == 0) {
                //System.out.println("sArray[" + i + "]: " + sArray[i]);
                //System.out.println("qArray[i] = SCAPI_PRG(m, kArray[i])");
                qArray[i] = GlobalMethods.SCAPI_PRG(m / 8, kArray[i]);
            } else {
                //System.out.println("sArray[" + i + "]: " + sArray[i]);
                //System.out.println("qArray[i] = uArray[i] XOR SCAPI_PRG(m, kArray[i])");
                qArray[i] = xorByteArrays(uArray[i], GlobalMethods.SCAPI_PRG(m / 8, kArray[i]));
            }
        }
    }

    // Method to print the qArray
    public void printQArray() {
        int counter = 0;
        System.out.println("\nBelow is the qArray which contains the result of [(Si * Ui) XOR G(Ksi)]\n");
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
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                array[y][x] = readBit(qArray[x], y);
            }
        }

        // Convert each int array to byte array and insert it in qjArray
        for (int i = 0; i < l; i++) {
            qjArray[i] = intArrayToByteArray(array[i]);
        }
    }

    // Method to print the qj array
    public void printQJArray() {
        System.out.println("\nBelow is the qjArray:\n");
        for (int i = 0; i < m; i++) {
            System.out.println("Length: " + qjArray[i].length);
            System.out.println("Output: " + qjArray[i]);
            System.out.println("Output (String): " + Arrays.toString(qjArray[i]));
        }

    }

    // Method the set both yArrays (y0Array, y1Array)
    public void setYArrays() throws NoSuchAlgorithmException {

        byte[] result;

        // Set y0Array
        for (int i = 0; i < m; i++) {

            System.out.println("\n\n=============================================");

            System.out.println("x0Array length: " + x0Array[i].length);
            System.out.println("x0Array output: " + Arrays.toString(x0Array[i]));

            System.out.println("qjArray length: " + qjArray[i].length);
            System.out.println("qjArray output: " + Arrays.toString(qjArray[i]));

            System.out.println("SHA1 length: " + GlobalMethods.SHA1(qjArray[i]).length);
            System.out.println("SHA1 output: " + Arrays.toString(GlobalMethods.SHA1(qjArray[i])));

            y0Array[i] = xorByteArrays(x0Array[i], GlobalMethods.SHA1(qjArray[i]));

            System.out.println("y0Array length: " + y0Array[i].length);
            System.out.println("y0Array output: " + Arrays.toString(y0Array[i]));

            System.out.println("\n=============================================");

        }

        // Set y1Array
        for (int i = 0; i < m; i++) {

            System.out.println("\n\n=============================================\n");

            System.out.println("qjArray length: " + qjArray[i].length);
            System.out.println("qjArray output: " + Arrays.toString(qjArray[i]));

            System.out.println("sArray length: " + sArray.length);
            System.out.println("sArray output: " + Arrays.toString(sArray));

            result = xorByteArrays(qjArray[i], sArray);

            System.out.println("result length: " + result.length);
            System.out.println("result output: " + Arrays.toString(result));

            System.out.println("x1Array length: " + x1Array[i].length);
            System.out.println("x1Array output: " + Arrays.toString(x1Array[i]));

            System.out.println("SHA1 length: " + GlobalMethods.SHA1(result).length);
            System.out.println("SHA1 output: " + Arrays.toString(GlobalMethods.SHA1(result)));

            y1Array[i] = xorByteArrays(x1Array[i], GlobalMethods.SHA1(result));

            System.out.println("y1Array length: " + y1Array[i].length);
            System.out.println("y1Array output: " + Arrays.toString(y1Array[i]));

            System.out.println("\n=============================================");

        }

    }

    // Method to print both yArrays
    public void printYArrays() {
        System.out.println("Below are the yArrays:");
        System.out.println("\ny0Array:\n");
        for (int i = 0; i < m; i++) {
            System.out.println("Length: " + y0Array[i].length + ", Output: " + Arrays.toString(y0Array[i]));
        }
        System.out.println("\ny1Array:\n");
        for (int i = 0; i < m; i++) {
            System.out.println("Length: " + y1Array[i].length + ", Output: " + Arrays.toString(y1Array[i]));
        }

    }

    // Method to transfer the yArrays to PReceiver
    public void yArrayTransferSender() throws TimeoutException, DuplicatePartyException, IOException {

        System.out.println("\nTransfer of the yArrays begins now...\n");
        Channel plainTCPChannel = plainTCPChannelCreation();

        for (int i = 0; i < m; i++) {
            plainTCPChannel.send(y0Array[i]);
        }

        for (int i = 0; i < m; i++) {
            plainTCPChannel.send(y1Array[i]);
        }

        plainTCPChannel.close();
        System.out.println("\nTransfer completed.\n");

    }

}













































