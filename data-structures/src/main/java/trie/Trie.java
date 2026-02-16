package trie;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Trie {

    /** 整棵 Trie 的根节点，不存储实际字符，仅作为入口。 */
    public final static TrieNode wordsTree = new TrieNode();

    /**
     * 向 Trie 中插入一个单词及其解释。
     * 约定：单词由 a-z 小写字母组成。
     */
    public void insert(String words, String explain) {
        TrieNode root = wordsTree;      // 每次插入都从根节点开始向下遍历
        char[] chars = words.toCharArray(); // 先转字符数组，减少重复随机访问
        for (char c : chars) {
            int idx = c - 'a'; // 将字符映射到 [0,25]，对应 slot 数组下标
            // 当前字符路径不存在时，创建新节点，保证路径连续
            if (root.slot[idx] == null) {
                root.slot[idx] = new TrieNode();
            }
            root = root.slot[idx]; // 游标下沉到当前字符节点，继续处理下一个字符
            root.c = c;            // 记录该节点代表的字符，便于调试和可视化
            root.prefix++;         // 经过该节点的单词数量 +1，用于前缀检索统计
        }
        // 字符遍历结束后，root 正好落在完整单词的尾节点
        root.explain = explain; // 保存单词解释信息
        root.isWord = true;     // 标记该节点为“完整单词结束位置”
    }

    /**
     * 按前缀检索候选单词列表（最多返回 15 条）。
     */
    public List<String> searchPrefix(String prefix) {
        TrieNode root = wordsTree;          // 先从根节点定位前缀终点
        char[] chars = prefix.toCharArray();
        StringBuilder cache = new StringBuilder(); // 记录当前已匹配的前缀字符串
        // 第一步：精准匹配前缀路径，找出前缀对应的最后一个节点
        for (char c : chars) {
            int idx = c - 'a';
            // 只要字符越界或路径不存在，说明 Trie 中没有这个前缀
            if (idx >= root.slot.length || idx < 0 || root.slot[idx] == null) {
                return Collections.emptyList();
            }
            cache.append(c);
            root = root.slot[idx]; // 沿前缀路径继续下沉
        }
        // 第二步：从前缀终点向下做 DFS，收集所有可能单词
        ArrayList<String> list = new ArrayList<>();
        if (root.prefix != 0) {
            for (int i = 0; i < root.slot.length; i++) {
                if (root.slot[i] != null) {
                    char c = (char) (i + 'a');
                    // pre 传入“前缀 + 当前首个扩展字符”，递归收集后续路径
                    collect(root.slot[i], String.valueOf(cache) + c, list, 15);
                    // 达到上限后立即返回，避免无意义遍历
                    if (list.size() >= 15) {
                        return list;
                    }
                }
            }
        }
        return list;
    }

    /**
     * 深度优先收集单词。
     *
     * @param trieNode    当前遍历节点
     * @param pre         当前节点对应的完整字符串
     * @param queue       结果集
     * @param resultLimit 结果上限
     */
    protected void collect(TrieNode trieNode, String pre, List<String> queue, int resultLimit) {
        // 当前节点若是完整单词终点，则加入结果
        if (trieNode.isWord) {
            trieNode.word = pre;
            // 输出格式：单词 -> 解释
            queue.add(trieNode.word + " -> " + trieNode.explain);
            // 已达到上限则停止当前分支继续扩展
            if (queue.size() >= resultLimit) {
                return;
            }
        }
        // 继续遍历 26 个子节点，递归构造更长的单词
        for (int i = 0; i < trieNode.slot.length; i++) {
            char c = (char) ('a' + i);
            if (trieNode.slot[i] != null) {
                collect(trieNode.slot[i], pre + c, queue, resultLimit);
            }
        }
    }

    @Override
    public String toString() {
        return "Trie：" + JSON.toJSONString(wordsTree);
    }
}
