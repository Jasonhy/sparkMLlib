package helper;

import java.math.BigInteger;
import java.util.List;

/**
 * 构建SimHash的工具类
 * 在构建海明距离的时候，认为小于7就是相似，当然
 * 这个相似的临界值是可以调整的
 */
public class SimHash {
    // 传递过来的字符串
    private String tokens;
    // simHash值
    public BigInteger simHashVal;
    // 控制多少位字符
    private int hashbits;

    /**
     * @param tokens   需要处理的字符串
     * @param hashbits 可设置bit值，一般情况设置64和128比较理想
     */
    public SimHash(String tokens, int hashbits) {
        this.tokens = tokens;
        this.hashbits = hashbits;
        this.simHashVal = this.simHash();
    }

    /**
     * 获取simhash值
     *
     * @return
     */
    private BigInteger simHash() {
        int[] v = new int[this.hashbits];
        Segment instance = Segment.getInstance();
        List<String> splitWord = instance.nlp(this.tokens);
        for (int j = 0; j < splitWord.size(); j++) {
            BigInteger t = this.hash(splitWord.get(j));
            for (int i = 0; i < this.hashbits; i++) {
                BigInteger bitmask = new BigInteger("1").shiftLeft(i);
                if (t.and(bitmask).signum() != 0) {
                    v[i] += 1;
                } else {
                    v[i] -= 1;
                }
            }
        }
        BigInteger fingerprint = new BigInteger("0");
        for (int i = 0; i < this.hashbits; i++) {
            if (v[i] >= 0) {
                fingerprint = fingerprint.add(new BigInteger("1").shiftLeft(i));
            }
        }
        return fingerprint;
    }

    private BigInteger hash(String source) {
        if (source == null || source.length() == 0) {
            return new BigInteger("0");
        } else {
            char[] sourceArray = source.toCharArray();
            BigInteger x = BigInteger.valueOf(((long) sourceArray[0]) << 7);
            BigInteger m = new BigInteger("1000003");
            BigInteger mask = new BigInteger("2").pow(this.hashbits).subtract(
                    new BigInteger("1"));
            for (char item : sourceArray) {
                BigInteger temp = BigInteger.valueOf((long) item);
                x = x.multiply(m).xor(temp).and(mask);
            }
            x = x.xor(new BigInteger(String.valueOf(source.length())));
            if (x.equals(new BigInteger("-1"))) {
                x = new BigInteger("-2");
            }
            return x;
        }
    }

    /**
     * 计算海明距离
     *
     * @param currHash  当前hash值
     * @param otherHash 其他hash值
     * @return 返回的是两个hash值的海明距离
     */
    public int hammingDistance(BigInteger currHash, BigInteger otherHash) {
        BigInteger m = new BigInteger("1").shiftLeft(this.hashbits).subtract(
                new BigInteger("1"));
        // BigInteger x = this.strSimHash.xor(other.strSimHash).and(m);
        BigInteger x = currHash.xor(otherHash).and(m);
        int tot = 0;
        while (x.signum() != 0) {
            tot += 1;
            x = x.and(x.subtract(new BigInteger("1")));
        }
        return tot;
    }

}