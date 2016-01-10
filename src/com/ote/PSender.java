package com.ote;

import edu.biu.scapi.comm.AuthenticatedChannel;
import edu.biu.scapi.comm.Channel;
import edu.biu.scapi.comm.EncryptedChannel;
import edu.biu.scapi.comm.PlainTCPChannel;
import edu.biu.scapi.comm.twoPartyComm.*;
import edu.biu.scapi.exceptions.DuplicatePartyException;
import edu.biu.scapi.exceptions.FactoriesException;
import edu.biu.scapi.exceptions.SecurityLevelException;
import edu.biu.scapi.interactiveMidProtocols.ot.OTOnByteArrayROutput;
import edu.biu.scapi.interactiveMidProtocols.ot.OTRBasicInput;
import edu.biu.scapi.interactiveMidProtocols.ot.OTROutput;
import edu.biu.scapi.interactiveMidProtocols.ot.oneSidedSimulation.OTOneSidedSimDDHOnByteArrayReceiver;
import edu.biu.scapi.midLayer.symmetricCrypto.encryption.ScEncryptThenMac;
import edu.biu.scapi.midLayer.symmetricCrypto.mac.ScCbcMacPrepending;
import edu.biu.scapi.primitives.prf.bc.BcAES;
import edu.biu.scapi.primitives.prg.PseudorandomGenerator;
import edu.biu.scapi.tools.Factories.PrgFactory;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class PSender {

    private int m, n, k, l;
    private byte[][] x0Array; // Array which contains the initial objects (Array of byte[])
    private byte[][] x1Array; // Array which contains the initial objects (Array of byte[])
    private byte[] sArray; // Choice bits for the OT PHASE
    private byte[][] kArray; // The array which contains the keys received via the OT PHASE (Array of byte[])
    private byte[][] uArray; // Array which contains the result of [G(k0) XOR G(k1) XOR choiceBits] to be sent to the Sender for the OT EXTENSION PHASE (Array of byte[])
    private byte[][] qArray; // Array which contains the result of [(Si * Ui) XOR G(Ksi)] for the OT EXTENSION PHASE (Array of byte[])
    private byte[][] qjArray; // Array which contains the [...]

    // Default Constructor
    public PSender() {
        m = 128;
        n = 128;
        k = 128;
        l = 128;
        x0Array = new byte[m][];
        x1Array = new byte[m][];
        sArray = new byte[l];
        kArray = new byte[l][];
        uArray = new byte[l][];
        qArray = new byte[l][];
        qjArray = new byte[l][];
    }

    // Overloaded Constructor
    public PSender(int m, int n, int k, int l) {

    }

    // Method to create randomly the initial X objects for the Sender
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

    // Method to printXArray
    public void printXArray() {
        for (int i = 0; i < x0Array.length; i++) {
            System.out.println(Arrays.toString(x0Array[i]));
        }

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
    // The Sender acts as the Receicer and the Receiver as a Sender
    // Method to create the sArray of size l
    public void setSArray() {
        new SecureRandom().nextBytes(sArray);
    }

    // Method to print the sArray of size l
    public void printSArray() {
        int counter = 0;
        for (int i = 0; i < l; i++) {
            System.out.println(readBit(sArray, i));
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
    // The Sender acts as the Receiver and the Receiver acts as the Sender
    // Method for the PSender to use OT as a receiver (OT PHASE). The method will run l times
    public void obliviousTransferReceiver() throws DuplicatePartyException, IOException, TimeoutException, ClassNotFoundException {

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

    // Method to print the kArray
    public void printKArray() {
        System.out.println("kArray: ");
        for (int i = 0; i < l; i++) {
            System.out.println(Arrays.toString(kArray[i]));
        }

    }

    // Method for sending the uArray during the OT EXTENSION PHASE
    public void uArrayTransferReceiver() throws DuplicatePartyException, IOException, TimeoutException, SecurityLevelException, NoSuchAlgorithmException, ClassNotFoundException {

        EncryptedChannel encryptedChannel = encryptedChannelCreation(); // Create the channel object
        ScEncryptThenMac encScheme = new ScEncryptThenMac();
        EncryptedChannel encryptedTCPChannel = new EncryptedChannel(encryptedChannel, encScheme);

        for (int i = 0; i < l; i++) {
            uArray[i] = (byte[]) encryptedTCPChannel.receive();
            System.out.println("Success!");
        }

        encryptedTCPChannel.close();

    }

    // Method to create a encrypted channel
    public EncryptedChannel encryptedChannelCreation() throws DuplicatePartyException, TimeoutException {
        // Prepare the parties list.
        LoadSocketParties loadParties = new LoadSocketParties("SocketParties.properties");
        List<PartyData> listOfParties = loadParties.getPartiesList();

        TwoPartyCommunicationSetup commSetup = new SocketCommunicationSetup(listOfParties.get(0), listOfParties.get(1));

        // Call the prepareForCommunication function to establish one connection within 2000000 milliseconds.
        Map<String, Channel> connections = commSetup.prepareForCommunication(1, 2000000);

        // Return the channel to the calling application. There is only one created channel.
        return (EncryptedChannel) connections.values().toArray()[0];
    }

    // Method to print the uArray
    public void printUArray() {
        System.out.println("\nU array:");
        for (int i = 0; i < l; i++) {
            System.out.println("Length: " + uArray[i].length + ", Output: " + Arrays.toString(uArray[i]));
        }
    }

    // NOT TESTED YET
    // OT EXTENSION PHASE
    // Method to set the qArray which contains the result of [(Si * Ui) XOR G(Ksi)]
    public void setQArray() throws InvalidKeyException, FactoriesException {

        for (int i = 0; i < l; i++) {
            if (sArray[i] == 0) {
                //System.out.println("sArray[" + i + "]: " + sArray[i]);
                //System.out.println("qArray[i] = SCAPI_PRG(m, kArray[i])");
                qArray[i] = SCAPI_PRG(m, kArray[i]);
            } else {
                //System.out.println("sArray[" + i + "]: " + sArray[i]);
                //System.out.println("qArray[i] = uArray[i] XOR SCAPI_PRG(m, kArray[i])");
                qArray[i] = xorByteArrays(uArray[i], SCAPI_PRG(m, kArray[i]));
            }
        }

    }

    // NOT TESTED YET
    // Method to print the qArray
    public void printQArray() {
        for (int i = 0; i < l; i++) {
            System.out.println("Length: " + qArray[i].length + ", Output: " + Arrays.toString(qArray[i]));
        }
    }

    // Pseudorandom Generator
    // SCAPI's implementation using RC4 (only RC4 is supported at the moment)
    // Uses the byte array as key and produces an outpout of size m
    public byte[] SCAPI_PRG(int m, byte[] k) throws FactoriesException, InvalidKeyException {

        // Create prg using the PrgFactory
        PseudorandomGenerator prg = PrgFactory.getInstance().getObject("RC4");

        // http://stackoverflow.com/a/14204473/873309
        // Use the incoming byte array as the secret key
        SecretKey secretKey = new SecretKeySpec(k, 0, k.length, "RC4");

        // set the key
        prg.setKey(secretKey);

        // Get PRG bytes. The caller is responsible for allocating the out array.
        // The result will be put in the out array.
        byte[] outBytes = new byte[m];
        prg.getPRGBytes(outBytes, 0, m);

        return outBytes;
    }

    // Method to get the XOR result between two byte arrays
    static byte[] xorByteArrays(byte[] a, byte[] b) {

        byte[] result = new byte[Math.min(a.length, b.length)];

        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (((int) a[i]) ^ ((int) b[i]));
        }

        return result;

    }

    // NOT TESTED YET
    // OT EXTENSION PHASE
    // Method to set the qjArray
    public void setQJArray() throws IOException {

        List<Integer> list = new ArrayList<>();
        int counter = 0;

        for (int j = 0; j < m; j++) {

            for (int i = 0; i < l; i++) {
                int x = (readBit(qArray[i], counter));
                list.add(x);

                // write to byte array
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(baos);
                for (int element : list) {
                    out.writeUTF(Integer.toString(element));
                }

                byte[] bytes = baos.toByteArray();


            }

            qjArray[i] = bytes;
            counter++;
        }
    }

}













































