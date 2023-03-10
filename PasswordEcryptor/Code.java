/* Aryan Suri
   Password-Encryption Software */

import java.util.Scanner;
import java.security.SecureRandom;
import java.lang.*;
import java.lang.StringBuilder;
import java.util.Map;
import java.util.HashMap;
import java.math.BigInteger;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;

public class PasswordEcryptor 
{

    //returns a next closest prime value
    public static BigInteger getNextPrime(String ans) 
    {
        BigInteger one = new BigInteger("1");
        BigInteger test = new BigInteger(ans);
        //decreases the chances of a prime to slip
        while (!test.isProbablePrime(128))
            test = test.add(one);
        return test;        
    }

    //generates a random b value to use later 
    public static BigInteger randomBigInt()
    {
        BigInteger maxLimit = new BigInteger("5000000000000");
        BigInteger minLimit = new BigInteger("25000000000");
        BigInteger bigInteger = maxLimit.subtract(minLimit);
        Random randNum = new Random();
        int len = maxLimit.bitLength();
        BigInteger res = new BigInteger(len, randNum);
        if (res.compareTo(minLimit) < 0)
            res = res.add(minLimit);
        if (res.compareTo(bigInteger) >= 0)
            res = res.mod(bigInteger).add(minLimit);
        return res;
    }

    //performs the diffie-hellman algorithm 
    public static BigInteger diffieHellman(BigInteger p, BigInteger g, BigInteger a, BigInteger b)
    {
        BigInteger resultB = g.modPow(b,p);
        BigInteger key = resultB.modPow(a,p);
        return key;
    }

    //splits the plaintext into left
    public static StringBuilder splitLeft(String[] plainText)
    {
        int length = plainText.length / 2;
        StringBuilder left = new StringBuilder();

        for(int i = 0; i < length; i++)
            left.append(plainText[i]);

        return left;
    }

    //splits the plaintext into right
    public static StringBuilder splitRight(String[] plainText)
    {
        int length = plainText.length / 2;
        StringBuilder right = new StringBuilder();

        for(int j = length; j < plainText.length; j++)
            right.append(plainText[j]);

        return right;
    }

    //performs ceasar cipher on the right side of the plainText
    public static StringBuilder CeasarCipherEncryption(StringBuilder right, int position)
    {
        StringBuilder encrypt = new StringBuilder();
        Map<Character,Character> dict = new HashMap<>()
            {{
                    put('$', '@'); put('@', '&'); put('&', '$'); put('#', '!'); put('%', '*'); put('!', '%'); put('*', '#');
                }};
        for (int i = 0; i < right.length(); i++)
        {
            if(right.charAt(i) == '$' || right.charAt(i) == '@' || right.charAt(i) == '&' || right.charAt(i) == '#' || right.charAt(i) == '%' || right.charAt(i) == '!' || right.charAt(i) == '*')
            {
                char ch = (char)(dict.get(right.charAt(i)));
                encrypt.append(ch);
            }
            else
            if(Character.isDigit(right.charAt(i)))
            {
                char ch = (char)(right.charAt(i) + position);
                encrypt.append(ch);
            }
            else
            if (Character.isUpperCase(right.charAt(i)))
            {
                char ch = (char)(((int)right.charAt(i) + position - 65) % 26 + 65);
                encrypt.append(ch);
            }
            else
            if (Character.isLowerCase(right.charAt(i)))
            {
                char ch = (char)(((int)right.charAt(i) + position - 97) % 26 + 97);
                encrypt.append(ch);
            }
        }
        return encrypt;
    }

    //performs ceasar cipher decryption on the right text
    public static StringBuilder CeasarCipherDecryption(StringBuilder encrypted, int position)
    {
        StringBuilder decrypt = new StringBuilder();
        Map<Character,Character> dict = new HashMap<>()
            {{
                    put('@', '$'); put('&', '@'); put('$', '&'); put('!', '#'); put('*', '%'); put('%', '!'); put('#', '*');
                }};
        for (int i = 0; i < encrypted.length(); i++)
        {
            if(encrypted.charAt(i) == '$' || encrypted.charAt(i) == '@' || encrypted.charAt(i) == '&' || encrypted.charAt(i) == '#' || encrypted.charAt(i) == '%' || encrypted.charAt(i) == '!' || encrypted.charAt(i) == '*')
            {
                char ch = (char)(dict.get(encrypted.charAt(i)));
                decrypt.append(ch);
            }
            else
            if(Character.isDigit(encrypted.charAt(i)))
            {
                char ch = (char)(encrypted.charAt(i) - position);
                decrypt.append(ch);
            }
            else
            if (Character.isUpperCase(encrypted.charAt(i)))
            {
                char ch = (char)(((int)encrypted.charAt(i) - position - 65) % 26 + 65);
                decrypt.append(ch);
            }
            else
            if (Character.isLowerCase(encrypted.charAt(i)))
            {
                char ch = (char)(((int)encrypted.charAt(i) - position - 97) % 26 + 97);
                decrypt.append(ch);
            }
        }
        return decrypt;
    }

