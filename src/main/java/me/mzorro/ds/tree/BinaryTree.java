/**
 * Created by Zorro on 6/20 020.
 */
package me.mzorro.ds.tree;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * 二叉树
 */
public abstract class BinaryTree {

    /**
     * 二叉树节点
     */
    public static class TNode {
        private TNode left;
        private TNode right;
        private TNode parent;
        private int value;

        public TNode(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "" + value;
        }

        public TNode getLeft() {
            return left;
        }

        public void setLeft(TNode left) {
            this.left = left;
        }

        public TNode getRight() {
            return right;
        }

        public void setRight(TNode right) {
            this.right = right;
        }

        public TNode getParent() {
            return parent;
        }

        public void setParent(TNode parent) {
            this.parent = parent;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        private void checkParent() {
            if (parent == null)
                throw new NullPointerException("parent is null");
        }
        
        public boolean isLeftChild() {
            checkParent();
            return this == parent.left;
        }

        public boolean isRightChild() {
            checkParent();
            return this == parent.right;
        }
        
        public TNode getGrantParent() {
            checkParent();
            return parent.parent;
        }
    }
    
    private TNode root;

    protected TNode getRoot() {
        return root;
    }

    protected void setRoot(TNode root) {
        this.root = root;
    }

    /**
     * 输出前序树形结构中的一个节点，在printAsPreOrder中使用
     * @param node 要输出的节点
     * @param parent node的父节点
     * @param childrenCount 各节点已输出的孩子数映射表
     */
    private String printNodeAsPreOrder(TNode node, TNode parent,
                                     HashMap<TNode, Integer> childrenCount) {
        StringBuilder sb = new StringBuilder();
        Deque<TNode> pathToRoot = new LinkedList<TNode>();
        if (parent != null) {
            if (!childrenCount.containsKey(parent)) {
                childrenCount.put(parent, 1);
            } else {
                childrenCount.put(parent, 2);
            }
            TNode p = parent.parent;
            while (p != null) {
                pathToRoot.addLast(p);
                p = p.parent;
            }
            while (!pathToRoot.isEmpty()) {
                p = pathToRoot.removeLast();
                if (childrenCount.get(p) == 2) {
                    sb.append("    ");
                } else {
                    sb.append("|   ");
                }
            }
            if (childrenCount.get(parent) == 2) {
                sb.append("\\---");
            } else {
                sb.append("|---");
            }
        }
        sb.append(node);
        sb.append('\n');
        return sb.toString();
    }

    /**
     * 以前序树形结构输出一棵树
     * @return
     */
    public String printAsPreOrder() {
        StringBuilder sb = new StringBuilder();
        // 用栈来进行前序遍历
        Deque<TNode> stack = new LinkedList<TNode>();

        // 用来记录每个节点已输出的孩子数
        HashMap<TNode, Integer> childrenCount = new HashMap<TNode, Integer>();

        TNode p = root, pnt = null;
        while (p != null) {
            sb.append(printNodeAsPreOrder(p, p.parent, childrenCount));
            stack.addLast(p);
            pnt = p;
            p = p.left;
        }
        sb.append(printNodeAsPreOrder(null, pnt, childrenCount));
        while (!stack.isEmpty()) {
            TNode node = stack.removeLast();
            p = node.right;
            pnt = node;
            while (p != null) {
                sb.append(printNodeAsPreOrder(p, p.parent, childrenCount));
                stack.addLast(p);
                pnt = p;
                p = p.left;
            }
            sb.append(printNodeAsPreOrder(null, pnt, childrenCount));
        }
        return sb.toString();
    }

    /**
     * 输出中序树形结构中的一个节点，在printAsInOrder中使用
     * @param node 要输出的节点
     * @param parent node的父节点
     * @param childrenCount 各节点已输出的孩子数映射表
     */
    private String printNodeAsInOrder(TNode node, TNode parent,
                                    HashMap<TNode, Integer> childrenCount) {
        StringBuilder sb = new StringBuilder();
        Deque<TNode> pathToRoot = new LinkedList<TNode>();
        if (parent != null) {
            if (!childrenCount.containsKey(parent)) {
                childrenCount.put(parent, 1);
            } else {
                childrenCount.put(parent, 2);
            }
            TNode p = parent.parent;
            while (p != null) {
                pathToRoot.addLast(p);
                p = p.parent;
            }
            while (!pathToRoot.isEmpty()) {
                p = pathToRoot.removeLast();
                if (!childrenCount.containsKey(p) || childrenCount.get(p) == 2) {
                    sb.append("    ");
                } else {
                    sb.append("|   ");
                }
            }
            if (childrenCount.get(parent) == 2) {
                sb.append("\\---");
            } else if (childrenCount.get(parent) == 1) {
                sb.append("/---");
            }
        }
        sb.append(node);
        sb.append('\n');
        return sb.toString();
    }

    /**
     * 以中序树形结构输出一棵树
     * @return
     */
    public String printAsInOrder() {
        StringBuilder sb = new StringBuilder();
        // 用栈来进行中序遍历
        Deque<TNode> stack = new LinkedList<TNode>();

        // 用来记录每个节点已输出的孩子数
        HashMap<TNode, Integer> childrenCount = new HashMap<TNode, Integer>();

        TNode p = root;
        while (p != null) {
            stack.addLast(p);
            p = p.left;
        }
        stack.addLast(stack.peekLast());
        stack.addLast(null);
        while (!stack.isEmpty()) {
            p = stack.removeLast();
            if (p == null) {
                sb.append(printNodeAsInOrder(null, stack.removeLast(), childrenCount));
            } else {
                sb.append(printNodeAsInOrder(p, p.parent, childrenCount));
                TNode pnt = p;
                p = p.right;
                while (p != null) {
                    stack.addLast(p);
                    pnt = p;
                    p = p.left;
                }
                stack.addLast(pnt);
                stack.addLast(null);
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return printAsInOrder();
    }

    /**
     * 插入的具体实现
     * @param value
     */
    protected abstract void doInsert(int value);

    /**
     * 插入节点
     * @param value 新节点的值
     */
    public final void insert(int value) {
        System.out.print(this.getClass().getSimpleName() + " insert value=" + value);
        doInsert(value);
        System.out.println(", after insert: ");
        System.out.println(this);
    }

    /**
     * 删除的具体实现
     * @param value
     */
    protected abstract boolean doDelete(int value);

    /**
     * 删除值
     * @param value
     * @return 是否成功
     */
    public final boolean delete(int value) {
        System.out.print(this.getClass().getSimpleName() + " delete value=" + value);
        boolean success = doDelete(value);
        System.out.println(", after delete: ");
        System.out.println(this);
        return success;
    }
}
