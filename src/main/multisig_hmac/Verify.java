package multisig_hmac;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Verify represents a verification of a signature of data against a list of keys.
 *
 * @author Amalie Due Jensen
 */
public class Verify {

    /**
     * Verifies a signature of data against a list of keys
     *
     * @param Keys - list of all keys
     * @param Signature - combined signature
     * @param data - data which has been signed
     * @param Threshold - minimum number of keys that the list Keys should contain
     * @param Algorithm - algorithm used for HMAC
     * @param BYTES - length of signature
     * @return verification of the signature (true/false)
     * @throws InvalidKeyException - if the given key is inappropriate for initializing this HMAC
     * @throws NoSuchAlgorithmException - if the specified algorithm is not available
     */
    public static boolean verify(List<IndexKey> Keys, Combine Signature, byte[] data, int Threshold, String Algorithm, int BYTES) throws InvalidKeyException, NoSuchAlgorithmException {
        assert Signature.sig.length == BYTES: "Signature must be BYTES long";
        assert data != null : "data must be bytes";
        assert Threshold > 0 : "Threshold must be at least 1";

        int BitField = Signature.bitfield;
        int nKeys = PopCount(BitField);
        int HighestKey = 32 - LeadingZeros(BitField);
        assert Keys.size() >= nKeys && Keys.size() >= HighestKey : "Not enough keys given based on Signature.bitfield";

        if (nKeys < Threshold) {
            return false;
        }

        List<Integer> UsedKeys = keyIndexes(BitField);
        byte[] Sig = Signature.sig;

        for (Object obj : UsedKeys) {
            IndexKey Key = Keys.get((Integer) obj);
            Sign KeySig = new Sign(Key, data, Algorithm);
            Sig = Combine.xorBytes(Sig, KeySig.sign, BYTES);
            BitField ^= KeySig.index;
        }

        return (BitField == 0 && Arrays.equals(Sig,new byte[BYTES]));
    }

    /**
     * Computes the indexes of the keys (i.e. high bits)
     *
     * @param BitField - indexes of keys represented as one integer
     * @return indexes of keys in a list
     */
    public static List<Integer> keyIndexes(int BitField) {
        List<Integer> KeyIndexes = new ArrayList<>();
        int i = 0;
        while (BitField > 0) {
            if ((BitField & 0x1) == 1) KeyIndexes.add(i);
            BitField >>= 1;
            i++;
        }
        return KeyIndexes;
    }

    /**
     * Computes the number of keys (i.e. high bits)
     *
     * @param BitField - indexes of keys represented as one integer
     * @return the number of keys
     */
    public static int PopCount(int BitField) {
        return Integer.bitCount(BitField);
    }

    public static int LeadingZeros(int BitField) {
        int n = 32;
        int c = 16;
        int y;
        while(c != 0) {
            y = BitField >> c;
            if(y != 0) {
                n = n - c;
                BitField = y;
            }
            c = c >> 1;
        }
        return n - BitField;
    }
}