    //TEA Algorithm
    private static final int delta = 0x9e3779b9; // key scheduling constant for TEA

    //Encryption method for TEA
    public static long encryptTEA(long plainText, int[] key) 
    {   
        //splitting the plaintext into two halves
        int right = (int) plainText; //right half of plain text 
        int left = (int) (plainText >>> 32); // left half og plain text
        long sum = 0;        //initialze the sum

        //encrypts the left and right side using the key
        for (int i = 0; i < 32; i++) 
        {
            sum += delta;
            left += ((right << 4) + key[0]) ^ (right + sum) ^ ((right >>> 5) + key[1]);
            right += ((left << 4) + key[2]) ^ (left + sum) ^ ((left >>> 5) + key[3]);
        }

        //puts the left and right side back together
        long leftResult = (long) left;
        leftResult = leftResult << 32;
        long rightResult = (long) right;

        //correctly shifts the right side 
        rightResult = rightResult << 32;
        rightResult = rightResult >>> 32;

        //puts right and left side back together
        long finalResult = rightResult | leftResult;

        //returns the result
        return finalResult;
    }

    //Decryption method for TEA
    public static long decryptTEA(long cipherText, int[] key) 
    {      
        //splits the ciphertext into two halves
        int right = (int) cipherText;      //right side of the ciphertext
        int left = (int) (cipherText >>> 32);    //left side of the ciphertext
        long sum = delta << 5;           //initializes the sum

        //decrypts the left and right side of the ciphertext using the key
        for (int i = 0; i < 32; i++) 
        {
            right -= ((left << 4) + key[2]) ^ (left + sum) ^ ((left >>> 5) + key[3]);
            left -= ((right << 4) + key[0]) ^ (right + sum) ^ ((right >>> 5) + key[1]);
            sum -= delta;
        }

        //puts the left and right side back together
        long leftResult = (long) left;
        long rightResult = (long) (right);
        long finalResult =  (rightResult << 32) | (leftResult);   //right side is on the left and left side is on the right

        //splits the result again and puts it back together in the correct order
        long leftFinal = finalResult;
        long rightFinal = finalResult >>> 32;
        long rearrange =  rightFinal | (leftFinal << 32);    //correctly assembles the key

        //returns the final result
        return rearrange;
    }

