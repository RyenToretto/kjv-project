import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * [主题]: List 深挖 —— ArrayList vs LinkedList 直觉
 * [难度]: Easy～Medium
 * [前置]: 00_java_basics、01_java8_plus
 *
 * 一、主题说明
 * List：有序、可重复。最常用 ArrayList（底层动态数组）。
 * LinkedList：双向链表，头尾插入快，随机下标访问慢——日常业务几乎总是 ArrayList。
 *
 * 二、写法对比
 * 1. 直观版：固定数组手动扩容思路（帮助理解 ArrayList）。
 * 2. 进阶版：直接用 ArrayList / 对比 LinkedList 头插。
 *
 * 三、真实用途
 * - 订单明细、搜索结果页、批量 ID 列表。
 * - 面试常问：为什么默认 ArrayList？随机访问 O(1)。
 */
public class L00_ListDeepDive {

    /** 直观版：手动「动态数组」追加（简化版，每次扩容 2 倍）。 */
    public static int[] appendNaive(int[] arr, int value) {
        int[] bigger = new int[arr.length + 1];
        System.arraycopy(arr, 0, bigger, 0, arr.length);
        bigger[arr.length] = value;
        return bigger; // 真·每次都拷贝；ArrayList 会按容量成批扩容更优
    }

    public static List<Integer> buildArrayList(int... values) {
        List<Integer> list = new ArrayList<>();
        for (int v : values) {
            list.add(v);
        }
        return list;
    }

    /** 头插：LinkedList 合适；ArrayList 头插要搬移元素。 */
    public static List<Integer> prependLinked(int head, List<Integer> rest) {
        LinkedList<Integer> list = new LinkedList<>(rest);
        list.addFirst(head);
        return list;
    }

    public static int getAt(List<Integer> list, int index) {
        return list.get(index);
    }

    /** 删除满足条件的元素（倒序删，避免下标错乱）。 */
    public static void removeEvenInPlace(List<Integer> list) {
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i) % 2 == 0) {
                list.remove(i);
            }
        }
    }

    public static void runTests() {
        int[] a = appendNaive(new int[]{1, 2}, 3);
        if (a.length != 3 || a[2] != 3) {
            throw new AssertionError("appendNaive 失败");
        }
        List<Integer> list = buildArrayList(10, 20, 30);
        if (list.size() != 3 || getAt(list, 1) != 20) {
            throw new AssertionError("ArrayList 读写失败");
        }
        List<Integer> linked = prependLinked(0, List.of(1, 2));
        if (!linked.equals(List.of(0, 1, 2))) {
            throw new AssertionError("头插失败: " + linked);
        }
        List<Integer> mutable = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        removeEvenInPlace(mutable);
        if (!mutable.equals(List.of(1, 3, 5))) {
            throw new AssertionError("删偶数失败: " + mutable);
        }
        // List.of 不可变
        boolean immutable = false;
        try {
            List.of(1).add(2);
        } catch (UnsupportedOperationException ex) {
            immutable = true;
        }
        if (!immutable) {
            throw new AssertionError("List.of 应不可变");
        }
        System.out.println("✅ ListDeepDive 所有测试用例通过！");
    }

    public static void bootstrap() {
        System.out.println("\n\n************ L00 ListDeepDive ************\n");
        System.out.println(buildArrayList(1, 2, 3));
        System.out.println(prependLinked(9, List.of(8, 7)));
        runTests();
    }

    public static void main(String[] args) {
        bootstrap();
    }
}
