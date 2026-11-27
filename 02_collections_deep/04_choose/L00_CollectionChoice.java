import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * [主题]: 集合选型清单 —— 场景 → 该用啥
 * [难度]: Medium
 * [前置]: 00_～03_ 本阶段前几课
 *
 * 一、主题说明
 * 会 API 还不够，要会选结构。本课用小例子固化选型直觉。
 *
 * 二、写法对比
 * 1. 直观版：全程 ArrayList 硬扛（能跑但不合适）。
 * 2. 进阶版：按场景选 List / Map / Queue / Deque。
 *
 * 三、真实用途
 * - 任务调度、LRU 雏形、排行榜、消息缓冲。
 * - 代码评审时「这个结构选错了」是高频意见。
 */
public class L00_CollectionChoice {

    /** 错误直觉：用 List 当队列头删（慢）。 */
    public static Integer pollNaive(List<Integer> list) {
        if (list.isEmpty()) {
            return null;
        }
        return list.remove(0); // ArrayList 头删 O(n)
    }

    /** 队列：ArrayDeque 作 Queue。 */
    public static Integer pollBetter(Queue<Integer> queue) {
        return queue.poll();
    }

    /** 栈：Deque 的 push/pop。 */
    public static String reverseByStack(String s) {
        Deque<Character> stack = new ArrayDeque<>();
        for (char ch : s.toCharArray()) {
            stack.push(ch);
        }
        StringBuilder sb = new StringBuilder();
        while (!stack.isEmpty()) {
            sb.append(stack.pop());
        }
        return sb.toString();
    }

    /** 小顶堆：每次取最小优先级任务。 */
    public static List<Integer> drainPriority(int... tasks) {
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        for (int t : tasks) {
            pq.offer(t);
        }
        List<Integer> order = new ArrayList<>();
        while (!pq.isEmpty()) {
            order.add(pq.poll());
        }
        return order;
    }

    /**
     * 简易 LRU 雏形：LinkedHashMap accessOrder=true，超容删最老。
     * 真实 Redis/Guava 更复杂，这里只建直觉。
     */
    public static Map<String, String> lruPut(LinkedHashMap<String, String> cache, int max, String k, String v) {
        cache.put(k, v);
        // 访问一次以更新顺序
        cache.get(k);
        while (cache.size() > max) {
            String eldest = cache.keySet().iterator().next();
            cache.remove(eldest);
        }
        return cache;
    }

    public static LinkedHashMap<String, String> newLru() {
        return new LinkedHashMap<>(16, 0.75f, true);
    }

    /** 计数场景：肯定用 Map，不是 List。 */
    public static Map<String, Integer> countWithMap(String[] arr) {
        Map<String, Integer> map = new HashMap<>();
        for (String s : arr) {
            map.merge(s, 1, Integer::sum);
        }
        return map;
    }

    public static void runTests() {
        List<Integer> listQ = new ArrayList<>(List.of(1, 2, 3));
        if (pollNaive(listQ) != 1 || !listQ.equals(List.of(2, 3))) {
            throw new AssertionError("naive poll 失败");
        }
        Queue<Integer> q = new ArrayDeque<>(List.of(1, 2, 3));
        if (pollBetter(q) != 1 || q.size() != 2) {
            throw new AssertionError("queue poll 失败");
        }
        if (!"cba".equals(reverseByStack("abc"))) {
            throw new AssertionError("栈反转失败");
        }
        if (!drainPriority(5, 1, 3).equals(List.of(1, 3, 5))) {
            throw new AssertionError("优先队列顺序错误");
        }
        LinkedHashMap<String, String> lru = newLru();
        lruPut(lru, 2, "a", "1");
        lruPut(lru, 2, "b", "2");
        lruPut(lru, 2, "c", "3"); // 应挤掉最久未用（a）
        if (lru.containsKey("a") || !lru.containsKey("b") || !lru.containsKey("c")) {
            throw new AssertionError("LRU 淘汰错误: " + lru);
        }
        if (countWithMap(new String[]{"x", "x", "y"}).get("x") != 2) {
            throw new AssertionError("计数失败");
        }
        System.out.println("✅ CollectionChoice 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 CollectionChoice ************\n");
        System.out.println("reverse=" + reverseByStack("java"));
        System.out.println("priority=" + drainPriority(4, 2, 9));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