    //performs CBC on the left side of the text using the key from TEA
    public static byte[] encryptCBC(String plainText, String key)
    {
        try{
            int ivSize = 16; 
            byte[] plainbytes = plainText.getBytes();       
            byte[] iv = new byte[ivSize];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(key.getBytes("UTF-8"));
            byte[] keyBytes = new byte[16];
            System.arraycopy(digest.digest(), 0, keyBytes, 0, keyBytes.length);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] encrypted = cipher.doFinal(plainbytes);
            byte[] encryptedIVAndText = new byte[ivSize + encrypted.length];
            System.arraycopy(iv, 0, encryptedIVAndText, 0, ivSize);
            System.arraycopy(encrypted, 0, encryptedIVAndText, ivSize, encrypted.length);
            return encryptedIVAndText;
        } catch(Exception ex){
            System.out.println("CBC encryption failed");
        }
        return null;
    }

    //decrypts TEA
    public static String decryptCBC(byte[] encryptedIvTextBytes, String key)
    {
        int ivSize = 16;
        int keySize = 16;
        try
        {

            // Extract IV.
            byte[] iv = new byte[ivSize];
            System.arraycopy(encryptedIvTextBytes, 0, iv, 0, iv.length);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            // Extract encrypted part.
            int encryptedSize = encryptedIvTextBytes.length - ivSize;
            byte[] encryptedBytes = new byte[encryptedSize];
            System.arraycopy(encryptedIvTextBytes, ivSize, encryptedBytes, 0, encryptedSize);

            // Hash key.
            byte[] keyBytes = new byte[keySize];
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(key.getBytes());
            System.arraycopy(md.digest(), 0, keyBytes, 0, keyBytes.length);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

            // Decrypt.
            Cipher cipherDecrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

            byte[] decrypted = cipherDecrypt.doFinal(encryptedBytes);
            return new String(decrypted);
        }catch(Exception ex){
            System.out.println("CBC decryption failed");
        }
        return null;
    }

    public static void main(String[] args) throws Exception
    {
        Scanner scan = new Scanner(System.in);

        //asks user for the p-value
        System.out.print("Enter the approximate value of p you want.");
        String ans = scan.next();
        BigInteger p = getNextPrime(ans);
        System.out.println("Your p value is: " + p);

        //asks user for the g value
        System.out.print("Enter a g-value between 2 and p - 1 ");
        BigInteger g = new BigInteger(scan.next());

        //asks the user for the a-value
        System.out.print("Enter the secret a-value ");
        BigInteger a = new BigInteger(scan.next());
        System.out.println();

        //randmonly generates the b-value
        BigInteger b = randomBigInt();

        //performs the diffie-hellman algorithm
        BigInteger diffieHellman = diffieHellman(p,g,a,b);
        //System.out.println("DiffieHellman: " + diffieHellman);
        //System.out.println();

        long DiffieLong = diffieHellman.longValue();

        //asks user for the plaintext and splits it into left and right
        System.out.print("Please enter the plaintext: ");
        String plaintext = scan.next();
        String[] userInfoArray = plaintext.split("");
        //splits into left
        StringBuilder Left = splitLeft(userInfoArray);
        //splits into right
        StringBuilder Right = splitRight(userInfoArray);

        //performs the Ceasar cipher encryption on right
        StringBuilder CeasarRightEncrypt = CeasarCipherEncryption(Right, 1);
        //System.out.println("Ceasar Cipher Encryption: " + CeasarRightEncrypt);

        //performs ceasar cipher decrypion on right
        StringBuilder CeasarRightDecrypt = CeasarCipherDecryption(CeasarRightEncrypt, 1);
        //System.out.println("Ceasar Cipher Decryption: " + CeasarRightDecrypt);

        //does the encryption and decryption for TEA
        long TEAPlaintext = DiffieLong;
        int[] keyTEA = {0xbf6babcd, 0xef00f000, 0xfeafafaf, 0xaccdef01};

        /*
        System.out.println("TEA:");
        System.out.println("-------------------------");
        System.out.println("Original Plaintext : ");
        System.out.print(TEAPlaintext);
        System.out.println();
        System.out.println("-------------------------");
        System.out.println("Key used for encryption : ");
        System.out.printf("%x", keyTEA[0]);
        System.out.printf("%x", keyTEA[1]);
        System.out.printf("%x", keyTEA[2]);
        System.out.printf("%x", keyTEA[3]);
        System.out.println();  
        System.out.println("-------------------------");
         */

        long encryptionTEA = encryptTEA(TEAPlaintext, keyTEA); 

        /*
        System.out.println("Ciphertext: "); 
        System.out.printf("%x", encryptionTEA);  
        System.out.println();  
        System.out.println("-------------------------");
         */

        long decryptionTEA = decryptTEA(encryptionTEA, keyTEA);  

        /*
        System.out.println("Decrypted Text: ");
        System.out.print(decryptionTEA);
        System.out.println();
        System.out.println("-------------------------");
         */

        String CBCKey = Long.toHexString(encryptionTEA);

        String leftPlain = Left.toString();

        //System.out.println("Key used: " + CBCKey);
        //System.out.println("Left Text: " + leftPlain);

        byte[] encryptedCBC = encryptCBC(leftPlain, CBCKey);

        //System.out.println("Encrypted: " + encrypted);

        String decryptedCBC = decryptCBC(encryptedCBC, CBCKey);
        //System.out.println("Decrypted: " + decrypted);

        System.out.println("Final Encrypted: ");
        System.out.print(encryptedCBC);
        System.out.print(CeasarRightEncrypt);
        System.out.println();
        System.out.println("Final Decrypt: ");
        System.out.print(decryptedCBC);
        System.out.print(CeasarRightDecrypt);
    }

    //TOPICS DISCUSSED:
    //Secure Random
    //Hashes
    //TEA
    //CBC
    //Diffie-Hellman
    //Ceasar Cipher

}
